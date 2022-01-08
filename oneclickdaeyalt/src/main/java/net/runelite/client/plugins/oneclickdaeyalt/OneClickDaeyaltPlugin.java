package net.runelite.client.plugins.oneclickdaeyalt;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.ObjectID;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.Client;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Extension
@PluginDescriptor(
        name = "One Click Daeyalt",
        description = "Click anywhere to mine the essence",
        tags = {"sundar", "pajeet"},
        enabledByDefault = false
)

@Slf4j
public class OneClickDaeyaltPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Provides
    OneClickDaeyaltConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickDaeyaltConfig.class);
    }

    private static final int DAEYALT_MINE_REGION = 14744;
    private static final Set<Integer> MINING_ANIM = Set.of(
        AnimationID.MINING_BRONZE_PICKAXE,
        AnimationID.MINING_IRON_PICKAXE,
        AnimationID.MINING_STEEL_PICKAXE,
        AnimationID.MINING_BLACK_PICKAXE,
        AnimationID.MINING_MITHRIL_PICKAXE,
        AnimationID.MINING_ADAMANT_PICKAXE,
        AnimationID.MINING_RUNE_PICKAXE,
        AnimationID.MINING_GILDED_PICKAXE,
        AnimationID.MINING_DRAGON_PICKAXE,
        AnimationID.MINING_DRAGON_PICKAXE_UPGRADED,
        AnimationID.MINING_DRAGON_PICKAXE_OR,
        AnimationID.MINING_DRAGON_PICKAXE_OR_TRAILBLAZER,
        AnimationID.MINING_INFERNAL_PICKAXE,
        AnimationID.MINING_3A_PICKAXE,
        AnimationID.MINING_CRYSTAL_PICKAXE,
        AnimationID.MINING_TRAILBLAZER_PICKAXE,
        AnimationID.MINING_TRAILBLAZER_PICKAXE_2,
        AnimationID.MINING_TRAILBLAZER_PICKAXE_3,
        AnimationID.MINING_MOTHERLODE_BRONZE,
        AnimationID.MINING_MOTHERLODE_IRON,
        AnimationID.MINING_MOTHERLODE_STEEL,
        AnimationID.MINING_MOTHERLODE_BLACK,
        AnimationID.MINING_MOTHERLODE_MITHRIL,
        AnimationID.MINING_MOTHERLODE_ADAMANT,
        AnimationID.MINING_MOTHERLODE_RUNE,
        AnimationID.MINING_MOTHERLODE_GILDED,
        AnimationID.MINING_MOTHERLODE_DRAGON,
        AnimationID.MINING_MOTHERLODE_DRAGON_UPGRADED,
        AnimationID.MINING_MOTHERLODE_DRAGON_OR,
        AnimationID.MINING_MOTHERLODE_DRAGON_OR_TRAILBLAZER,
        AnimationID.MINING_MOTHERLODE_INFERNAL,
        AnimationID.MINING_MOTHERLODE_3A,
        AnimationID.MINING_MOTHERLODE_CRYSTAL,
        AnimationID.MINING_MOTHERLODE_TRAILBLAZER
    );

    @Override
    protected void startUp()
    {
    }

    @Override
    protected void shutDown()
    {
    }

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        if (client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
            return;

        if (client.getLocalPlayer().getWorldLocation().getRegionID() == DAEYALT_MINE_REGION)
        {
            client.insertMenuItem(
                    "One Click Daeyalt",
                    "",
                    MenuAction.CC_OP.getId(),
                    0,
                    0,
                    0,
                    true);
        }
    }

    @Subscribe
    void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (!event.getMenuOption().equals("One Click Daeyalt"))
        {
            return;
        }

        if (client.getLocalPlayer().isMoving()
                || MINING_ANIM.contains(client.getLocalPlayer().getAnimation())
                || client.getLocalPlayer().getIdlePoseAnimation() != client.getLocalPlayer().getPoseAnimation())
        {
            event.consume();
            return;
        }

        GameObject essence = new GameObjectQuery()
                .idEquals(ObjectID.DAEYALT_ESSENCE_39095)
                .result(client)
                .nearestTo(client.getLocalPlayer());

        if (essence != null)
        {
            event.setMenuEntry(client.createMenuEntry(
                    "Mine",
                    "Daeyalt Essence",
                    ObjectID.DAEYALT_ESSENCE_39095,
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    essence.getSceneMinLocation().getX(),
                    essence.getSceneMinLocation().getY(),
                    false
            ));
        }
        else
        {
           event.consume();
        }
    }
}
