package net.runelite.client.plugins.soundfilter;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
@PluginDescriptor(
      name = "Sound Filter",
      description = "Filter ingame sounds using a white/blacklist",
      tags = {"sundar", "pajeet","sound"},
      enabledByDefault = false
)

@Slf4j
public class soundfilterPlugin extends Plugin
{
   @Inject
   private soundfilterConfig config;

   @Inject
   private soundfilterEffectOverlay effectOverlay;

   @Inject
   private soundfilterAreaOverlay areaOverlay;

   @Inject
   private ConfigManager configManager;

   @Inject
   private OverlayManager overlayManager;

   @Provides
   soundfilterConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(soundfilterConfig.class);
   }

   Set<Integer> whitelistedEffectSound = new HashSet<>();
   Set<Integer> whitelistedAreaSound = new HashSet<>();
   Set<Integer> blacklistedEffectSound = new HashSet<>();
   Set<Integer> blacklistedAreaSound = new HashSet<>();
   LinkedList<Integer> effectSoundsList = new LinkedList<>();
   LinkedList<Integer> areaSoundsList = new LinkedList<>();


   @Override
   protected void startUp()
   {
      enableOverlay(config.soundDebug());
      updateEffectFilter();
      updateAreaFilter();
   }

   @Override
   protected void shutDown()
   {
      enableOverlay(false);
   }

   @Subscribe
   private void onConfigChanged(ConfigChanged event)
   {
      if(event.getGroup().equals("soundfilter"))
      {
         switch (event.getKey())
         {
            case "soundDebug":
               enableOverlay(config.soundDebug());
               break;
            case "effectwhitelist":
            case "effectblacklist":
               updateEffectFilter();
               break;
            case "areablacklist":
            case "areawhitelist":
               updateAreaFilter();
         }
      }
   }


   @Subscribe
   public void onSoundEffectPlayed(SoundEffectPlayed soundEffectPlayed)
   {
      switch (config.filterMode())
      {
         case DISABLED:
            break;
         case WHITELIST:
            if (whitelistedEffectSound.isEmpty())
            {
               soundEffectPlayed.consume();
               break;
            }
            if (!whitelistedEffectSound.contains(soundEffectPlayed.getSoundId())) soundEffectPlayed.consume();
            break;
         case BLACKLIST:
            if(blacklistedEffectSound.isEmpty())
            {
               break;
            }
            if(blacklistedEffectSound.contains(soundEffectPlayed.getSoundId())) soundEffectPlayed.consume();
            break;
      }
      if (!soundEffectPlayed.isConsumed())
      {
      addEffectSound(soundEffectPlayed.getSoundId());
      }
   }

   @Subscribe
   public void onAeraSoundEffectPlayed(AreaSoundEffectPlayed areaSoundEffectPlayed)
   {
      switch (config.filterMode())
      {
         case DISABLED:
            break;
         case WHITELIST:
            if (whitelistedAreaSound.isEmpty())
            {
               areaSoundEffectPlayed.consume();
               break;
            }
            if (!whitelistedAreaSound.contains(areaSoundEffectPlayed.getSoundId())) areaSoundEffectPlayed.consume();
            break;
         case BLACKLIST:
            if(blacklistedAreaSound.isEmpty())
            {
               break;
            }
            if(blacklistedAreaSound.contains(areaSoundEffectPlayed.getSoundId())) areaSoundEffectPlayed.consume();
            break;
      }
      if(!areaSoundEffectPlayed.isConsumed())
      {
         addAreaSound(areaSoundEffectPlayed.getSoundId());
      }
   }

   private void updateEffectFilter()
   {
         whitelistedEffectSound = getNumbersFromConfig(config.effectwhitelist());
         blacklistedEffectSound = getNumbersFromConfig(config.effectblacklist());
   }

   private void updateAreaFilter()
   {
         whitelistedAreaSound = getNumbersFromConfig(config.areawhitelist());
         blacklistedAreaSound = getNumbersFromConfig(config.areablacklist());
   }

   private Set<Integer> getNumbersFromConfig(String source)
   {
      return Arrays.stream(source.split(","))
              .map(String::trim)
              .filter(NumberUtils::isParsable)
              .map(Integer::parseInt)
              .collect(Collectors.toSet());
   }

   public void enableOverlay(boolean bool)
   {
      if (bool)
      {
         overlayManager.add(effectOverlay);
         overlayManager.add(areaOverlay);
      }
      else
      {
         overlayManager.remove(effectOverlay);
         overlayManager.remove(areaOverlay);
         effectSoundsList.clear();
         areaSoundsList.clear();
      }

   }
   
   private void addEffectSound(Integer sound)
   {
      effectSoundsList.addFirst(sound);
      if (effectSoundsList.size()>7)
         effectSoundsList.removeLast();
   }

   private void addAreaSound(Integer sound)
   {
      areaSoundsList.addFirst(sound);
      if (areaSoundsList.size()>7)
         areaSoundsList.removeLast();
   }
}
