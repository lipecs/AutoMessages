package br.com.lipe.automessages.proxy.message;

import br.com.lipe.automessages.message.BroadcastMessage;
import br.com.lipe.automessages.message.BroadcastType;
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

    public BroadcastMessage getNextMessage() {
        List<BroadcastMessage> availableMessages = getAvailableMessages();
        if (availableMessages.isEmpty()) {
            return null;
        }

        BroadcastMessage selectedMessage;
        if (configManager.isRandomOrder()) {
            selectedMessage = availableMessages.get(random.nextInt(availableMessages.size()));
        } else {
            if (sequenceIndex >= availableMessages.size()) {
                sequenceIndex = 0;
            }
            selectedMessage = availableMessages.get(sequenceIndex);
            sequenceIndex = (sequenceIndex + 1) % availableMessages.size();
        }
        return selectedMessage.withTextPrefix(configManager.getPrefix());
    }

    public void resetSequence() {
        sequenceIndex = 0;
    }

    private List<BroadcastMessage> getAvailableMessages() {
        List<BroadcastMessage> availableMessages = new ArrayList<BroadcastMessage>();
        for (String id : configManager.getMessageIds()) {
            BroadcastMessage message = configManager.getBroadcastMessage(id);
            if (message != null && message.isEnabled() && isSendable(message)) {
                availableMessages.add(message);
            }
        }
        return availableMessages.isEmpty() ? Collections.<BroadcastMessage>emptyList() : availableMessages;
    }

    private boolean isSendable(BroadcastMessage message) {
        if (message.getType() == BroadcastType.TITLE) {
            return !message.getTitle().isEmpty() || !message.getSubtitle().isEmpty();
        }
        return !message.getText().isEmpty();
    }
}
