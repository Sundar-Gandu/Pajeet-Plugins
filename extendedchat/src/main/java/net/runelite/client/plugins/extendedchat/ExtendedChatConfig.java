package net.runelite.client.plugins.extendedchat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ModifierlessKeybind;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

@ConfigGroup("extendedchat")
public interface ExtendedChatConfig extends Config
{
    @ConfigItem(
		    position = 0,
		    keyName = "togglepasting",
		    name = "Chat Pasting",
		    description = "Enables Ctrl+V pasting into the chat"
    )
    default boolean enablePasting()
    {
        return false;
    }

    @ConfigItem(
            position = 1,
            keyName = "toggleprevious",
            name = "Previous Message",
            description = "Sets your chatbox to the prevous message"
    )
    default boolean enablePrevious()
    {
        return false;
    }

    @ConfigItem(
            position = 2,
            keyName = "previouskeybind",
            name = "Previous Message Keybind",
            description = "The key which will set your chatbox to the prevous message"
    )
    default Keybind previouskeybind()
    {
        return new Keybind(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK);
    }

    @ConfigItem(
            position = 3,
            keyName = "toggleincomingfiltering",
            name = "Enable incoming chat filtering",
            description = "Filters incoming chat messages"
    )
    default boolean enableIncomingFiltering()
    {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "toggleoutgoingfiltering",
            name = "Enable outgoing chat filtering",
            description = "Filters outgoing chat messages"
    )
    default boolean enableOutgoingFiltering()
    {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "incomingtextswaps",
            name = "Incoming Text Swaps",
            description = "Add custom swaps here, 1 per line."+
                    "<br>Syntax: trigger word:swapped phrase" +
                    "<br>Examples:" +
                    "<br> sit:good fight" +
                    "<br> dds:Dragon Dagger" +
                    "<br> linux:I'd just like to interject for a moment.  What you're referring to as Linux, is in fact, GNU/Linux, or as I've recently taken to calling it, GNU plus Linux.",
            hidden = true,
            unhide = "toggleincomingfiltering"
    )
    default String incomingtextswaps()
    {
        return "dds:Dragon Dagger";
    }

    @ConfigItem(
            position = 6,
            keyName = "outgoingtextswaps",
            name = "Outgoing Text Swaps",
            description = "Add words to replace, 1 per line." +
                    "<br>Syntax: trigger word:swapped phrase" +
                    "<br>Examples:" +
                    "<br> fag:friend" +
                    "<br> retard:better luck next time",
            hidden = true,
            unhide = "toggleoutgoingfiltering"
    )
    default String outgoingtextswaps() {
        return "";
    }
}
