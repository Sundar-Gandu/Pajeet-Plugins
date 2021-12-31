package net.runelite.client.plugins.oneclickthieving;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Locatable;
import net.runelite.api.MenuAction;
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
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
@PluginDescriptor(name = "One Click Pickpocket", description = "QOL for pickpocketing", tags = {"Sundar", "Pickpocket", "Skilling", "Thieving"}, enabledByDefault = false)
public class OneClickThievingPlugin extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(OneClickThievingPlugin.class);

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
    OneClickThievingConfig provideConfig(ConfigManager configManager) {
        return (OneClickThievingConfig)configManager.getConfig(OneClickThievingConfig.class);
    }

    protected void startUp() {}

    protected void shutDown() {}

    Set<String> foodMenuOption = Set.of("Drink", "Eat");

    Set<Integer> prayerPotionIDs = Set.of((Integer[])new Integer[] {
            Integer.valueOf(139), Integer.valueOf(141), Integer.valueOf(143), Integer.valueOf(2434), Integer.valueOf(3024), Integer.valueOf(3026), Integer.valueOf(3028), Integer.valueOf(3030), Integer.valueOf(189), Integer.valueOf(191),
            Integer.valueOf(193), Integer.valueOf(2450) });

    Set<Integer> foodBlacklist = Set.of((Integer[])new Integer[] {
            Integer.valueOf(139), Integer.valueOf(141), Integer.valueOf(143), Integer.valueOf(2434), Integer.valueOf(3024), Integer.valueOf(3026), Integer.valueOf(3028), Integer.valueOf(3030), Integer.valueOf(24774), Integer.valueOf(189),
            Integer.valueOf(191), Integer.valueOf(193), Integer.valueOf(2450) });

    Set<Integer> coinPouches = Set.of((Integer[])new Integer[] {
            Integer.valueOf(22521), Integer.valueOf(22522), Integer.valueOf(22523), Integer.valueOf(22524), Integer.valueOf(22525), Integer.valueOf(22526), Integer.valueOf(22527), Integer.valueOf(22528), Integer.valueOf(22529), Integer.valueOf(22530),
            Integer.valueOf(22531), Integer.valueOf(22532), Integer.valueOf(22533), Integer.valueOf(22534), Integer.valueOf(22535), Integer.valueOf(22536), Integer.valueOf(22537), Integer.valueOf(22538), Integer.valueOf(24703) });

    private boolean shouldHeal = false;

    private int prayerTimeOut = 0;

    private static final int DODGY_NECKLACE_ID = 21143;

    private boolean npcKnockedOut = false;

    private long nextKnockOutTick = 0L;

    private boolean foodIsOut = false;

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getMessage().contains("You have run out of prayer points"))
            this.prayerTimeOut = 0;
        if(event.getType() == ChatMessageType.SPAM && (event.getMessage().equals("You smack the bandit over the head and render them unconscious."))){
            this.npcKnockedOut = true;
            this.nextKnockOutTick = this.client.getTickCount() + 4;
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (event.getSkill() == Skill.PRAYER && event.getBoostedLevel() == 0 && this.prayerTimeOut == 0)
            this.prayerTimeOut = 10;
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        if (!this.config.clickOverride() || this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN || this.client.isMenuOpen())
            return;
        this.client.insertMenuItem("One Click Pickpocket", "", MenuAction.UNKNOWN

                .getId(), 0, 0, 0, true);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (this.config.clickOverride() && event.getMenuOption().equals("One Click Pickpocket")) {
            NPC npc = (NPC)(new NPCQuery()).idEquals(new int[] { this.config.npcID() }).result(this.client).nearestTo((Locatable)this.client.getLocalPlayer());
            if (npc != null) {
                event.setMenuEntry(this.client.createMenuEntry("Pickpocket", npc

                        .getName(), npc
                        .getIndex(), MenuAction.NPC_THIRD_OPTION
                        .getId(), 0, 0, false));
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
            } else {
                sendGameMessage("Npc not found please change the id");
                event.consume();
                return;
            }
        }
        changeMenuAction(event);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.prayerTimeOut > 0)
            this.prayerTimeOut--;
        if (this.client.getBoostedSkillLevel(Skill.HITPOINTS) >= Math.min(this.client.getRealSkillLevel(Skill.HITPOINTS), this.config.HPTopThreshold())) {
            this.shouldHeal = false;
        } else if (this.client.getBoostedSkillLevel(Skill.HITPOINTS) <= Math.max(5, this.config.HPBottomThreshold())) {
            this.shouldHeal = true;
        }
        if(this.client.getTickCount() >= this.nextKnockOutTick){
            this.npcKnockedOut = false;
        }
        if(this.config.BlackjackResetFood()){
            this.foodIsOut = false;
        }
    }

    private List<String> getActions(NPC npc) {
        return (List<String>)Arrays.<String>stream(npc.getComposition().getActions()).map(o -> (o == null) ? null : Text.removeTags(o)).collect(Collectors.toList());
    }

    private void changeMenuAction(MenuOptionClicked event) {
        if (this.config.disableWalk() && event.getMenuOption().equals("Walk here")) {
            event.consume();
            return;
        }
        if (!event.getMenuOption().equals("Pickpocket") && !event.getMenuOption().contains("Knock-Out"))
            return;
        WidgetItem coinpouch = getWidgetItem(this.coinPouches);
        if (this.config.enableHeal() && this.shouldHeal) {
            WidgetItem food = getItemMenu(this.foodMenuOption, this.foodBlacklist);
            if(!config.enableBlackjackFix() || this.foodIsOut){
                if (this.config.haltOnLowFood() && food == null) {
                    event.consume();
                    this.notifier.notify("You are out of food");
                    sendGameMessage("You are out of food");
                    return;
                }
            }
            if(config.enableBlackjackFix()){
                if (this.config.haltOnLowFood() && food == null && this.npcKnockedOut) {
                    this.foodIsOut = true;
                    event.consume();
                    this.notifier.notify("You are out of food");
                    sendGameMessage("Food is out toggled");
                    return;
                }
            }

            if(!config.enableBlackjackFix()){
                if (food != null) {
                    String[] foodMenuOptions = this.itemManager.getItemComposition(food.getId()).getInventoryActions();
                    event.setMenuEntry(this.client.createMenuEntry(foodMenuOptions[0], foodMenuOptions[0], food

                            .getId(), MenuAction.ITEM_FIRST_OPTION
                            .getId(), food
                            .getIndex(), WidgetInfo.INVENTORY
                            .getId(), false));
                    return;
                }
            }
            if(config.enableBlackjackFix()){
                if(food != null && this.npcKnockedOut){
                    String[] foodMenuOptions = this.itemManager.getItemComposition(food.getId()).getInventoryActions();
                    event.setMenuEntry(this.client.createMenuEntry(foodMenuOptions[0], foodMenuOptions[0], food

                            .getId(), MenuAction.ITEM_FIRST_OPTION
                            .getId(), food
                            .getIndex(), WidgetInfo.INVENTORY
                            .getId(), false));
                    return;
                }
            }

        }
        if (this.config.enableCoinPouch() && coinpouch != null && coinpouch.getQuantity() == 28) {
            event.setMenuEntry(this.client.createMenuEntry("Open-all", "Coin Pouch", coinpouch

                    .getId(), MenuAction.ITEM_FIRST_OPTION
                    .getId(), coinpouch
                    .getIndex(), WidgetInfo.INVENTORY
                    .getId(), false));
        } else if (this.config.enableNecklace() && getWidgetItem(21143) != null && !isItemEquipped(List.of(Integer.valueOf(21143)))) {
            event.setMenuEntry(this.client.createMenuEntry("Wear", "Necklace", 21143, MenuAction.ITEM_SECOND_OPTION

                            .getId(),
                    getWidgetItem(21143).getIndex(), WidgetInfo.INVENTORY
                            .getId(), false));
        } else if (this.config.enableSpell() && this.client.getVarbitValue(12414) == 0) {
            if (this.client.getVarbitValue(4070) != 3) {
                event.consume();
                this.notifier.notify("You are on the wrong spellbook");
                sendGameMessage("You are on the wrong spellbook");
            } else if (this.client.getBoostedSkillLevel(Skill.MAGIC) >= 47) {
                event.setMenuEntry(this.client.createMenuEntry("Cast", "Shadow Veil", 1, MenuAction.CC_OP

                        .getId(), -1, WidgetInfo.SPELL_SHADOW_VEIL

                        .getId(), false));
            } else {
                event.consume();
                this.notifier.notify("Magic level too low to cast this spell!");
                sendGameMessage("Magic level too low to cast this spell!");
            }
        } else if (this.config.enablePray()) {
            if (this.client.getBoostedSkillLevel(Skill.PRAYER) == 0 && this.prayerTimeOut == 0) {
                WidgetItem prayerPotion = getWidgetItem(this.prayerPotionIDs);
                if (prayerPotion != null) {
                    event.setMenuEntry(this.client.createMenuEntry("Drink", "Prayer", prayerPotion

                            .getId(), MenuAction.ITEM_FIRST_OPTION
                            .getId(), prayerPotion
                            .getIndex(), WidgetInfo.INVENTORY
                            .getId(), false));
                } else if (this.config.haltOnLowFood()) {
                    event.consume();
                    this.notifier.notify("You are out of prayer potions");
                    sendGameMessage("You are out of prayer potions");
                }
            } else if (this.client.getVarbitValue(Varbits.PRAYER_REDEMPTION.getId()) == 0 && this.client.getBoostedSkillLevel(Skill.PRAYER) > 0) {
                if ((this.config.prayMethod() == PrayMethod.REACTIVE_PRAY && shouldPray()) || this.config
                        .prayMethod() == PrayMethod.LAZY_PRAY)
                    event.setMenuEntry(this.client.createMenuEntry("Activate", "Redemption", 1, MenuAction.CC_OP

                            .getId(), -1, WidgetInfo.PRAYER_REDEMPTION

                            .getId(), false));
            }
        }
    }

    private boolean shouldPray() {
        return (this.client.getBoostedSkillLevel(Skill.HITPOINTS) < 11);
    }

    public boolean isItemEquipped(Collection<Integer> itemIds) {
        assert this.client.isClientThread();
        ItemContainer equipmentContainer = this.client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipmentContainer != null) {
            Item[] items = equipmentContainer.getItems();
            for (Item item : items) {
                if (itemIds.contains(Integer.valueOf(item.getId())))
                    return true;
            }
        }
        return false;
    }

    public WidgetItem getWidgetItem(Collection<Integer> ids) {
        Widget inventoryWidget = this.client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ids.contains(Integer.valueOf(item.getId())))
                    return item;
            }
        }
        return null;
    }

    private WidgetItem getWidgetItem(int id) {
        return getWidgetItem(Collections.singletonList(Integer.valueOf(id)));
    }

    private WidgetItem getItemMenu(Collection<String> menuOptions, Collection<Integer> ignoreIDs) {
        Widget inventoryWidget = this.client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (ignoreIDs.contains(Integer.valueOf(item.getId())))
                    continue;
                String[] menuActions = this.itemManager.getItemComposition(item.getId()).getInventoryActions();
                for (String action : menuActions) {
                    if (action != null && menuOptions.contains(action))
                        return item;
                }
            }
        }
        return null;
    }

    private void sendGameMessage(String message) {
        String chatMessage = (new ChatMessageBuilder()).append(ChatColorType.HIGHLIGHT).append(message).build();
        this.chatMessageManager
                .queue(QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(chatMessage)
                        .build());
    }
}