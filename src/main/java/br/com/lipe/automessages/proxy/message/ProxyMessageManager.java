package br.com.lipe.automessages.proxy.message;

import br.com.lipe.automessages.proxy.config.ProxyConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ProxyMessageManager {

    private final ProxyConfigManager configManager;
    private final Random random = new Random();
    private int sequenceIndex;

    public ProxyMessageManager(ProxyConfigManager configManager) {
        this.configManager = configManager;
    }

    public List<String> getNextMessageLines() {
        List<String> availableMessages = getAvailableMessages();
        if (availableMessages.isEmpty()) {
            return Collections.emptyList();
        }

        String id = selectNextMessage(availableMessages);
        List<String> prefixedLines = new ArrayList<String>();
        for (String line : configManager.getMessageLines(id)) {
            prefixedLines.add(configManager.getPrefix() + line);
        }
        return prefixedLines;
    }

    public void resetSequence() {
        sequenceIndex = 0;
    }

    private List<String> getAvailableMessages() {
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
}
