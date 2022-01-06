package net.runelite.client.plugins.oneclickdropper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.InventoryWidgetItemQuery;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.Client;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

@Extension
@PluginDescriptor(
      name = "One Click Dropper",
      description = "Drop items without having to click on them",
      tags = {"sundar", "pajeet"},
      enabledByDefault = false
)

@Slf4j
public class OneClickDropperPlugin extends Plugin
{

   @Inject
   private Client client;

   @Inject
   private ClientThread clientThread;

   @Inject
   private OneClickDropperConfig config;

   @Inject
   private ConfigManager configManager;

   @Provides
   OneClickDropperConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(OneClickDropperConfig.class);
   }

   boolean dropping = false;
   int previousSize = 0;
   List<WidgetItem> dropList;
   ListIterator<WidgetItem> dropListIterator;

   Set<Integer> dropIDs = Set.of(19669,11328,11332,11330);
   HashMap<Integer,Integer> dropOrder;

   @Override
   protected void startUp()
   {
      parseConfig();
   }

   @Override
   protected void shutDown()
   {
      dropListIterator = null;
      dropList = null;
      dropOrder = null;
      dropping = false;
   }

   @Subscribe
   private void onConfigChanged(ConfigChanged event)
   {
      if(event.getGroup().equals("oneclickdropper"))
      {
         parseConfig();
         dropping = false;
         clientThread.execute(this::createDropList);
      }
   }

   @Subscribe
   private void onMenuOptionClicked(MenuOptionClicked event)
   {
      if (!event.getMenuOption().equals("One Click Drop")) return;

      if(dropping)
      {
         if(dropListIterator.hasNext())
         {
            event.setMenuEntry(createDropMenuEntry(dropListIterator.next()));
         }
         if(!dropListIterator.hasNext())
         {
            dropping = false;
         }
      }
   }

   @Subscribe
   private void onClientTick(ClientTick event)
   {
      if(client.getLocalPlayer() == null
              || client.getGameState() != GameState.LOGGED_IN
              || client.getItemContainer(InventoryID.BANK) != null
              || client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null
              || client.isMenuOpen()
      )
         return;

      if(dropping)
      {
         client.insertMenuItem(
                 "One Click Drop",
                 "",
                 MenuAction.UNKNOWN.getId(),
                 0,
                 0,
                 0,
                 true
         );
      }
   }

   @Subscribe
   private void onItemContainerChanged(ItemContainerChanged event)
   {
      if (event.getContainerId() != InventoryID.INVENTORY.getId())
      {
         return;
      }
      createDropList();
   }

   //has to be called on client thread
   private void createDropList()
   {
      int size = client.getWidget(WidgetInfo.INVENTORY).getWidgetItems().size();
      if (size == 28)
      {
         updateDropList();
      }
      else if (!config.requireFullInventory() && (!dropping || size >= previousSize))
      {
         updateDropList();
      }
      previousSize = size;
   }

   private void updateDropList()
   {
      dropList = getItems(dropIDs);
      if( dropList == null || dropList.size() == 0)
      {
         dropping = false;
         return;
      }
      dropListIterator = dropList.listIterator();
      dropping = true;
   }

   public List<WidgetItem> getItems(Collection<Integer> ids)
   {
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      List<WidgetItem> matchedItems = new ArrayList<>();

      if (inventoryWidget != null)
      {
         for(int i = 0; i <= 27; i++)
         {
            int index = config.customDrop() ? dropOrder.get(i) : i;
            WidgetItem item = getItemAtIndex(index);
            if (item != null && ids.contains(item.getId()))
            {
               matchedItems.add(item);
            }
         }
         return matchedItems;
      }
      return null;
   }

   public WidgetItem getItemAtIndex(int index) {
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null) {
         Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
         for (WidgetItem item : items) {
            if (item.getIndex() == index) {
               return item;
            }
         }
      }
      return null;
   }

   private MenuEntry createDropMenuEntry(WidgetItem item)
   {
      return client.createMenuEntry(
              "Drop",
              "Item",
              item.getId(),
              MenuAction.ITEM_FIFTH_OPTION.getId(),
              item.getIndex(),
              9764864,
              false);
   }

   private void parseConfig()
   {
      dropIDs = new HashSet<>();
      dropOrder = new HashMap<>();
      for (String s : Text.COMMA_SPLITTER.split(config.itemIDs()))
      {
         try
         {
            dropIDs.add(Integer.parseInt(s));
         }
         catch (NumberFormatException ignored)
         {
         }
      }

      int order = 0;
      Set<Integer> uniquieIndexes = new HashSet<>();
      for (String s : Text.COMMA_SPLITTER.split(config.dropOrder()))
      {
         try
         {
            int inventoryIndex = Integer.parseInt(s)-1;

            //check if inx is out of bounds or already used
            if(inventoryIndex > 27 || inventoryIndex < 0 || uniquieIndexes.contains(inventoryIndex))
            {
               continue;
            }
            uniquieIndexes.add(inventoryIndex);
            dropOrder.put(order,inventoryIndex);
            order++;
         }
         catch (Exception ignored)
         {
            //catches the error from Integer.parseInt()
         }
      }
   }
}
