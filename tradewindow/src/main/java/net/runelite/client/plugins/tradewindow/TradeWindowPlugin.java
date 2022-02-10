package net.runelite.client.plugins.tradewindow;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import static net.runelite.api.widgets.WidgetInfo.SECOND_TRADING_WITH_ACCEPT_BUTTON;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import javax.inject.Inject;

@Extension
@PluginDescriptor(
      name = "Easy Trade",
      description = "Resizes accept button in trade window",
      tags = {"sundar", "pajeet"},
      enabledByDefault = false
)

@Slf4j
public class TradeWindowPlugin extends Plugin
{
   @Inject
   private Client client;

   @Inject
   private ClientThread clientThread;

   @Override
   protected void startUp()
   {
   }

   @Override
   protected void shutDown()
   {
   }

   @Subscribe
   private void onConfigChanged(ConfigChanged event)
   {
      resizeButton1();
      resizeButton2();
   }

   @Subscribe
   private void onScriptPostFired(ScriptPostFired event) throws Exception
   {
      resizeButton1();
      resizeButton2();
   }

   public void resizeButton1()
   {
      //first accept button
      Widget widget = client.getWidget(335,10);
      if(widget == null)
         return;
      //this might break stuff, but is required to prioritize accept over other menu options
      widget.setParentId(21954562);

      widget.setOriginalX(0).setOriginalY(0).setOriginalWidth(495).setOriginalHeight(305);
      clientThread.invoke(widget::revalidate);

      for(Widget child:widget.getDynamicChildren())
      {
         child.setHidden(true);
         clientThread.invoke(child::revalidate);
      }

      for(Widget child:widget.getStaticChildren())
      {
         if(child.getId() == 21954571)
         {
            child.setFilled(false);
         }
         else if(child.getId() == 21954572)
         {
            child.setText("Accept Anywhere");
         }
         clientThread.invoke(child::revalidate);
      }
   }

   private void resizeButton2()
   {
      Widget widget = client.getWidget(SECOND_TRADING_WITH_ACCEPT_BUTTON);
      if(widget == null)
         return;
      widget.setOriginalX(0);
      widget.setOriginalY(0);
      widget.setOriginalWidth(488);
      widget.setOriginalHeight(305);
      widget.setFilled(false);
      clientThread.invoke(widget::revalidate);
   }

}
