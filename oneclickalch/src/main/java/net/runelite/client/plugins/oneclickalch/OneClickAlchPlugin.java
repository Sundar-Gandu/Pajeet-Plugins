package net.runelite.client.plugins.oneclickalch;

import com.google.inject.Provides;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.Client;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Extension
@PluginDescriptor(
      name = "One Click Alch",
      description = "click anywhere to alch",
      tags = {"sundar", "pajeet"},
      enabledByDefault = false
)
public class OneClickAlchPlugin extends Plugin
{
   @Inject
   private OneClickAlchConfig config;

   @Inject
   private Client client;

   @Inject
   private ConfigManager configManager;

   @Provides
   OneClickAlchConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(OneClickAlchConfig.class);
   }

   @Override
   protected void startUp()
   {
   }

   @Override
   protected void shutDown()
   {
   }

   int cooldown = 0;
   boolean itemExists = false;

   @Subscribe
   private void onGameTick(GameTick event)
   {
      if (cooldown > 0)
      {
         cooldown--;
      }

      itemExists = getItem(List.of(config.itemID())) != null;
   }

   @Subscribe
   private void onClientTick(ClientTick event)
   {
      if(client.getLocalPlayer() == null
            || client.getGameState() != GameState.LOGGED_IN
            || client.isMenuOpen()
            || client.getWidget(378,78) != null)//login button
      {
         return;
      }

      if (cooldown == 0
              && client.getVarbitValue(4070) == 0 //spellbook varbit
              && itemExists
              && client.getLocalPlayer().getGraphic() != 113 //high alch graphic
              && client.getLocalPlayer().getAnimation() == -1)
         client.insertMenuItem("One Click Alch","",MenuAction.UNKNOWN.getId(),0,0,0,true);
   }

   @Subscribe
   private void onMenuOptionClicked(MenuOptionClicked event)
   {
      if(!event.getMenuOption().equals("One Click Alch"))
         return;

      Widget item = getItem(List.of(config.itemID()));

      if (item == null)
         return;

      setSelectSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY);

      event.setMenuOption("Cast");
      event.setMenuTarget("High Level Alchemy -> Item");
      event.setId(0);
      event.setMenuAction(MenuAction.WIDGET_TARGET_ON_WIDGET);
      event.setParam0(item.getIndex());
      event.setParam1(WidgetInfo.INVENTORY.getId());
      cooldown = 5;
   }

   private void setSelectSpell(WidgetInfo widget)
   {
      final Widget spell = client.getWidget(widget);
      client.setSelectedSpellName("<col=00ff00>" + spell.getName() + "</col>");
      client.setSelectedSpellWidget(spell.getId());
      client.setSelectedSpellChildIndex(-1);
      client.setSelectedSpellItemId(-1);
   }

   public Widget getItem(Collection<Integer> ids) {
      List<Widget> matches = getItems(ids);
      return matches.size() != 0 ? matches.get(0) : null;
   }

   public ArrayList<Widget> getItems(Collection<Integer> ids)
   {
      client.runScript(6009, 9764864, 28, 1, -1);
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      ArrayList<Widget> matchedItems = new ArrayList<>();

      if (inventoryWidget != null)
      {
         Widget[] items = inventoryWidget.getDynamicChildren();
         if (items == null) return matchedItems;
         for(Widget item : items)
         {
            if (ids.contains(item.getItemId()))
            {
               matchedItems.add(item);
            }
         }
      }
      return matchedItems;
   }
}
