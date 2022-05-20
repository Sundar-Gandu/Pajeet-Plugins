package net.runelite.client.plugins.catfacts;

import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.api.Client;
import org.apache.commons.text.StringEscapeUtils;
import org.pf4j.Extension;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Extension
@PluginDescriptor(
      name = "Cat Facts",
      description = "Daily Cat Facts",
      tags = {"sundar", "pajeet"},
      enabledByDefault = false
)

@Slf4j
public class CatFactsPlugin extends Plugin
{
   @Inject
   private CatFactsConfig config;

   @Inject
   private Client client;

   @Inject
   private ConfigManager configManager;

   @Inject
   private ChatMessageManager chatMessageManager;

   @Provides
   CatFactsConfig provideConfig(ConfigManager configManager)
   {
      return configManager.getConfig(CatFactsConfig.class);
   }

   @Override
   protected void startUp()
   {
      if (client.getGameState() == GameState.LOGGED_IN)
         new Thread(this::printCatFact).start();
   }

   @Override
   protected void shutDown()
   {
   }

   @Subscribe
   private void onGameStateChanged(GameStateChanged event)
   {
      if (event.getGameState() == GameState.LOGGED_IN)
      {
         new Thread(this::printCatFact).start();
      }

   }

   @Subscribe
   private void onConfigButtonClicked(ConfigButtonClicked event)
   {
      if (event.getGroup().equals("catfacts"))
      {
         new Thread(this::printCatFact).start();
      }
   }


   public void printCatFact()
   {
      try
      {
         HttpRequest request = HttpRequest.newBuilder(new URL("https://catfact.ninja/fact").toURI())
               .header("accept", "application/json")
               .build();

         String test =  HttpClient.newHttpClient()
               .send(request, HttpResponse.BodyHandlers.ofString())
               .body();
         Map json = new Gson().fromJson(test, Map.class);
         String response = (String)json.get("fact");
         String factText = Normalizer.normalize(response, Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]+", "");
         chatMessageManager.queue(QueuedMessage.builder().type(ChatMessageType.CONSOLE).runeLiteFormattedMessage(
               new ChatMessageBuilder()
                     .append(config.color(), "Cat Fact: ")
                     .append(factText)
                     .build())
               .build());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
