package net.runelite.client.plugins.oneclickagility;

import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class WerewolfObstacleArea extends ObstacleArea
{
    WerewolfObstacleArea()
    {
        super(3523, 3549, 9861, 9897, 0, -1);
    }

    WorldPoint point = new WorldPoint(3538, 9874, 0);

    @Override
    public MenuEntry createMenuEntry(Client client)
    {
        NPC werewolf = new NPCQuery().idEquals(5927).result(client).first();
        if (client.getLocalPlayer().getWorldLocation().getY() > 9875)
        {
            return client.createMenuEntry("Walk here",
                    "",
                    0,
                    MenuAction.WALK.getId(),
                    3528,
                    9866,
                    false);
        }
        else if (getItems(Collections.singletonList(ItemID.STICK),client) != null && werewolf != null)
            return client.createMenuEntry("Give-Stick",
                    "Agility Trainer",
                    werewolf.getIndex(),
                    MenuAction.NPC_FIRST_OPTION.getId(),
                    0,
                    0,
                    false);
        else
            return client.createMenuEntry("Walk here",
                    "",
                    0,
                    MenuAction.WALK.getId(),
                    point.getX(),
                    point.getY(),
                    false);

    }


    public Widget getItems(Collection<Integer> ids, Client client)
    {
        client.runScript(6009, 9764864, 28, 1, -1);
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null && inventoryWidget.getDynamicChildren() != null)
        {
            Widget[] items = inventoryWidget.getDynamicChildren();
            for(Widget item : items)
            {
                if (ids.contains(item.getItemId()))
                {
                    return item;
                }
            }
        }
        return null;
    }

}
