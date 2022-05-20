package net.runelite.client.plugins.catfacts;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("catfacts")
public interface CatFactsConfig extends Config
{
   @ConfigItem(
         position = 0,
         keyName = "catFact",
         name = "New Cat Fact",
         description = "Click for cat facts"
   )
   default Button catFact()
   {
      return new Button();
   }

    @ConfigItem(
		    position = 1,
		    keyName = "color",
		    name = "Chat Color",
		    description = ""
    )
    default Color color()
    {
        return Color.BLUE;
    }

}
