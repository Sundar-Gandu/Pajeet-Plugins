package net.runelite.client.plugins.oneclick2tick;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclick2tick")
public interface OneClick2TickConfig extends Config
{
    @ConfigItem(
            position = 0,
            keyName = "dropItems",
            name = "Drop Items",
            description = "I dont know why you would disable this."
    )
    default boolean dropItems()
    {
        return true;
    }
}
