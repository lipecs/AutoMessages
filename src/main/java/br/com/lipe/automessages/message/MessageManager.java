package br.com.lipe.automessages.message;

import br.com.lipe.automessages.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
    private final Random random;
    private int sequenceIndex;

    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
        random = new Random();
    }

    public boolean broadcastNextMessage() {
        List<String> availableMessages = getAvailableAutomaticMessages();
        if (availableMessages.isEmpty()) {
            return false;
        }

        String id = selectNextMessage(availableMessages);
        return broadcastMessage(id) == SendResult.SENT;
    }

    public SendResult broadcastMessage(String id) {
        String configuredId = configManager.findMessageId(id);
        if (configuredId == null) {
            return SendResult.UNKNOWN_MESSAGE;
        }

        List<String> lines = configManager.getMessageLines(configuredId);
        if (lines.isEmpty()) {
            return SendResult.EMPTY_MESSAGE;
        }

        List<String> prefixedLines = new ArrayList<String>();
        for (String line : lines) {
            prefixedLines.add(configManager.getPrefix() + line);
        }
        broadcastLines(prefixedLines);
        return SendResult.SENT;
    }

    public void broadcastLines(List<String> lines) {
        for (String line : lines) {
            broadcastLine(line);
        }
    }

    public void resetSequence() {
        sequenceIndex = 0;
    }

    private List<String> getAvailableAutomaticMessages() {
        List<String> availableMessages = new ArrayList<String>();
        for (String id : configManager.getMessageIds()) {
            if (configManager.isMessageEnabled(id) && !configManager.getMessageLines(id).isEmpty()) {
                availableMessages.add(id);
            }
        }
        return availableMessages;
    }

    private String selectNextMessage(List<String> availableMessages) {
        if (configManager.isRandomOrder()) {
            return availableMessages.get(random.nextInt(availableMessages.size()));
        }

        if (sequenceIndex >= availableMessages.size()) {
            sequenceIndex = 0;
        }
        String selectedMessage = availableMessages.get(sequenceIndex);
        sequenceIndex = (sequenceIndex + 1) % availableMessages.size();
        return selectedMessage;
    }

    private void broadcastLine(String line) {
        String coloredLine = ChatColor.translateAlternateColorCodes('&', line);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission("automessages.bypass")) {
                player.sendMessage(coloredLine);
            }
        }
    }
}
