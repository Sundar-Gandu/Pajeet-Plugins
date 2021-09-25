/*
 * Copyright (c) 2021, Nicole <github.com/losingticks>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.reflection;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("reflection")
public interface ReflectionConfig extends Config {
    String GROUP_NAME = "reflection";

    @ConfigItem(
            name = "Change Icon",
            keyName = "changeIcon",
            description = "Change the client icon. By default it will be runelite" +
                    "<br>If you want a custom icon, put a picture named icon.png in your .openosrs folder",
            position = -1
    )
    default boolean changeIcon() {
        return true;
    }

    @ConfigItem(
            name = "Client Title",
            keyName = "clientTitle",
            description = "",
            position = 0
    )
    default String getClientTitle() {
        return "RuneLite";
    }

    @ConfigItem(
            name = "Discord App ID",
            keyName = "discordAppId",
            description = "",
            position = 1
    )
    default String getDiscordAppId() {
        return "409416265891971072";
    }

    @ConfigItem(
            name = "Plugin Title Color",
            keyName = "pluginTitleColor",
            description = "",
            position = 2
    )
    default Color pluginTitleColor() {
        return Color.WHITE;
    }

    @ConfigItem(
            name = "Plugin Toggled Color",
            keyName = "pluginSwitcherOnColor",
            description = "",
            position = 3
    )
    Color pluginSwitcherOnColor();

    @ConfigItem(
            name = "Plugin Favorited Color",
            keyName = "pluginStarOnColor",
            description = "",
            position = 4
    )
    Color pluginStarOnColor();
}
