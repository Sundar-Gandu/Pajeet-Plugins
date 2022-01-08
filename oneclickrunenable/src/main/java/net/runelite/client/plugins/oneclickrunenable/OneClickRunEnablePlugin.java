package net.runelite.client.plugins.oneclickrunenable;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.Client;
import org.pf4j.Extension;
import javax.inject.Inject;

@Extension
@PluginDescriptor(
      name = "One Click Run Enable",
      description = "Click anywhere to reenable run",
      tags = {"sundar", "pajeet"},
      enabledByDefault = false
)

@Slf4j
public class OneClickRunEnablePlugin extends Plugin
{
   @Inject
   private OneClickRunEnableConfig config;

   @Inject
   private Client client;

   @Inject
   private ConfigManager configManager;

   @Provides
   OneClickRunEnableConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(OneClickRunEnableConfig.class);
   }

   @Override
   protected void startUp()
   {
   }

   @Override
   protected void shutDown()
   {
   }

   boolean clicked;

   @Subscribe
   private void onClientTick(ClientTick event)
   {
      if (client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
         return;

      if (client.getVarpValue(173) == 0 && client.getEnergy() >= Math.min(config.minimumRun(),100) && !clicked)
      {
         client.insertMenuItem(
                 "One Click Enable Run",
                 "",
                 MenuAction.CC_OP.getId(),
                 0,
                 0,
                 0,
                 true);
         return;
      }
   }

   @Subscribe
   private void onMenuOptionClicked(MenuOptionClicked event)
   {
      if (event.getMenuOption().equals("One Click Enable Run"))
      {
         event.setMenuEntry(client.createMenuEntry(
                 "Toggle Run",
                 "",
                 1,
                 MenuAction.CC_OP.getId(),
                 -1,
                 WidgetInfo.MINIMAP_TOGGLE_RUN_ORB.getId(),
                 false));
      }
   }

   @Subscribe
   private void onGameTick(GameTick event)
   {
      clicked = false;
   }

}
