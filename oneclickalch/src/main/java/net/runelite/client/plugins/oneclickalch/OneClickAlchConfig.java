package net.runelite.client.plugins.oneclickalch;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickalch")
public interface OneClickAlchConfig extends Config
{
    @ConfigItem(
		    position = 0,
		    keyName = "itemID",
		    name = "Item ID",
		    description = "ID of the item to be alched"
    )
    default int itemID()
    {
        return -1;
    }
}
