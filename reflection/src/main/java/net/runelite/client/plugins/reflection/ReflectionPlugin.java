/*
 * Copyright (c) 2021, Nicole <github.com/losingticks>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.reflection;

import com.google.inject.Provides;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.RuneLite;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.discord.DiscordService;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.config.ConfigPlugin;
import net.runelite.client.plugins.discord.DiscordPlugin;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "Reflection",
        tags = {"config", "rewriter", "reflection", "zhuri/nicole","sundar", "pajeet"},
        enabledByDefault = false
)
@PluginDependency(ConfigPlugin.class)
@Slf4j
public class ReflectionPlugin extends Plugin
{
    private static final BufferedImage[] CONFIG_RESOURCES = new BufferedImage[2];
    private static final File ICON_FILE = new File(RuneLite.RUNELITE_DIR, "icon.png");
    private static Color originalColour;

    static
    {
        CONFIG_RESOURCES[0] = getImageFromConfigResource("star_on");
        CONFIG_RESOURCES[1] = getImageFromConfigResource("switcher_on");
    }

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ClientUI clientUI;
    @Inject
    private DiscordService discordService;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private ReflectionConfig config;

    private static ImageIcon remapImage(BufferedImage image, Color color, boolean restore)
    {
        if (color != null && !restore)
        {
            BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), 2);
            Graphics2D graphics = img.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.setColor(color);
            graphics.setComposite(AlphaComposite.getInstance(10, 1));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.dispose();
            return new ImageIcon(img);
        } else
        {
            return new ImageIcon(ImageUtil.recolorImage(image, originalColour));
        }
    }

    private static BufferedImage getImageFromConfigResource(String imgName)
    {
        try
        {
            Class<?> clazz = Class.forName("net.runelite.client.plugins.config.ConfigPanel");
            return ImageUtil.loadImageResource(clazz, imgName.concat(".png"));
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Provides
    ReflectionConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ReflectionConfig.class);
    }

    protected void startUp()
    {
        clientThread.invokeLater(()->{
            if (!(client.getGameState() == GameState.LOGGED_IN) && !(client.getGameState() == GameState.LOGIN_SCREEN))
                return false;
            originalColour = ColorScheme.BRAND_ORANGE;
            updateIcon(false);
            updateClientTitle(false);
            updateDiscordAppID(false);
            updatePluginListResourceImages(false);
            return true;
        });
    }

    protected void shutDown()
    {
        clientThread.invokeLater(()->{
            if (client.getGameState() == GameState.LOADING || client.getGameState() == GameState.UNKNOWN)
                return false;

            updateIcon(true);
            updateClientTitle(true);
            updateDiscordAppID(true);
            updatePluginListResourceImages(true);
            return true;
        });

    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if (event.getGroup().equalsIgnoreCase("reflection"))
        {
            switch (event.getKey())
            {
                case "clientTitle":
                    updateClientTitle(false);
                    break;
                case "discordAppId":
                    updateDiscordAppID(false);
                    break;
                case "changeIcon":
                    updateIcon(config.changeIcon());
                    break;
                case "pluginTitleColor":
                case "pluginStarOnColor":
                case "pluginSwitcherOnColor":
                    updatePluginListResourceImages(false);
                    break;
            }
        }
    }

    private void updateIcon(boolean restore)
    {
        Image image;
        if  (restore || !config.changeIcon())
        {
            image = ImageUtil.loadImageResource(ClientUI.class, "/openosrs.png");
        }
        else
        {
            try
            {
                image = ImageIO.read(ICON_FILE);
            } catch (IOException e)
            {
                image = ImageUtil.loadImageResource(ClientUI.class, "/runelite.png");
            }
        }
        ClientUI.getFrame().setIconImage(image);
    }

    private void updateDiscordAppID(boolean restore)
    {
        DiscordPlugin plugin = (DiscordPlugin) pluginManager.getPlugins().stream().filter((pl) -> pl instanceof DiscordPlugin).findAny().orElse(null);
        if (plugin != null)
        {
            boolean restoreAppId = restore || Strings.isNullOrEmpty(config.getDiscordAppId());
            String appId = restoreAppId ? "409416265891971072" : config.getDiscordAppId();

            try
            {
                Object state = FieldUtils.readDeclaredField(plugin, "discordState", true);
                discordService.close();
                FieldUtils.writeDeclaredField(state, "runeliteTitle", "RuneLite", true);
                FieldUtils.writeDeclaredField(discordService, "discordAppId", appId, true);
                discordService.init();
                if (pluginManager.isPluginEnabled(plugin))
                {
                    pluginManager.stopPlugin(plugin);
                    pluginManager.startPlugin(plugin);
                }
            } catch (Exception e)
            {
                log.debug("error: {}", e.getMessage());
            }

        }
    }

    private void updateClientTitle(boolean restore)
    {
        boolean restoreTitle = restore || Strings.isNullOrEmpty(config.getClientTitle());
        String clientTitle = restoreTitle ? RuneLiteProperties.getTitle() : config.getClientTitle();

        try
        {
            FieldUtils.writeDeclaredField(clientUI, "title", clientTitle, true);
            clientThread.invokeLater(() ->
            {
                SwingUtilities.invokeLater(() ->
                {
                    try
                    {
                        MethodUtils.invokeMethod(clientUI, true, "updateFrameConfig", true);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var2)
                    {
                        log.debug("Couldn't invoke 'updateFrameConfig' method in {}", clientUI.getClass());
                        log.debug("Method Invocation Exception: Message -> {}", var2.getMessage());
                    }

                });
            });
        } catch (Exception var5)
        {
            var5.printStackTrace();
        }

    }

    private void updatePluginListResourceImages(boolean restore)
    {
        if (config.pluginSwitcherOnColor() != null || restore)
        {
            try
            {
                Class<?> colorScheme = Class.forName("net.runelite.client.ui.ColorScheme");
                Field osField = colorScheme.getDeclaredField("BRAND_BLUE");
                osField.setAccessible(true);
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(osField, osField.getModifiers() & -17);
                osField.set(null, restore ? originalColour : config.pluginSwitcherOnColor());

            } catch (Exception e)
            {
                log.debug("Exception Message -> {}", e.getMessage());
            }
            try
            {
                Class<?> colorScheme = Class.forName("net.runelite.client.ui.ColorScheme");
                Field osField = colorScheme.getDeclaredField("BRAND_ORANGE");
                osField.setAccessible(true);
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(osField, osField.getModifiers() & -17);
                osField.set(null, restore ? originalColour : config.pluginSwitcherOnColor());

            } catch (Exception e)
            {
                log.debug("Exception Message -> {}", e.getMessage());
            }
            try
            {
                Class<?> colorScheme = Class.forName("net.runelite.client.ui.components.ToggleButton");
                Field osField = colorScheme.getDeclaredField("ON_SWITCHER");
                osField.setAccessible(true);
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(osField, osField.getModifiers() & -17);
                osField.set(null, remapImage(CONFIG_RESOURCES[1], config.pluginSwitcherOnColor(), restore));

            } catch (Exception e)
            {
                log.debug("Exception Message -> {}", e.getMessage());
            }
        }

        ConfigPlugin configPlugin = (ConfigPlugin) pluginManager.getPlugins().stream().filter((plugin) ->
                plugin instanceof ConfigPlugin).findAny().orElse(null);
        if (configPlugin == null)
        {
            log.debug("Couldn't find ConfigPlugin");
        }
        else
        {
            try
            {
                Object pluginListPanel = FieldUtils.readDeclaredField(configPlugin, "pluginListPanel", true);
                Class<?> configPanelClass = Class.forName("net.runelite.client.plugins.config.ConfigPanel");
                JPanel muxer = (JPanel) FieldUtils.readDeclaredField(pluginListPanel, "muxer", true);
                Component[] var6 = muxer.getComponents();

                for (Component c : var6)
                {
                    if (configPanelClass.isInstance(c))
                    {
                        JLabel title = (JLabel) FieldUtils.readDeclaredField(c, "title", true);
                        title.setForeground(restore ? Color.WHITE : config.pluginTitleColor());
                        JToggleButton pluginToggle = (JToggleButton) FieldUtils.readDeclaredField(c, "pluginToggle", true);
                        pluginToggle.setSelectedIcon(remapImage(CONFIG_RESOURCES[1], config.pluginSwitcherOnColor(), restore));
                    }
                }

                List<?> pluginList = (List<?>) FieldUtils.readDeclaredField(pluginListPanel, "pluginList", true);

                for (Object plugin : pluginList)
                {
                    if (plugin instanceof JPanel)
                    {
                        Component[] var12 = ((JPanel) plugin).getComponents();
                        for (Component c : var12)
                        {
                            if (c instanceof JLabel)
                            {
                                c.setForeground(restore ? Color.WHITE : config.pluginTitleColor());
                            }
                        }
                    }

                    JToggleButton pinButton = (JToggleButton) FieldUtils.readDeclaredField(plugin, "pinButton", true);
                    pinButton.setSelectedIcon(remapImage(CONFIG_RESOURCES[0], config.pluginStarOnColor(), restore));
                    JToggleButton onOffToggle = (JToggleButton) FieldUtils.readDeclaredField(plugin, "onOffToggle", true);
                    onOffToggle.setSelectedIcon(remapImage(CONFIG_RESOURCES[1], config.pluginSwitcherOnColor(), restore));
                }
            }
            catch (Exception e)
            {
                log.debug("Exception Message -> {}", e.getMessage());
            }

        }
    }
}

