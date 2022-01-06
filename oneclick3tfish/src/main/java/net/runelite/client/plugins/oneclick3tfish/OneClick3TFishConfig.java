package net.runelite.client.plugins.oneclick3tfish;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclick3tfish")
public interface OneClick3TFishConfig extends Config
{
    @ConfigItem(
		    position = 0,
		    keyName = "manipType",
		    name = "Tick Manip type",
		    description = ""
    )
    default TickMethod manipType()
    {
        return TickMethod.HERB_TAR;
    }

    @ConfigItem(
            position = 1,
            keyName = "flavorText",
            name = "Menu Entry Text",
            description = "Change the menu entry to show what action it will perform"
    )
    default boolean flavorText()
    {
        return false;
    }

    @ConfigItem(
            position = 2,
            keyName = "clickAnywhere",
            name = "Click Anywhere",
            description = "Enable this if you dont want to manually click on the spot"
    )
    default boolean clickAnywhere()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName = "dropFish",
            name = "Drop Fish",
            description = "Drop fish if you click right after tick manipulating"
    )
    default boolean dropFish()
    {
        return true;
    }
}
