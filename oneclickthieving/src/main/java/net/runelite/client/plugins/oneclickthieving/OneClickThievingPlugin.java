package net.runelite.client.plugins.oneclickthieving;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
      name = "One Click Pickpocket",
      description = "QOL for pickpocketing",
      tags = {"sundar", "pajeet","pickpocket","skilling","thieving"},
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
   Set<Integer> prayerPotionIDs = Set.of(139,141,143,2434,3024,3026,3028,3030,189,191,193,2450,26340,26342,26344,26346);
   Set<Integer> foodBlacklist = Set.of(139,141,143,2434,3024,3026,3028,3030,24774,189,191,193,2450,26340,26342,26344,26346);
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
   private void onClientTick(ClientTick event)
   {
      if(!config.clickOverride()
            || client.getLocalPlayer() == null
            || client.getGameState() != GameState.LOGGED_IN
            || client.isMenuOpen()
            || client.getWidget(378,78) != null)//login button
      {
         return;
      }
      client.insertMenuItem(
              "One Click Pickpocket",
              "",
              MenuAction.UNKNOWN.getId(),
              0,
              0,
              0,
              true);
   }

   @Subscribe
   public void onMenuOptionClicked(MenuOptionClicked event)
   {
      //change click to pickpocket
      if(config.clickOverride() && event.getMenuOption().equals("One Click Pickpocket"))
      {
         NPC npc =  new NPCQuery().idEquals(config.npcID()).result(client).nearestTo(client.getLocalPlayer());
         if (npc != null)
         {
            setEntry(event, client.createMenuEntry(
                    "Pickpocket",
                     "Pickpocket",
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
      return Arrays.stream(npc.getComposition().getActions()).map(o -> o == null ? null : Text.removeTags(o)).collect(Collectors.toList());
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

      Widget coinpouch = getItem(coinPouches);

      if(config.enableHeal() && shouldHeal)
      {
         Widget food = getItemMenu(foodMenuOption, foodBlacklist);
         if (config.haltOnLowFood() && food == null)
         {
            event.consume();
            notifier.notify("You are out of food");
            sendGameMessage("You are out of food");
            return;
         }
         else if (food != null)
         {
            setEntry(event, itemEntry(food, 2));
            return;
         }
         //else fallthrough
      }

      if (config.enableCoinPouch() && coinpouch != null && coinpouch.getItemQuantity() == 28)
      {
         setEntry(event, itemEntry(coinpouch, 2));
      }
      //dodgy necklace
      else if(config.enableNecklace() && getItem(List.of(DODGY_NECKLACE_ID)) != null && !isItemEquipped(List.of(DODGY_NECKLACE_ID)))
      {
         setEntry(event, itemEntry(getItem(List.of(DODGY_NECKLACE_ID)), 3));
      }
      //varbit is shadowveil cooldown
      else if(config.enableSpell() && client.getVarbitValue(12414) == 0)
      {
         //check spellbook
         if(client.getVarbitValue(4070) != 3)
         {
            event.consume();
            notifier.notify("You are on the wrong spellbook");
            sendGameMessage("You are on the wrong spellbook");
         }
         else if(client.getBoostedSkillLevel(Skill.MAGIC) >= 47)
         {
            setEntry(event, client.createMenuEntry(
                    "Cast",
                    "Shadow Veil",
                    1,
                    MenuAction.CC_OP.getId(),
                    -1,
                    WidgetInfo.SPELL_SHADOW_VEIL.getId(),
                    false));
         } 
         else 
         {
            event.consume();
            notifier.notify("Magic level too low to cast this spell!");
            sendGameMessage("Magic level too low to cast this spell!");
         }
      }
      else if(config.enablePray())
      {
         if (client.getBoostedSkillLevel(Skill.PRAYER) == 0 && prayerTimeOut == 0)
         {
            Widget prayerPotion = getItem(prayerPotionIDs);
            if (prayerPotion != null)
            {
               setEntry(event, itemEntry(prayerPotion, 2));
            }
            else if (config.haltOnLowFood())
            {
               event.consume();
               notifier.notify("You are out of prayer potions");
               sendGameMessage("You are out of prayer potions");
            }
         }
         //if redemption is off
         else if(client.getVarbitValue(Varbits.PRAYER_REDEMPTION) == 0 && client.getBoostedSkillLevel(Skill.PRAYER) > 0 )
         {
            if ((config.prayMethod() == PrayMethod.REACTIVE_PRAY && shouldPray())
                    || config.prayMethod() == PrayMethod.LAZY_PRAY)
            {
               setEntry(event, client.createMenuEntry(
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


   private Widget getItem(Collection<Integer> ids) {
      List<Widget> matches = getItems(ids);
      return matches.size() != 0 ? matches.get(0) : null;
   }

   private ArrayList<Widget> getItems(Collection<Integer> ids)
   {
      client.runScript(6009, 9764864, 28, 1, -1);
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      ArrayList<Widget> matchedItems = new ArrayList<>();

      if (inventoryWidget != null && inventoryWidget.getDynamicChildren() != null)
      {
         Widget[] items = inventoryWidget.getDynamicChildren();
         for(Widget item : items)
         {
            if (ids.contains(item.getItemId()))
            {
               matchedItems.add(item);
            }
         }
      }
      return matchedItems;
   }

   private MenuEntry itemEntry(Widget item, int action)
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

   private void setEntry(MenuOptionClicked event, MenuEntry entry)
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

   private Widget getItemMenu(Collection<String>menuOptions, Collection<Integer> ignoreIDs) {
      client.runScript(6009, 9764864, 28, 1, -1);
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null && inventoryWidget.getChildren() != null) {
         Widget[] items = inventoryWidget.getChildren();
         for (Widget item : items) {
            if (ignoreIDs.contains(item.getItemId())) {
               continue;
            }
            String[] menuActions = itemManager.getItemComposition(item.getItemId()).getInventoryActions();
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
