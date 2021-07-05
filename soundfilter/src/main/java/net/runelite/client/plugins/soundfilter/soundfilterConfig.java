package net.runelite.client.plugins.soundfilter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("soundfilter")
public interface soundfilterConfig extends Config
{
    enum FilterMode
    {
        DISABLED,
        WHITELIST,
        BLACKLIST,
    }

    @ConfigItem(
		    position = 0,
		    keyName = "soundDebug",
		    name = "Enable Sound Debug",
		    description = "Enable this to find the id of sounds being played"
    )
    default boolean soundDebug()
    {
        return false;
    }

    @ConfigItem(
            position = 1,
            keyName = "filterMode",
            name = "Type of filter",
            description = ""
    )
    default FilterMode filterMode()
    {
        return FilterMode.DISABLED;
    }

    @ConfigItem(
            position = 2,
            keyName = "effectwhitelist",
            name = "Sound Effect Whitelist",
            description = "ONLY sound effects in this list will be played" +
                    "<br>Use the format 2665,2424,530",
            hidden = true,
            unhide = "filterMode",
            unhideValue = "WHITELIST"
    )
    default String effectwhitelist()
    {
        return "";
    }

    @ConfigItem(
            position = 3,
            keyName = "areawhitelist",
            name = "Area Sound Whitelist",
            description = "ONLY area sounds in this list will be played" +
                    "<br>Use the format 2665,2424,530",
            hidden = true,
            unhide = "filterMode",
            unhideValue = "WHITELIST"
    )
    default String areawhitelist()
    {
        return "";
    }

    @ConfigItem(
            position = 4,
            keyName = "effectblacklist",
            name = "Sound Effect Blacklist",
            description = "Any sound effects in this list will NOT be played" +
                    "<br>Use the format 2665,2424,530",
            hidden = true,
            unhide = "filterMode",
            unhideValue = "BLACKLIST"
    )
    default String effectblacklist()
    {
        return "";
    }

    @ConfigItem(
            position = 5,
            keyName = "areablacklist",
            name = "Area Sound Blacklist",
            description = "Any area sounds in this list will NOT be played" +
                    "<br>Use the format 2665,2424,530",
            hidden = true,
            unhide = "filterMode",
            unhideValue = "BLACKLIST"
    )
    default String areablacklist()
    {
        return "";
    }

}
