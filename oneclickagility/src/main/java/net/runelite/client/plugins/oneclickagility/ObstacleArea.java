package net.runelite.client.plugins.oneclickagility;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Locatable;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;


@Slf4j
public class ObstacleArea
{
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int z;
    @Getter
    private final int nextObstacleID;
    @Setter
    private TileObject nextObstacle;

    ObstacleArea(int minX,int maxX, int minY, int maxY,int z, int nextObstacleID)
    {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.z = z;
        this.nextObstacleID = nextObstacleID;
    }

    public boolean containsObject(Locatable locatable)
    {
        return containsObject(locatable.getWorldLocation());
    }

    public boolean containsObject(WorldPoint worldPoint)
    {
        return containsObject(worldPoint.getX(),worldPoint.getY(), worldPoint.getPlane());
    }

    private boolean containsObject(int x, int y, int z)
    {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z == this.z;
    }

    public MenuEntry createMenuEntry(Client client)
    {
        if (nextObstacle != null)
        {
            return client.createMenuEntry(
                    "Interact",
                    "Obstacle",
                    nextObstacle.getId(),
                    MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                    getObjectParam(nextObstacle),
                    getObjectParam1(nextObstacle),
                    true);
        }
        return null;
    }

    private int getObjectParam(Locatable gameObject)
    {
        if (gameObject instanceof GameObject)
        {
            return ((GameObject) gameObject).getSceneMinLocation().getX();
        }
        return(gameObject.getLocalLocation().getSceneX());
    }

    private int getObjectParam1(Locatable gameObject)
    {
        if (gameObject instanceof GameObject)
        {
            return ((GameObject) gameObject).getSceneMinLocation().getY();
        }
        return(gameObject.getLocalLocation().getSceneY());
    }
}
