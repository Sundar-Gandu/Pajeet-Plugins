package net.runelite.client.plugins.oneclickthieving;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
      name = "One Click Pickpocket",
      description = "QOL for pickpocketing",
      tags = {"Sundar","Pickpocket","Skilling","Thieving"},
      enabledByDefault = false
)

@Slf4j
public class OneClickThievingPlugin extends Plugin
{
   @Inject
   private Client client;

   @Inject
   private ItemManager itemManager;

   @Inject
   private OneClickThievingConfig config;

   @Inject
   private Notifier notifier;

   @Inject
   private ChatMessageManager chatMessageManager;

   @Inject
   private ConfigManager configManager;

   @Inject
   private OverlayManager overlayManager;

   @Provides
   OneClickThievingConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(OneClickThievingConfig.class);
   }

   @Override
   protected void startUp()
   {

   }

   @Override
   protected void shutDown()
   {

   }

   Set<String> foodMenuOption = Set.of("Drink","Eat");
   Set<Integer> prayerPotionIDs = Set.of(139,141,143,2434,3024,3026,3028,3030,189,191,193,2450);
   Set<Integer> foodBlacklist = Set.of(139,141,143,2434,3024,3026,3028,3030,24774,189,191,193,2450);
   Set<Integer> coinPouches = Set.of(22521,22522,22523,22524,22525,22526,22527,22528,22529,22530,22531,22532,22533,22534,22535,22536,22537,22538,24703);
   private boolean shouldHeal = false;
   private int prayerTimeOut = 0;

   private static final int DODGY_NECKLACE_ID = 21143;

   @Subscribe
   public void onChatMessage(ChatMessage event)
   {
      if(event.getMessage().contains("You have run out of prayer points"))
      {
         prayerTimeOut = 0;
      }

   }

   @Subscribe
   public void onStatChanged(StatChanged event)
   {
      if(event.getSkill() == Skill.PRAYER && event.getBoostedLevel() == 0 && prayerTimeOut == 0)
      {
         prayerTimeOut = 10;
      }
   }

   @Subscribe
   public void onMenuOptionClicked(MenuOptionClicked event)
   {
      //change click to pickpocket
      if(config.clickOverride())
      {
         NPC npc =  new NPCQuery().idEquals(config.npcID()).result(client).nearestTo(client.getLocalPlayer());
         if (npc != null)
         {
            event.setMenuEntry(new MenuEntry(
                    "Pickpocket",
                     npc.getName(),
                     npc.getIndex(),
                     MenuAction.NPC_THIRD_OPTION.getId(),
                     0,
                     0,
                     false));

            switch (getActions(npc).indexOf("Pickpocket")) {
               case 0:
                  event.setMenuAction(MenuAction.NPC_FIRST_OPTION);
                  break;
               case 1:
                  event.setMenuAction(MenuAction.NPC_SECOND_OPTION);
                  break;
               case 2:
                  event.setMenuAction(MenuAction.NPC_THIRD_OPTION);
                  break;
               case 3:
                  event.setMenuAction(MenuAction.NPC_FOURTH_OPTION);
                  break;
               case 4:
                  event.setMenuAction(MenuAction.NPC_FIFTH_OPTION);
                  break;
               default:
                  sendGameMessage("Did not find pickpocket option on npc, check configs");
                  event.consume();
                  return;
            }
         }
         else
         {
            sendGameMessage("Npc not found please change the id");
            event.consume();
            return;
         }
      }

      changeMenuAction(event);
   }

   @Subscribe
   public void onGameTick(GameTick event)
   {
      if(prayerTimeOut>0)
      {
         prayerTimeOut--;
      }

      if (client.getBoostedSkillLevel(Skill.HITPOINTS) >= Math.min(client.getRealSkillLevel(Skill.HITPOINTS),config.HPTopThreshold()))
      {
         shouldHeal = false;
      }
      else if (client.getBoostedSkillLevel(Skill.HITPOINTS) <= Math.max(5,config.HPBottomThreshold()))
      {
         shouldHeal = true;
      }
   }

   private List<String> getActions(NPC npc) {
      return Arrays.stream(npc.getComposition().getActions()).filter(Objects::nonNull).map(Text::removeTags).collect(Collectors.toList());
   }

   private void changeMenuAction(MenuOptionClicked event)
   {
      if (config.disableWalk() && event.getMenuOption().equals("Walk here"))
      {
         event.consume();
         return;
      }
      if (!event.getMenuOption().equals("Pickpocket"))
      {
         return;
      }

      WidgetItem coinpouch = getWidgetItem(coinPouches);
      if (config.enableCoinPouch() && coinpouch != null && coinpouch.getQuantity() == 28)
      {
         event.setMenuEntry(new MenuEntry(
                 "Open-all",
                 "Coin Pouch",
                 coinpouch.getId(),
                 MenuAction.ITEM_FIRST_OPTION.getId(),
                 coinpouch.getIndex(),
                 WidgetInfo.INVENTORY.getId(),
                 false));
      }
      //dodgy necklace
      else if(config.enableNecklace() && getWidgetItem(DODGY_NECKLACE_ID) != null && !isItemEquipped(List.of(DODGY_NECKLACE_ID)))
      {
         event.setMenuEntry(new MenuEntry(
                 "Wear",
                 "Necklace",
                 DODGY_NECKLACE_ID,
                 MenuAction.ITEM_SECOND_OPTION.getId(),
                 getWidgetItem(DODGY_NECKLACE_ID).getIndex(),
                 WidgetInfo.INVENTORY.getId(), false));
      }
      else if(config.enableHeal() && shouldHeal)
      {
         WidgetItem food = getItemMenu(foodMenuOption,foodBlacklist);
         if (config.haltOnLowFood() && food == null)
         {
            event.consume();
            notifier.notify("You are out of food");
            sendGameMessage("You are out of food");
         }
         else if (food != null)
         {
            String[] foodMenuOptions = itemManager.getItemComposition(food.getId()).getInventoryActions();
            event.setMenuEntry(new MenuEntry(
                    foodMenuOptions[0],
                    foodMenuOptions[0],
                    food.getId(),
                    MenuAction.ITEM_FIRST_OPTION.getId(),
                    food.getIndex(),
                    WidgetInfo.INVENTORY.getId(),
                    false));
         }
      }
      else if(config.enableSpell())
      {
         //check spellbook
         if(client.getVarbitValue(4070) != 3)
         {
            event.consume();
            notifier.notify("You are on the wrong spellbook");
            sendGameMessage("You are on the wrong spellbook");
         }
         //check Shadowveil cooldown
         else if(client.getVarbitValue(12414) == 0)
         {
            event.setMenuEntry(new MenuEntry(
                    "Cast",
                    "Shadow Veil",
                    1,
                    MenuAction.CC_OP.getId(),
                    -1,
                    WidgetInfo.SPELL_SHADOW_VEIL.getId(),
                    false));
         }
      }
      else if(config.enablePray())
      {
         if (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && prayerTimeOut == 0)
         {
            WidgetItem prayerPotion = getWidgetItem(prayerPotionIDs);
            if (prayerPotion != null)
            {
               event.setMenuEntry(new MenuEntry(
                       "Drink",
                       "Prayer",
                       prayerPotion.getId(),
                       MenuAction.ITEM_FIRST_OPTION.getId(),
                       prayerPotion.getIndex(),
                       WidgetInfo.INVENTORY.getId(),
                       false));
            }
         }
         //if redemption is off
         else if(client.getVarbitValue(Varbits.PRAYER_REDEMPTION.getId()) == 0 && client.getBoostedSkillLevel(Skill.PRAYER) > 0 )
         {
            if ((config.prayMethod() == PrayMethod.REACTIVE_PRAY && shouldPray())
                    || config.prayMethod() == PrayMethod.LAZY_PRAY)
            {
               event.setMenuEntry(new MenuEntry(
                       "Activate",
                       "Redemption",
                       1,
                       MenuAction.CC_OP.getId(),
                       -1,
                       WidgetInfo.PRAYER_REDEMPTION.getId(),
                       false));
            }
         }
      }
   }

   private boolean shouldPray()
   {
      return (client.getBoostedSkillLevel(Skill.HITPOINTS) < 11);
   }

   public boolean isItemEquipped(Collection<Integer> itemIds) {
      assert client.isClientThread();

      ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
      if (equipmentContainer != null) {
         Item[] items = equipmentContainer.getItems();
         for (Item item : items) {
            if (itemIds.contains(item.getId())) {
               return true;
            }
         }
      }
      return false;
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

   private WidgetItem getWidgetItem(int id) {
      return getWidgetItem(Collections.singletonList(id));
   }

   private WidgetItem getItemMenu(Collection<String>menuOptions, Collection<Integer> ignoreIDs) {
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null) {
         Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
         for (WidgetItem item : items) {
            if (ignoreIDs.contains(item.getId())) {
               continue;
            }
            String[] menuActions = itemManager.getItemComposition(item.getId()).getInventoryActions();
            for (String action : menuActions) {
               if (action != null && menuOptions.contains(action)) {
                  return item;
               }
            }
         }
      }
      return null;
   }

   private void sendGameMessage(String message) {
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
