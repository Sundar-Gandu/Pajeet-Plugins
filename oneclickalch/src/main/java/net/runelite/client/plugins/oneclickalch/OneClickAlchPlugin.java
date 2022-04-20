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

      itemExists = getWidgetItem(config.itemID()) != null;
   }

   @Subscribe
   private void onClientTick(ClientTick event)
   {
      if(client.getLocalPlayer() == null || client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
         return;

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

      Widget item = getWidgetItem(config.itemID());

      if (item == null)
         return;

      setSelectSpell(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY);

      event.setMenuOption("Cast");
      event.setMenuTarget("High Level Alchemy -> Item");
      event.setId(item.getId());
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
   }

   public Widget getWidgetItem(int id) {
      Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
      if (inventoryWidget != null && inventoryWidget.getChildren() != null) {
         Widget[] items = inventoryWidget.getChildren();
         for (Widget item : items) {
            if (id == item.getId()) {
               return item;
            }
         }
      }
      return null;
   }
}
