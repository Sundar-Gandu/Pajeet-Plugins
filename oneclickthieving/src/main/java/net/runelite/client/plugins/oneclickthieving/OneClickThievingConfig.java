package net.runelite.client.plugins.oneclickthieving;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;
import static net.runelite.client.plugins.oneclickthieving.PrayMethod.LAZY_PRAY;

@ConfigGroup("oneclicjthieving")
public interface OneClickThievingConfig extends Config
{
    @ConfigItem(
            keyName = "clickOverride",
            name = "Clicks Override",
            description = "Makes all of your clicks pickpocket chosen NPC",
            position = -2
    )
    default boolean clickOverride()
    {
        return false;
    }
    @ConfigItem(
            keyName = "npcID",
            name = "NPC ID",
            description = "the id of the npc you want to pickpocket",
            position = -1,
            hidden = true,
            unhide = "clickOverride"
    )
    default int npcID()
    {
        return 0;
    }
    @ConfigItem(
            keyName = "enableCoinPouch",
            name = "Open Coin Pouch",
            description = "Opens the coinpouch when it reaches the maximum of 28",
            position = 0
    )
    default boolean enableCoinPouch()
    {
        return true;
    }

    @ConfigItem(
            keyName = "enableHeal",
            name = "Eat food",
            description = "This will eat/drink anything in your inventory when your HP gets low",
            position = 1
    )
    default boolean enableHeal()
    {
        return true;
    }

    @Range(
            max = 99,
            min = 5
    )
    @ConfigItem(
            keyName = "HPTopThreshold",
            name = "Maximum HP",
            description = "You will STOP eating when your HP is at or above this number",
            position = 2,
            hidden = true,
            unhide = "enableHeal"
    )
    default int HPTopThreshold()
    {
        return 80;
    }

    @Range(
            max = 99,
            min = 5
    )
    @ConfigItem(
            keyName = "HPBottomThreshold",
            name = "Minimum HP",
            description = "You will START eating when your HP is at or below this number",
            position = 3,
            hidden = true,
            unhide = "enableHeal"
    )
    default int HPBottomThreshold()
    {
        return 5;
    }

    @ConfigItem(
            keyName = "haltOnNoFood",
            name = "Stop on No Food/Prayer",
            description = "This will notify you and prevent you from picpocketing when you run out of food or prayer potions",
            position = 4,
            hidden = true,
            unhide = "enableHeal"
    )
    default boolean haltOnLowFood()
    {
        return true;
    }

    @ConfigItem(
            keyName = "enablePray",
            name = "Use Prayer",
            description = "This will use redemption with prayer/restore potions" +
                    "<br>This is mainly for pickpocketing elves/vyres",
            position = 5
    )
    default boolean enablePray()
    {
        return false;
    }

    @ConfigItem(
            keyName = "prayMethod",
            name = "Pray Method",
            description = "Lazy reactivates redemption when possible. Active only enables when your hp is low" +
                    "<br>Active should only be used if your max hp is greater than 60",
            position = 6,
            hidden = true,
            unhide = "enablePray"
    )
    default PrayMethod prayMethod()
    {
        return LAZY_PRAY;
    }

    @ConfigItem(
            keyName = "enableSpell",
            name = "Use Shadow Veil",
            description = "This will cast the arceuus spell Shadow Veil",
            position = 7
    )
    default boolean enableSpell()
    {
        return false;
    }

    @ConfigItem(
            keyName = "enableNecklace",
            name = "Dodgy Necklace",
            description = "This will put on dodgy necklaces when they break",
            position = 8
    )
    default boolean enableNecklace()
    {
        return true;
    }

    @ConfigItem(
            keyName = "disableWalk",
            name = "Disable Walk",
            description = "This will prevent you from misclicking Walk Here",
            position = 9
    )
    default boolean disableWalk()
    {
        return false;
    }
}
