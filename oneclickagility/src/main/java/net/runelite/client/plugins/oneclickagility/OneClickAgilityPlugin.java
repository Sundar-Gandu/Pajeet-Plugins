package net.runelite.client.plugins.oneclickagility;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.GameEventManager;
import net.runelite.rs.api.RSClient;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Extension
@PluginDescriptor(
        name = "One Click Agility",
        description = "Reclined gaming",
        tags = {"Sundar","Agility","One Click","Skilling"},
        enabledByDefault = false
)

@Slf4j
@Singleton
public class OneClickAgilityPlugin extends Plugin
{

    @Inject
    private Client client;

    @Inject
    GameEventManager gameEventManager;

    @Inject
    ItemManager itemManager;

    @Inject
    private OneClickAgilityConfig config;

    @Inject
    private ConfigManager configManager;

    @Provides
    OneClickAgilityConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(OneClickAgilityConfig.class);
    }

    private static final int MARK_ID = 11849;
    private static final Set<Integer> PORTAL_IDS = Set.of(36241,36242,36243,36244,36245,36246);
    private static final Set<Integer> SUMMER_PIE_ID = Set.of(7220,7218);
    private static final WorldPoint SEERS_END = new WorldPoint(2704,3464,0);
    private static final WorldPoint PYRAMID_TOP_RIGHT = new WorldPoint(3043,4697,3);
    private static final WorldPoint PYRAMID_TOP_LEFT = new WorldPoint(3042,4697,3);

    ArrayList<Tile> marks = new ArrayList<>();
    ArrayList<GameObject> portals = new ArrayList<>();
    DecorativeObject pyramidTopObstacle;
    GameObject pyramidTop;
    Course course;

    @Override
    protected void startUp()
    {
        course = CourseFactory.build(config.courseSelection(),client);
    }

    @Override
    protected void shutDown()
    {

    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if(event.getGroup().equals("oneclickagility"))
        {
            course = CourseFactory.build(config.courseSelection(),client);
            gameEventManager.simulateGameEvents(this);
        }
    }
    
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) 
    {
        if (config.showGreenText())
        {
            if(event.getMenuOption().equals("<col=00ff00>One Click Agility")) 
            {
                handleClick(event);
                log.info(event.getMenuOption()+ ", "
                        + event.getMenuTarget() + ", "
                        + event.getId() + ", "
                        + event.getMenuAction().name() + ", "
                        + event.getParam0() + ", "
                        + event.getParam1());
            }
        } 
        else if(event.getMenuOption().equals("")) 
        {
            handleClick(event);
            log.info(event.getMenuOption()+ ", "
                    + event.getMenuTarget() + ", "
                    + event.getId() + ", "
                    + event.getMenuAction().name() + ", "
                    + event.getParam0() + ", "
                    + event.getParam1());
        } 
        else if(event.getMenuOption().equals("Crossing Obstacle")) {
            event.consume();
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event)
    {
        if(client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN) return;
        String text;

        if(course.getCurrentObstacleArea(client.getLocalPlayer()) == null) 
        {
            if (config.consumeMisclicks()) 
            {
                text = "Crossing Obstacle";
            } 
            else 
            {
                return;
            }
        } 
        else if (config.showGreenText())
        {
            text =  "<col=00ff00>One Click Agility";
        } 
        else 
        {
            text = "";
        }

        client.insertMenuItem(
                text,
                "",
                MenuAction.UNKNOWN.getId(),
                0,
                0,
                0,
                true);
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        if(event.getGameObject() == null)
        {
            return;
        }

        if(event.getGameObject().getId() == 10869)
        {
            pyramidTop = event.getGameObject();
        }
        if (PORTAL_IDS.contains(event.getGameObject().getId()))
        {
            portals.add(event.getGameObject());
            return;
        }

        addToCourse(event.getGameObject());
    }

    @Subscribe
    public void onGameObjectDepawned(GameObjectDespawned event)
    {
        if(event.getGameObject() == null)
        {
            return;
        }
        if (PORTAL_IDS.contains(event.getGameObject().getId()))
        {
            portals.remove(event.getGameObject());
            return;
        }
        if(event.getGameObject().getId() == 10869)
        {
            pyramidTop = null;
        }
        removeFromCourse(event.getGameObject());
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event)
    {
        addToCourse(event.getWallObject());
    }


    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event)
    {
        removeFromCourse(event.getWallObject());
    }


    @Subscribe
    public void DecorativeObjectSpawned(DecorativeObjectSpawned event)
    {
        if(event.getDecorativeObject().getId() == 10851)
        {
            if(pyramidTopObstacle == null || pyramidTopObstacle.getY() > event.getDecorativeObject().getY())
            {
                pyramidTopObstacle = event.getDecorativeObject();
                return;
            }
        }

        addToCourse(event.getDecorativeObject());
    }

    @Subscribe
    public void DecorativeObjectDespawned(DecorativeObjectDespawned event)
    {
        if(event.getDecorativeObject().getId() == 10851 && event.getDecorativeObject() == pyramidTopObstacle)
        {
            pyramidTopObstacle = null;
            return;
        }

        removeFromCourse(event.getDecorativeObject());
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event)
    {
        addToCourse(event.getGroundObject());
    }

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned event)
    {
        removeFromCourse(event.getGroundObject());
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned event)
    {
        if (event.getItem().getId() == MARK_ID)
        {
            marks.add(event.getTile());
        }
    }

    @Subscribe
    public void ItemDespawned(ItemDespawned event)
    {
        if (event.getItem().getId() == MARK_ID)
        {
            marks.remove(event.getTile());
        }
    }

    private void addToCourse(TileObject tileObject)
    {
        if (course.obstacleIDs.contains(tileObject.getId()))
        {
            course.addObstacle(tileObject);
        }
    }

    private void removeFromCourse(TileObject tileObject)
    {
        if (course.obstacleIDs.contains(tileObject.getId()))
        {
            course.removeObstacle(tileObject);
        }
    }

    private void handleClick(MenuOptionClicked event)
    {
        if(config.skillBoost())
        {
            int boost = client.getBoostedSkillLevel(Skill.AGILITY)-client.getRealSkillLevel(Skill.AGILITY);
            if(config.boostAmount()>boost)
            {
                WidgetItem food = getWidgetItem(SUMMER_PIE_ID);
                if (food != null)
                {
                    event.setMenuEntry(createSummerPieMenuEntry(food));
                    return;
                }
            }
        }

        if(config.seersTele() && config.courseSelection() == AgilityCourse.SEERS_VILLAGE)
        {   //spellbook varbit, worldpoint of dropdown tile, teleportation animation ID
            if(client.getVarbitValue(4070) == 0 && client.getLocalPlayer().getWorldLocation().equals(SEERS_END) && client.getLocalPlayer().getAnimation() != 714)
            {
                event.setMenuEntry(createSeersTeleportMenuEntry());
                return;
            }
        }

        if(config.courseSelection() == AgilityCourse.AGILITY_PYRAMID)
        {
            if((client.getLocalPlayer().getWorldLocation().equals(PYRAMID_TOP_RIGHT) || client.getLocalPlayer().getWorldLocation().equals(PYRAMID_TOP_LEFT))
                    && pyramidTop.getRenderable().getModelHeight() == 309)
            {
                event.setMenuEntry(createPyramidTopMenuEntry());
                return;
            }
        }

        ObstacleArea obstacleArea = course.getCurrentObstacleArea(client.getLocalPlayer());
        if (obstacleArea == null)
        {
            return;
        }

        if (config.pickUpMarks() && !marks.isEmpty())
        {
            Tile wrongMarkTile = null;
            for (Tile mark : marks)
            {
                if (obstacleArea.containsObject(mark))
                {
                    Tile markTile = client.getScene().getTiles()[mark.getPlane()][mark.getSceneLocation().getX()][mark.getSceneLocation().getY()];

                    if (markTile != null && checkTileForMark(markTile))
                    {
                        event.setMenuEntry(createMarkMenuEntry(mark));
                        return;
                    }
                    else
                    {
                        wrongMarkTile = mark;
                    }
                }
            }

            if(wrongMarkTile != null)
            {
                marks.remove(wrongMarkTile);
            }
        }

        if (!portals.isEmpty())
        {
            for(GameObject portal:portals)
            {
                if (obstacleArea.containsObject(portal) && portal.getClickbox() != null)
                {
                    event.setMenuEntry(createPortalMenuEntry(portal));
                    return;
                }
            }
        }

        if(config.consumeMisclicks() &&
                (client.getLocalPlayer().isMoving()
                        || client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation()))
        {
            event.consume();
            return;
        }

        event.setMenuEntry(obstacleArea.createMenuEntry());

        if (event.getMenuOption().equals("Walk here"))
        {
            event.consume();
            LocalPoint point = LocalPoint.fromWorld(client, new WorldPoint(event.getParam0(),event.getParam1(),client.getPlane()));
            walkTile(point.getSceneX(), point.getSceneY());

        }
    }

    private boolean checkTileForMark(Tile tile)
    {
        List<TileItem> items = tile.getGroundItems();
        if (items == null)
        {
            log.info("no item found");
            return false;
        }

        for (TileItem item:items)
        {
            if (item == null)
                continue;

            if(item.getId() == MARK_ID)
                return true;
        }
        log.info("no matching item found");
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

    private void walkTile(int x, int y)
    {
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    private MenuEntry createSeersTeleportMenuEntry()
    {
        return new MenuEntry(
                "Seers'",
                "Camelot Teleport",
                2,
                MenuAction.CC_OP.getId(),
                -1,
                14286879,
                true);
    }

    private MenuEntry createSummerPieMenuEntry(WidgetItem food)
    {
        String[] foodMenuOptions = itemManager.getItemComposition(food.getId()).getInventoryActions();
        return new MenuEntry(
                foodMenuOptions[0],
                foodMenuOptions[0],
                food.getId(),
                MenuAction.ITEM_FIRST_OPTION.getId(),
                food.getIndex(),
                9764864,
                true);
    }

    private MenuEntry createMarkMenuEntry(Tile tile)
    {
        return new MenuEntry("Take",
                "Mark of Grace",
                MARK_ID,
                MenuAction.GROUND_ITEM_THIRD_OPTION.getId(),
                tile.getSceneLocation().getX(),
                tile.getSceneLocation().getY(),
                true);
    }

    private MenuEntry createPortalMenuEntry(GameObject portal)
    {
        return new MenuEntry(
                "Travel",
                "Portal",
                portal.getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                portal.getLocalLocation().getSceneX(),
                portal.getLocalLocation().getSceneY(),
                true
        );
    }

    private MenuEntry createPyramidTopMenuEntry()
    {
        return new MenuEntry(
                "Climb",
                "Climbing rocks",
                pyramidTopObstacle.getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                pyramidTopObstacle.getLocalLocation().getSceneX(),
                pyramidTopObstacle.getLocalLocation().getSceneY(),
                true
        );
    }

}
