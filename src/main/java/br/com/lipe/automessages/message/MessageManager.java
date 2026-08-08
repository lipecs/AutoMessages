package br.com.lipe.automessages.message;

import br.com.lipe.automessages.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MessageManager {

    public enum SendResult {
        SENT,
        UNKNOWN_MESSAGE,
        EMPTY_MESSAGE
    }

    private final ConfigManager configManager;
    private final AdventureMessageRenderer renderer;
    private final Random random;
    private int sequenceIndex;
    private long lastIntervalSeconds;

    public MessageManager(JavaPlugin plugin, ConfigManager configManager) {
        this.configManager = configManager;
        renderer = new AdventureMessageRenderer(plugin);
        random = new Random();
        lastIntervalSeconds = configManager.getIntervalSeconds();
    }

    public boolean broadcastNextMessage() {
        BroadcastMessage message = selectNextMessage();
        if (message == null) {
            lastIntervalSeconds = configManager.getIntervalSeconds();
            return false;
        }
        lastIntervalSeconds = message.getIntervalSeconds() > 0L
                ? message.getIntervalSeconds()
                : configManager.getIntervalSeconds();
        broadcast(message.withTextPrefix(configManager.getPrefix()));
        return true;
    }

    public SendResult broadcastMessage(String id) {
        BroadcastMessage message = configManager.getBroadcastMessage(id);
        if (message == null) {
            return SendResult.UNKNOWN_MESSAGE;
        }
        if (!isSendable(message)) {
            return SendResult.EMPTY_MESSAGE;
        }
        broadcast(message.withTextPrefix(configManager.getPrefix()));
        return SendResult.SENT;
    }

    public void broadcastRemote(BroadcastMessage message) {
        if (message != null && isSendable(message)) {
            broadcast(message);
        }
    }

    public void broadcastLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        broadcastRemote(BroadcastMessage.builder("remote")
                .type(BroadcastType.CHAT)
                .format(MessageFormat.LEGACY)
                .text(lines)
                .build());
    }

    public void resetSequence() {
        sequenceIndex = 0;
        lastIntervalSeconds = configManager.getIntervalSeconds();
    }

    public long getLastIntervalSeconds() {
        return lastIntervalSeconds;
    }

    public void close() {
        renderer.close();
    }

    private void broadcast(BroadcastMessage message) {
        if (message.getType() == BroadcastType.BOSS_BAR) {
            renderer.sendBossBarToAll(message);
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("automessages.bypass")) {
                renderer.send(player, message);
            }
        }
    }

    private BroadcastMessage selectNextMessage() {
        List<BroadcastMessage> availableMessages = getAvailableMessages();
        if (availableMessages.isEmpty()) {
            return null;
        }
        if (configManager.isRandomOrder()) {
            return availableMessages.get(random.nextInt(availableMessages.size()));
        }
        if (sequenceIndex >= availableMessages.size()) {
            sequenceIndex = 0;
        }
        BroadcastMessage message = availableMessages.get(sequenceIndex);
        sequenceIndex = (sequenceIndex + 1) % availableMessages.size();
        return message;
    }

    private List<BroadcastMessage> getAvailableMessages() {
        List<BroadcastMessage> availableMessages = new ArrayList<BroadcastMessage>();
        for (String id : configManager.getMessageIds()) {
            BroadcastMessage message = configManager.getBroadcastMessage(id);
            if (message != null && message.isEnabled() && isSendable(message)) {
                availableMessages.add(message);
            }
        }
        return availableMessages;
    }

    private boolean isSendable(BroadcastMessage message) {
        switch (message.getType()) {
            case TITLE:
                return !message.getTitle().isEmpty() || !message.getSubtitle().isEmpty();
            case CHAT:
            case ACTION_BAR:
            case BOSS_BAR:
            default:
                return !message.getText().isEmpty();
        }
    }
}
