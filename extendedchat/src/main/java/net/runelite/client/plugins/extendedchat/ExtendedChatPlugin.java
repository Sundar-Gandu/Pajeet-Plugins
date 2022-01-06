package net.runelite.client.plugins.extendedchat;

import com.google.common.base.CharMatcher;
import com.google.inject.Provides;
import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.vars.InputType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.Text;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Extension
@PluginDescriptor(
      name = "Chat Extended",
      description = "Chat filtering, Ctrl+V pasting, and retype previous message",
      tags = {"sundar", "pajeet"},
      enabledByDefault = false
)

@Slf4j
public class ExtendedChatPlugin extends Plugin
{
   @Inject
   private Client client;

   @Inject
   private KeyManager keyManager;

   @Inject
   private ExtendedChatConfig config;

   @Inject
   private ConfigManager configManager;

   @Provides
   ExtendedChatConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(ExtendedChatConfig.class);
   }

   private boolean ctrlPressed = false;
   private String lastMessage;
   private final CharMatcher jagexPrintableCharMatcher = Text.JAGEX_PRINTABLE_CHAR_MATCHER;
   private HashMap<String,String> incomingWordFilter = new HashMap<>();
   private HashMap<String,String> outgoingWordFilter = new HashMap<>();

   private final KeyListener pasteKeyListener = new KeyListener()
   {
      @Override
      public void keyTyped(KeyEvent e)
      {

      }

      @Override
      public void keyPressed(KeyEvent e)
      {
         if (Keybind.CTRL.matches(e))
         {
            ctrlPressed = true;
         }
         else if (KeyEvent.VK_V == e.getKeyCode() && e.getModifiersEx()== InputEvent.CTRL_DOWN_MASK && isChatEnabled())
         {
            try
            {
               String chatBoxText = client.getVar(VarClientStr.CHATBOX_TYPED_TEXT);
               chatBoxText = chatBoxText + (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
               if (chatBoxText.length() > 80)
               {
                  chatBoxText = chatBoxText.substring(0, 80);
               }

               client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, chatBoxText);
            } catch (UnsupportedFlavorException | IOException | HeadlessException var3)
            {
            }
         }
      }

      @Override
      public void keyReleased(KeyEvent e)
      {
         if (Keybind.CTRL.matches(e))
         {
            ctrlPressed = false;
         }
      }
   };

   private final KeyListener outgoingChatKeyListener = new KeyListener()
   {
      @Override
      public void keyTyped(KeyEvent e)
      {

      }

      @Override
      public void keyPressed(KeyEvent e)
      {
         if (e.getKeyCode() == KeyEvent.VK_ENTER && isChatEnabled())
         {
            int inputType = client.getVar(VarClientInt.INPUT_TYPE);
            if (inputType == InputType.PRIVATE_MESSAGE.getType() || inputType == InputType.NONE.getType())
            {
               VarClientStr var;
               if (inputType == InputType.PRIVATE_MESSAGE.getType())
               {
                  var = VarClientStr.INPUT_TEXT;
               }
               else
               {
                  var = VarClientStr.CHATBOX_TYPED_TEXT;
               }
               String text = client.getVar(var);
               if (text == null || "".equals(text))
               {
                  return;
               }
               lastMessage = text;
               if (config.enableOutgoingFiltering())
               {
                  String cleanedText = censorMessage(text, outgoingWordFilter);
                  if (!cleanedText.equals(text))
                  {
                     client.setVar(var, cleanedText);
                  }
                  log.debug("text:{}, censored:{}", text, cleanedText);
               }
            }
         }
      }

      @Override
      public void keyReleased(KeyEvent e)
      {

      }
   };

   private final KeyListener previousKeyListener = new KeyListener()
   {
      @Override
      public void keyTyped(KeyEvent e)
      {

      }

      @Override
      public void keyPressed(KeyEvent e)
      {
         if (lastMessage == null)
         {
            return;
         }

         if (config.previouskeybind().matches(e) && isChatEnabled())
         {
            client.setVar(VarClientStr.CHATBOX_TYPED_TEXT, lastMessage);
         }
      }

      @Override
      public void keyReleased(KeyEvent e)
      {

      }
   };

   @Override
   protected void startUp()
   {
      toggleKeyListener(config.enablePasting(),pasteKeyListener);
      toggleKeyListener(config.enableOutgoingFiltering() || config.enablePrevious(),outgoingChatKeyListener);
      toggleKeyListener(config.enablePrevious(),previousKeyListener);
      incomingWordFilter = parseConfig(config.incomingtextswaps());
      outgoingWordFilter = parseConfig(config.outgoingtextswaps());
      client.refreshChat();
   }

   @Override
   protected void shutDown()
   {
      toggleKeyListener(false,pasteKeyListener);
      toggleKeyListener(false,outgoingChatKeyListener);
      client.refreshChat();
   }

   @Subscribe
   public void onConfigChanged(ConfigChanged event)
   {
      if (event.getGroup().equals("extendedchat"))
      {
         switch (event.getKey())
         {
            case "incomingtextswaps":
               incomingWordFilter = parseConfig(config.incomingtextswaps());
               break;
            case "outgoingtextswaps":
               outgoingWordFilter = parseConfig(config.outgoingtextswaps());
               break;
            case "togglepasting":
               toggleKeyListener(config.enablePasting(),pasteKeyListener);
               break;
            case "toggleoutgoingfiltering":
               toggleKeyListener(config.enableOutgoingFiltering() || config.enablePrevious(),outgoingChatKeyListener);
               break;
            case "toggleprevious":
               toggleKeyListener(config.enablePrevious(),previousKeyListener);
               break;
            default:
               break;
         }
      }

   }

   @Subscribe
   public void onChatMessage(ChatMessage chatMessage)
   {
      if(!config.enableIncomingFiltering())
      {
         return;
      }

      switch (chatMessage.getType())
      {
         case PUBLICCHAT:
         case MODCHAT:
         case FRIENDSCHAT:
         case CLAN_CHAT:
         case CLAN_GUEST_CHAT:
         case PRIVATECHAT:
         case PRIVATECHATOUT:
         case MODPRIVATECHAT:
            break;
         default:
            return;
      }

      final MessageNode messageNode = chatMessage.getMessageNode();
      String output = censorMessage(messageNode.getValue(),incomingWordFilter);

      if (!output.equals(messageNode.getValue()))
      {
         messageNode.setValue(output);
      }
   }

   @Subscribe
   public void onOverheadTextChanged(final OverheadTextChanged event)
   {
      if(!config.enableIncomingFiltering())
      {
         return;
      }

      if (!(event.getActor() instanceof Player))
      {
         return;
      }

      final String output = censorMessage(event.getOverheadText(), incomingWordFilter);

      if (output != null)
      {
         event.getActor().setOverheadText(output);
      }
   }

   private void toggleKeyListener(Boolean enabled,KeyListener keylistener)
   {
      ctrlPressed = false;
      if (enabled)
      {
         keyManager.registerKeyListener(keylistener);
      }
      else
      {
         keyManager.unregisterKeyListener(keylistener);
      }
   }

   //checks if chat is disabled by the runelite key remaping plugin
   private boolean isChatEnabled()
   {
      Widget chatboxInput = client.getWidget(WidgetInfo.CHATBOX_INPUT);
      if (chatboxInput != null && client.getGameState() == GameState.LOGGED_IN)
      {
         String text = chatboxInput.getText();
         int idx = text.indexOf(':');
         if (idx != -1)
         {
            return !text.substring(idx + 2).equals("Press Enter to Chat...");
         }
      }
      //default, should probably throw an error or something
      return false;
   }

   private HashMap<String,String> parseConfig(String input)
   {
      HashMap<String,String> newFilterMap = new HashMap<>();
      String[] filterArguments = input.split("\\r?\\n");//split on newline

      for (String filterArg : filterArguments)
      {
         //ignores empty inputs, comments that start with //, and inputs that dont follow the format of thing:thing
         if (filterArg.matches("^\\s*$") || filterArg.startsWith("//") || !filterArg.contains(":"))
         {
            continue;
         }
         String[] pair = filterArg.split(":", 2);
         String key = pair[0].trim().toLowerCase();
         String value = pair[1].trim();

         //ensures key is one word
         if (key.contains(" "))
         {
            key = key.substring(key.lastIndexOf(" ") + 1);
         }

         if (!key.isBlank() && !value.isBlank())
         {
            newFilterMap.put(key, value);
         }
      }

      return newFilterMap;

   }

   private String censorMessage(String message, Map<String,String> wordFilter)
   {
      List<String> words = Arrays.asList(jagexPrintableCharMatcher
              .retainFrom(message)
              .replace(' ', ' ')
              .split(" "));

      boolean filtered = false;

      ListIterator<String> iterator = words.listIterator();
      while(iterator.hasNext())
      {
         String word = iterator.next().toLowerCase();
         if(wordFilter.containsKey(word))
         {
            iterator.set(wordFilter.get(word));
            filtered = true;
         }
      }

      String output = Strings.join(words, " ");
      if (output.length()>80)
      {
         output = output.substring(0, 80);
      }

      output = output.substring(0, 1).toUpperCase() + output.substring(1);
      return filtered ? output : message;
   }
}
