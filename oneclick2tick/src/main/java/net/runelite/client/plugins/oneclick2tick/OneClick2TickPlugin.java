package net.runelite.client.plugins.oneclick2tick;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.Client;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

@Extension
@PluginDescriptor(
      name = "One Click 2 Tick",
      description = "2 tick teaks and fishing",
      tags = {"sundar", "pajeet"},
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
   Set<Integer> dropIDs = Set.of(
           ItemID.RAW_TUNA,
           ItemID.TUNA,
           ItemID.RAW_SWORDFISH,
           ItemID.SWORDFISH,
           ItemID.RAW_SHARK,
           ItemID.SHARK,
           ItemID.TEAK_LOGS);

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
         //lets the menu entry go through normally
         cooldown = true;
         return;
      }
      else if (tick == 2)
      {
         Widget itemToDrop = getWidgetItem(dropIDs);
         if (itemToDrop != null && config.dropItems())
         {
            event.setMenuEntry(createDropMenuEntry(itemToDrop));
         }
         else if (event.getMenuTarget().contains("Teak"))
         {
            event.consume();
            walkTile(event.getParam0(),event.getParam1());
         }
         else
         {
            event.consume();
         }
         cooldown = true;
      }
   }

   public Widget getWidgetItem(Collection<Integer> ids) {
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null && inventoryWidget.getChildren() != null) {
         Widget[] items = inventoryWidget.getChildren();
         for (Widget item : items) {
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

   private MenuEntry createDropMenuEntry(Widget item)
   {
      return client.createMenuEntry("Drop",
              "Item",
              item.getId(),
              MenuAction.ITEM_FIFTH_OPTION.getId(),
              item.getIndex(),
              WidgetInfo.INVENTORY.getId(),
              false);
   }
}
