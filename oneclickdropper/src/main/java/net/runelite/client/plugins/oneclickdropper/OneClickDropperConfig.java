package net.runelite.client.plugins.oneclickdropper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickdropper")
public interface OneClickDropperConfig extends Config
{
    @ConfigItem(
		    position = 0,
		    keyName = "requireFullInventory",
		    name = "Full Inventory",
		    description = "Only drop when inventory is full"
    )
    default boolean requireFullInventory()
    {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "itemIDs",
            name = "Item IDs",
            description = "ID of items to drop"
    )
    default String itemIDs()
    {
        return "11328,11332,11330";
    }

    @ConfigItem(
            position = 2,
            keyName = "customDrop",
            name = "Change Drop Order",
            description = "Enable this to use the custom drop order"
    )
    default boolean customDrop()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "dropOrder",
            name = "Drop Order",
            description = "The order to drop items in. 1 is the top left, 28 is the bottom right"
    )
    default String dropOrder()
    {
        return "1,2,5,6,9,10,13,14,17,18,21,22,25,26,3,4,7,8,11,12,15,16,19,20,23,24,27,28";
    }

}
