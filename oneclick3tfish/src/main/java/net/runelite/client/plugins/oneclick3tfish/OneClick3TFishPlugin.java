package net.runelite.client.plugins.oneclick3tfish;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import static net.runelite.api.ItemID.*;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.Client;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

@Extension
@PluginDescriptor(
      name = "One Click 3T Fish",
      description = "3 tick fishing made easy",
      tags = {"sundar", "pajeet"},
      enabledByDefault = false
)

@Slf4j
public class OneClick3TFishPlugin extends Plugin
{
   @Inject
   private Client client;

   @Inject
   private ChatMessageManager chatMessageManager;

   @Inject
   private OneClick3TFishConfig config;

   @Inject
   private ConfigManager configManager;

   @Provides
   OneClick3TFishConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(OneClick3TFishConfig.class);
   }

   @Override
   protected void startUp()
   {
      tick = 0;
   }

   @Override
   protected void shutDown()
   {
   }

   int tick;
   boolean cooldown;
   boolean drop;
   Set<Integer> fishID = Set.of(LEAPING_TROUT,LEAPING_SALMON,LEAPING_STURGEON,RAW_SALMON,RAW_TROUT);
   Set<Integer> herbID = Set.of(GUAM_LEAF,MARRENTILL,TARROMIN,HARRALANDER);
   Set<Integer> tarID = Set.of(SWAMP_TAR);
   Set<Integer> logID = Set.of(MAHOGANY_LOGS,TEAK_LOGS);
   Set<Integer> knifeID = Set.of(KNIFE);
   Set<Integer> vambID = Set.of(LEATHER_VAMBRACES,GREEN_DHIDE_VAMBRACES,BLUE_DHIDE_VAMBRACES,RED_DHIDE_VAMBRACES,BLACK_DHIDE_VAMBRACES);
   Set<Integer> clawID = Set.of(KEBBIT_CLAWS);


   @Subscribe
   private void onClientTick(ClientTick event)
   {
      if (config.clickAnywhere())
      {
         client.insertMenuItem("One Click 3t Fish","",MenuAction.UNKNOWN.getId(),0,0,0,false);
      }
   }


   @Subscribe
   private void onGameTick(GameTick event)
   {
      tickCooldown();
      cooldown = false;
      drop = false;
   }

   @Subscribe
   private void onMenuEntryAdded(MenuEntryAdded event)
   {
      if(tick < 2 || !config.flavorText() || !event.getTarget().contains("Fishing spot"))
      {
         return;
      }
      else if(tick == 2)
      {
         event.setOption("Wait");
      }
      else if(tick == 3)
      {
         event.setOption("Tick/Drop");
      }
      event.setModified();
   }

   @Subscribe
   private void onMenuOptionClicked(MenuOptionClicked event)
   {
      if (event.getMenuOption().contains("One Click 3t Fish"))
      {
         NPC fishingSpot = new NPCQuery()
                 .nameContains("Fishing spot")
                 .result(client)
                 .nearestTo(client.getLocalPlayer());
         if (fishingSpot != null)
         {
            event.setMenuEntry(createFishMenuEntry(fishingSpot));
         }
         else
         {
            sendGameMessage("Cant find fishing spot");
            event.consume();
            return;
         }

      }

      if (!event.getMenuTarget().contains("Fishing spot"))
         return;

      if(cooldown)
      {
         event.consume();
         return;
      }

      if (tick == 0)
         tick = 1;

      switch (tick)
      {
         case 1:
            //click on spot
            cooldown = true;
            return;
         case 2:
            //wait
            cooldown = true;
            event.consume();
            return;
         case 3:
            //tick manip
            if(drop && config.dropFish())
            {
               Widget dropItem = getWidgetItem(fishID);
               if (dropItem != null)
               {
                  event.setMenuEntry(createDropMenuEntry(dropItem));
               }
               else
               {
                  event.consume();
               }
               cooldown = true;
            }
            else
            {
               tickManip(event);
               Widget dropItem = getWidgetItem(fishID);
               if (dropItem != null)
               {
                  drop = true;
               }
               else
               {
                  cooldown = true;
               }
            }
            return;
      }

   }

   private void tickManip(MenuOptionClicked event)
   {
      Widget highlightedItem;
      Widget usedItem;
      Collection<Integer> highlightedItemID;
      Collection<Integer> usedItemID;

      switch (config.manipType())
      {
         case HERB_TAR:
            highlightedItemID = tarID;
            usedItemID = herbID;
            break;
         case KNIFE_LOG:
            highlightedItemID = knifeID;
            usedItemID = logID;
            break;
         case CLAW_VAMB:
            highlightedItemID = clawID;
            usedItemID = vambID;
            break;
         default:
            throw new IllegalStateException("Unexpected value: " + config.manipType());
      }

      highlightedItem = getWidgetItem(highlightedItemID);
      usedItem = getWidgetItem(usedItemID);

      if(highlightedItem == null || usedItem == null)
      {
         event.consume();
         sendGameMessage("Tick Manipulation items not found");
         return;
      }

      client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
      client.setSelectedItemSlot(highlightedItem.getIndex());
      client.setSelectedItemID(highlightedItem.getId());

      event.setMenuEntry(client.createMenuEntry("Use",
              "Item -> Item",
              usedItem.getId(),
              MenuAction.ITEM_USE_ON_ITEM.getId(),
              usedItem.getIndex(),
              WidgetInfo.INVENTORY.getId(),
              false));
   }

   private void tickCooldown()
   {
      if(tick == 0)
         return;
      else if(tick == 3)
         tick = 0;
      else
         tick++;
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

   private MenuEntry createDropMenuEntry(Widget item)
   {
      return client.createMenuEntry(
              "Drop",
              "Item",
              item.getId(),
              MenuAction.ITEM_FIFTH_OPTION.getId(),
              item.getIndex(),
              WidgetInfo.INVENTORY.getId(),
              false);
   }

   private MenuEntry createFishMenuEntry(NPC fish)
   {
      return client.createMenuEntry(
              "Cast",
              "Fishing spot",
              fish.getIndex(),
              MenuAction.NPC_FIRST_OPTION.getId(),
              0,
              0,
              false);
   }

   public void sendGameMessage(String message) {
      String chatMessage = new ChatMessageBuilder()
              .append(ChatColorType.HIGHLIGHT)
              .append(message)
              .build();

      chatMessageManager
              .queue(QueuedMessage.builder()
                      .type(ChatMessageType.CONSOLE)
                      .runeLiteFormattedMessage(chatMessage)
                      .build());
   }

}
