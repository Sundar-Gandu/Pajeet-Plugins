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
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
   Set<Integer> prayerPotionIDs = Set.of(139,141,143,2434,3024,3026,3028,3030);
   Set<Integer> foodBlacklist = Set.of(139,141,143,2434,3024,3026,3028,3030,24774);
   Set<Integer> coinPouches = Set.of(22521,22522,22523,22524,22525,22526,22527,22528,22529,22530,22531,22532,22533,22534,22535,22536,22537,22538,24703);
   private boolean shouldHeal = false;
   private int prayerTimeOut = 0;

   @Subscribe
   public void onChatMessage(ChatMessage event)
   {
      if(event.getMessage().contains("You have run out of prayer points"))
      {
         log.info("prayer actually drained");
         prayerTimeOut = 0;
      }

   }

   @Subscribe
   public void onStatChanged(StatChanged event)
   {
      if(event.getSkill() == Skill.PRAYER && event.getBoostedLevel() == 0 && prayerTimeOut == 0)
      {
         log.info("prayer drained");
         prayerTimeOut = 10;
      }
   }

   @Subscribe
   public void onMenuOptionClicked(MenuOptionClicked event)
   {
      method2(event);
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

   private void method2(MenuOptionClicked event)
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

      //dodgy necklace
      if(config.enableNecklace() && getWidgetItem(21143) != null && !isItemEquipped(List.of(21143)))
      {
         event.setMenuEntry(new MenuEntry("Wear", "Wear", 21143, MenuAction.ITEM_SECOND_OPTION.getId(), getWidgetItem(21143).getIndex(), 9764864, false));
         return;
      }

      WidgetItem coinpouch = getWidgetItem(coinPouches);
      if (config.enableCoinPouch() && coinpouch != null && coinpouch.getQuantity() == 28)
      {
         event.setMenuEntry(new MenuEntry("Open-all", "Open-all", 22531, MenuAction.ITEM_FIRST_OPTION.getId(), coinpouch.getIndex(), 9764864, false));
         return;
      }

      if(config.enableHeal() && shouldHeal)
      {
         WidgetItem food = getItemMenu(foodMenuOption,foodBlacklist);
         if (config.haltOnLowFood() && food == null)
         {
            event.consume();
            notifier.notify("You are out of food");
            sendGameMessage("You are out of food");
            return;
         }
         else if (food != null)
         {
            String[] foodMenuOptions = itemManager.getItemComposition(food.getId()).getInventoryActions();
            log.info(foodMenuOptions[0]);
            event.setMenuEntry(new MenuEntry(foodMenuOptions[0], foodMenuOptions[0], food.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), food.getIndex(), 9764864, false));
            return;
         }
      }

      if(config.enableSpell())
      {
         //check spellbook
         if(client.getVarbitValue(4070) != 3)
         {
            event.consume();
            notifier.notify("You are on the wrong spellbook");
            sendGameMessage("You are on the wrong spellbook");
            return;
         }

         if(client.getVarbitValue(12414) == 0)
         {
            event.setMenuEntry(new MenuEntry("Cast", "<col=00ff00>Shadow Veil</col>", 1, MenuAction.CC_OP.getId(), -1, 14287024, false));
            return;
         }
      }

      if(config.enablePray())
      {
         if (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && prayerTimeOut == 0)
         {
            WidgetItem prayerPotion = getWidgetItem(prayerPotionIDs);
            if (prayerPotion != null)
            {
               event.setMenuEntry(new MenuEntry("Drink", "Drink", prayerPotion.getId(), MenuAction.ITEM_FIRST_OPTION.getId(), prayerPotion.getIndex(), 9764864, false));
               return;
            }
         }

         //if redemption is off
         if(client.getVarbitValue(4120) == 0 && client.getBoostedSkillLevel(Skill.PRAYER) > 0 )
         {
            if(config.prayMethod() == PrayMethod.LAZY_PRAY)
            {
               event.setMenuEntry(new MenuEntry("Activate", "<col=ff9040>Redemption</col>", 1, MenuAction.CC_OP.getId(), -1, 35454997, false));
               return;
            }
            else if(config.prayMethod() == PrayMethod.REACTIVE_PRAY && shouldPray())
            {
               event.setMenuEntry(new MenuEntry("Activate", "<col=ff9040>Redemption</col>", 1, MenuAction.CC_OP.getId(), -1, 35454997, false));
               return;
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

   private int getRandomIntBetweenRange(int min, int max) {
      //return (int) ((Math.random() * ((max - min) + 1)) + min); //This does not allow return of negative values
      return ThreadLocalRandom.current().nextInt(min, max + 1);
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
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null) {
         Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
         for (WidgetItem item : items) {
            if (item.getId() == id) {
               return item;
            }
         }
      }
      return null;
   }

   public WidgetItem getItemMenu(Collection<String>menuOptions, Collection<Integer> ignoreIDs) {
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

   private WidgetItem getItemMenu(Collection<String> menuOptions) {
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null) {
         Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
         for (WidgetItem item : items) {
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
