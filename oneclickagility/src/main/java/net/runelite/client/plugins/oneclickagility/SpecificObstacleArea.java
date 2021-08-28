package net.runelite.client.plugins.oneclickagility;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

public class SpecificObstacleArea extends ObstacleArea
{
    @Getter
    private final WorldPoint obstacleLocation;

    SpecificObstacleArea(int minX, int maxX, int minY, int maxY, int z, int nextObstacleID, WorldPoint obstacleLocation)
    {
        super(minX, maxX, minY, maxY, z, nextObstacleID);
        this.obstacleLocation = obstacleLocation;
    }
}
