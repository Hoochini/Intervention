package com.example;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Slf4j
@PluginDescriptor(
        name = "Intervention",
        description = "An automated guilt trip for your gaming addiction.",
        tags = {"timer", "humor", "sanity"}
)
public class RealityCheckPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ChatMessageManager chatMessageManager;

    private Instant loginTime;
    private int lastCheckedHour = 0;
    
    private int overheadTicks = 0;
    private String currentOverhead = null;
    private final Random random = new Random();

    private final String[] pcDisses = {
            "Is your posture looking like a cooked shrimp right now?",
            "Your electric bill is crying.",
            "Your gaming chair just filed a formal complaint.",
            "Reminder: Taking a real-life shower is not considered XP waste.",
            "Reminder: The sun still exists.",
            "Go outside. The graphics are amazing and the FPS is flawless."
    };

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN && loginTime == null)
        {
            loginTime = Instant.now();
            lastCheckedHour = 0;
        }
        else if (event.getGameState() == GameState.LOGIN_SCREEN)
        {
            loginTime = null;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (overheadTicks > 0 && currentOverhead != null)
        {
            client.getLocalPlayer().setOverheadText(currentOverhead);
            overheadTicks--;
        }
        else if (overheadTicks == 0 && currentOverhead != null)
        {
            client.getLocalPlayer().setOverheadText("");
            currentOverhead = null;
        }

        if (loginTime != null)
        {
            long hoursPlayed = Duration.between(loginTime, Instant.now()).toHours();

            if (hoursPlayed > lastCheckedHour)
            {
                lastCheckedHour = (int) hoursPlayed;
                triggerRealityCheck(lastCheckedHour);
            }
        }
    }

    private void triggerRealityCheck(int hour)
    {
        String overheadMessage = getOverheadForHour(hour);
        currentOverhead = overheadMessage;
        overheadTicks = 10;

        String diss = pcDisses[random.nextInt(pcDisses.length)];
        sendLocalMessage("Reality Check: You've been playing for " + hour + " hours. " + diss);
    }

    private void sendLocalMessage(String text)
    {
        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(new ChatMessageBuilder()
                        .append(ChatColorType.HIGHLIGHT)
                        .append(text)
                        .build())
                .build());
    }

    private String getOverheadForHour(int hour)
    {
        switch (hour)
        {
            case 1: return "I need a life";
            case 2: return "My chair is the bathroom";
            case 3: return "I need to touch grass";
            case 4: return "I have nothing better to do with my life";
            case 5: return "I'll nerd log in 1h then cry for 15min and log back in to repeat the torture";
            default: return "Still here... seek help.";
        }
    }
}
