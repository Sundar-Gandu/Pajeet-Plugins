package net.runelite.client.plugins.disablefakeclaw;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("disablefakeclaw")
public interface disablefakeclawConfig extends Config
{
    @ConfigItem(
		    position = 0,
		    keyName = "showIndicator",
		    name = "Enable Graphical indicator",
		    description = "Replaces claw scratch graphic with different graphic"
    )
    default boolean showIndicator()
    {
        return false;
    }
}
