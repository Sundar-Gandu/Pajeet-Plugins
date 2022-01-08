package net.runelite.client.plugins.oneclickrunenable;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickrunenable")
public interface OneClickRunEnableConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "minimumRun",
            name = "Minimum Run Energy",
            description = "This is the minimum amount before the plugin will let you run on"
    )
    default int minimumRun()
    {
        return 15;
    }
}
