package net.runelite.client.plugins.oneclickagility;

import net.runelite.api.TileObject;

import java.util.List;

public class PyramidCourse extends Course
{
    PyramidCourse(List<ObstacleArea> obstacleAreaList)
    {
        super(obstacleAreaList);
    }

    @Override
    void addObstacle(TileObject obstacle)
    {
        for (ObstacleArea area:obstacleAreaList)
        {
            if (obstacle.getId() == area.getNextObstacleID() && area.containsObject(obstacle))
            {
                area.setNextObstacle(obstacle);
                return;
            }
        }
    }

    @Override
    void removeObstacle(TileObject obstacle)
    {
        for (ObstacleArea area:obstacleAreaList)
        {
            if (obstacle.getId() == area.getNextObstacleID() && area.containsObject(obstacle))
            {
                area.setNextObstacle(null);
                return;
            }
        }
    }
}
