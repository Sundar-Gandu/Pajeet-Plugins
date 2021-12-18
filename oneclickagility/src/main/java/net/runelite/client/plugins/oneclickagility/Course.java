package net.runelite.client.plugins.oneclickagility;

import net.runelite.api.Player;
import net.runelite.api.TileObject;

import java.util.ArrayList;
import java.util.List;


public class Course
{
    List<Integer> obstacleIDs;
    List<ObstacleArea> obstacleAreaList;

    Course(List<ObstacleArea> obstacleAreaList)
    {
        List<Integer> obstacleIDs = new ArrayList<>();
        for (ObstacleArea area : obstacleAreaList)
        {
            if (!obstacleIDs.contains(area.getNextObstacleID()))
            {
                obstacleIDs.add(area.getNextObstacleID());
            }
        }
        this.obstacleIDs = obstacleIDs;
        this.obstacleAreaList = obstacleAreaList;
    }

    ObstacleArea getCurrentObstacleArea(Player player)
    {
        if(player == null)
            return null;

        for(ObstacleArea area:obstacleAreaList)
        {
            if(area.containsObject(player))
            {
                return area;
            }
        }
        return null;
    }

    void addObstacle(TileObject obstacle)
    {
        for (ObstacleArea area:obstacleAreaList)
        {
            if (obstacle.getId() == area.getNextObstacleID())
            {
                if (area instanceof SpecificObstacleArea
                        && !obstacle.getWorldLocation().equals(((SpecificObstacleArea) area).getObstacleLocation()))
                {
                    continue;
                }
                area.setNextObstacle(obstacle);
            }
        }
    }

    void removeObstacle(TileObject obstacle)
    {
        for (ObstacleArea area:obstacleAreaList)
        {
            if (obstacle.getId() == area.getNextObstacleID())
            {
                if (area instanceof SpecificObstacleArea
                        && !obstacle.getWorldLocation().equals(((SpecificObstacleArea) area).getObstacleLocation()))
                {
                    continue;
                }
                area.setNextObstacle(null);
            }
        }
    }
}
