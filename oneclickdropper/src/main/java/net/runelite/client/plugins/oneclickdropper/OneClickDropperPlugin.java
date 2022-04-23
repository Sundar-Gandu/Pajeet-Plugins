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
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

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
   List<Widget> dropList;
   ListIterator<Widget> dropListIterator;

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
            setEntry(event, itemEntry(dropListIterator.next(), 7));
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
      int size = nonemptyInventorySlots();
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

   public List<Widget> getItems(Collection<Integer> ids)
   {
      client.runScript(6009, 9764864, 28, 1, -1);
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      List<Widget> matchedItems = new ArrayList<>();

      if (inventoryWidget != null && inventoryWidget.getChildren() != null)
      {
         for(int i = 0; i <= 27; i++)
         {
            int index = config.customDrop() ? dropOrder.get(i) : i;
            Widget item = inventoryWidget.getChild(index);
            if (item != null && ids.contains(item.getItemId()))
            {
               matchedItems.add(item);
            }
         }
         return matchedItems;
      }
      return null;
   }

   private int nonemptyInventorySlots()
   {
      Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
      if (inventory == null)
      {
         return 28;
      }

      return (int) Arrays.stream(inventory.getDynamicChildren()).filter(w -> w.getItemId() != 6512).count();
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
         //catches the error from Integer.parseInt()
         catch (NumberFormatException ignored) {}
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
         //catches the error from Integer.parseInt()
         catch (NumberFormatException ignored) {}
      }
   }

   public MenuEntry itemEntry(Widget item, int action)
   {
      if (item == null)
         return null;

      return client.createMenuEntry(
              "",
              "",
              action,
              action < 6 ? MenuAction.CC_OP.getId() : MenuAction.CC_OP_LOW_PRIORITY.getId(),
              item.getIndex(),
              WidgetInfo.INVENTORY.getId(),
              false
      );
   }

   public void setEntry(MenuOptionClicked event, MenuEntry entry)
   {
      try
      {
         event.setMenuOption(entry.getOption());
         event.setMenuTarget(entry.getTarget());
         event.setId(entry.getIdentifier());
         event.setMenuAction(entry.getType());
         event.setParam0(entry.getParam0());
         event.setParam1(entry.getParam1());
      }
      catch (Exception e)
      {
         event.consume();
      }
   }
}
