package net.runelite.client.plugins.disablefakeclaw;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.List;


@Extension
@PluginDescriptor(
      name = "Hide Fake Claws",
      description = "Disable animation of django claws",
      tags = {"Sundar","claws","animation","pk"},
      enabledByDefault = false
)

@Slf4j
public class disablefakeclawPlugin extends Plugin
{
   @Inject
   private Client client;

   @Inject
   private disablefakeclawConfig config;

   @Inject
   private ConfigManager configManager;

   @Provides
   disablefakeclawConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(disablefakeclawConfig.class);
   }

   private static final int DJANGO_CLAW_ANIMATION = 5283;
   private static final int OLM_TELEPORT = 1359;
   private static final int RED_RINGS = 481;

   @Subscribe
   private void onGameTick(GameTick event)
   {
      List<Player> playerList = client.getPlayers();
      for(Player player:playerList)
      {
         if (player.getAnimation()==DJANGO_CLAW_ANIMATION)
         {
            player.setAnimation(-1);
            player.setAnimationFrame(0);
            if (config.showIndicator()) player.setGraphic(RED_RINGS);
         }
      }
   }

   @Override
   protected void startUp()
   {

   }

   @Override
   protected void shutDown()
   {

   }
}
