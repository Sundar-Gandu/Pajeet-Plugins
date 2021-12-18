package net.runelite.client.plugins.oneclickagility;

import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;

public class StickObstacleArea extends SpecificObstacleArea
{
    StickObstacleArea(int minX, int maxX, int minY, int maxY, int z, int nextObstacleID, WorldPoint obstacleLocation)
    {
        super(minX, maxX, minY, maxY, z, nextObstacleID, obstacleLocation);
    }

    @Override
    public MenuEntry createMenuEntry(Client client)
    {
        TileItem stick = getStick(client);
        if (stick != null)
        {
            return client.createMenuEntry("Take",
                    "Stick",
                    4179,
                    MenuAction.GROUND_ITEM_THIRD_OPTION.getId(),
                    stick.getTile().getLocalLocation().getSceneX(),
                    stick.getTile().getLocalLocation().getSceneY(),
                    false);
        }
        else
        {
            return super.createMenuEntry(client);
        }
    }

    //theres probably an api for this, but i couldnt find it
    private TileItem getStick(Client client)
    {
        List<Tile> tilesList = new ArrayList<>();
        Scene scene = client.getScene();
        Tile[][][] tiles = scene.getTiles();
        int z = client.getPlane();
        for (int x = 0; x < Constants.SCENE_SIZE; ++x)
        {
            for (int y = 0; y < Constants.SCENE_SIZE; ++y)
            {
                Tile tile = tiles[z][x][y];
                if (tile == null)
                {
                    continue;
                }
                tilesList.add(tile);
            }
        }

        for (Tile tile:tilesList)
        {
            if (tile.getGroundItems() != null)
            {
                for (TileItem item:tile.getGroundItems())
                {
                    if (item.getId() == 4179)
                        return item;
                }
            }
        }
        return null;
    }
}
