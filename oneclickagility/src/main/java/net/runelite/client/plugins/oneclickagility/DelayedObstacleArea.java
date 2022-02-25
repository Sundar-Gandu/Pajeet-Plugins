package net.runelite.client.plugins.oneclickagility;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.coords.WorldPoint;

public class DelayedObstacleArea extends ObstacleArea
{
    private int tickClicked = 0;
    private WorldPoint locClicked = new WorldPoint(0,0,0);

    DelayedObstacleArea(int minX, int maxX, int minY, int maxY, int z, int nextObstacleID)
    {
        super(minX, maxX, minY, maxY, z, nextObstacleID);
    }

    @Override
    public MenuEntry createMenuEntry(Client client)
    {
        if (nextObstacle != null
                //kinda ugly, doesnt really flow well when spam clicked
                && (client.getTickCount() > tickClicked + 1 || !client.getLocalPlayer().getWorldLocation().equals(locClicked)))
        {
            tickClicked = client.getTickCount();
            locClicked = client.getLocalPlayer().getWorldLocation();
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
}
