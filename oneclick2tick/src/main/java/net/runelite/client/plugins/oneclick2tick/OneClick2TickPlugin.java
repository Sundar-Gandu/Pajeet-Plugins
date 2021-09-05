package net.runelite.client.plugins.oneclick2tick;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.Client;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Extension
@PluginDescriptor(
      name = "One Click 2 Tick",
      description = "2 tick teaks and fishing",
      tags = {"Sundar"},
      enabledByDefault = false
)

@Slf4j
public class OneClick2TickPlugin extends Plugin
{
   @Inject
   private OneClick2TickConfig config;

   @Inject
   private Client client;

   @Inject
   private ConfigManager configManager;

   @Provides
   OneClick2TickConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(OneClick2TickConfig.class);
   }

   @Override
   protected void startUp()
   {
   }

   @Override
   protected void shutDown()
   {
   }

   int tick;
   boolean cooldown;
   Set<Integer> dropIDs = Set.of(359,371,6333);

   @Subscribe
   private void onGameTick(GameTick event)
   {
      tick++;
      cooldown = false;
   }

   @Subscribe
   private void onHitsplatApplied(HitsplatApplied event)
   {
      if (event.getActor() == client.getLocalPlayer())
      {
         tick = 0;
      }
   }

   @Subscribe
   private void onMenuEntryAdded(MenuEntryAdded event)
   {
      if(event.getTarget().contains("Fishing spot") || event.getTarget().contains("Teak"))
      {
         if(tick == 2)
         {
            event.setOption("Drop");
            event.setModified();
         }
      }
   }

   @Subscribe
   private void onMenuOptionClicked(MenuOptionClicked event)
   {
      if (!event.getMenuTarget().contains("Fishing spot") && !event.getMenuTarget().contains("Teak"))
         return;

      if(cooldown)
      {
         event.consume();
         return;
      }

      if (tick <= 1)
      {
         cooldown = true;
         return;
      }
      else if (tick == 2)
      {
         WidgetItem itemToDrop = getWidgetItem(dropIDs);
         if (itemToDrop != null)
         {
            event.setMenuEntry(createDropMenuEntry(itemToDrop));
         }
         else if (event.getMenuTarget().contains("Teak"))
         {
            event.consume();
            cooldown = true;
            walkTile(event.getParam0(),event.getParam1());
            return;
         }
         else
         {
            event.consume();
         }
         cooldown = true;
         return;
      }
   }

   public WidgetItem getWidgetItem(Collection<Integer> ids) {
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null) {
         Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
         for (WidgetItem item : items) {
            if (ids.contains(item.getId())) {
               return item;
            }
         }
      }
      return null;
   }

   private void walkTile(int x, int y) {
      RSClient rsClient = (RSClient) client;
      rsClient.setSelectedSceneTileX(x);
      rsClient.setSelectedSceneTileY(y);
      rsClient.setViewportWalking(true);
      rsClient.setCheckClick(false);
   }

   private MenuEntry createDropMenuEntry(WidgetItem item)
   {
      return new MenuEntry("Drop",
              "Item",
              item.getId(),
              MenuAction.ITEM_FIFTH_OPTION.getId(),
              item.getIndex(),
              9764864,
              false);
   }
}
