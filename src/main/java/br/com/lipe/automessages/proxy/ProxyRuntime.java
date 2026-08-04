package br.com.lipe.automessages.proxy;

import br.com.lipe.automessages.proxy.config.ProxyConfigManager;
import br.com.lipe.automessages.proxy.message.ProxyMessageManager;
import br.com.lipe.automessages.proxy.message.ProxyMessagePacket;

import java.nio.file.Path;
import java.util.List;

public final class ProxyRuntime {

    private final ProxyPlatform platform;
    private final ProxyConfigManager configManager;
    private ProxyMessageManager messageManager;
    private ProxyTask scheduledTask;

    public ProxyRuntime(ProxyPlatform platform, Path dataDirectory, ClassLoader resourceLoader) {
        this.platform = platform;
        configManager = new ProxyConfigManager(dataDirectory, resourceLoader, platform);
    }

    public void start() {
        configManager.initialize();
        messageManager = new ProxyMessageManager(configManager);
        restartTask();
    }

    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    private void restartTask() {
        stop();
        if (!configManager.isProxyEnabled()) {
            platform.info("Modo proxy do AutoMessages está desativado no config.yml.");
            return;
        }
        if (!configManager.isSystemEnabled()) {
            platform.info("Mensagens automáticas estão desativadas no config.yml.");
            return;
        }

        messageManager.resetSequence();
        scheduledTask = platform.scheduleRepeating(new Runnable() {
            @Override
            public void run() {
                broadcastNextMessage();
            }
        }, configManager.getIntervalSeconds());
        platform.info("Modo proxy do AutoMessages foi ativado.");
    }

    private void broadcastNextMessage() {
        if (configManager.shouldSkipWhenEmpty() && platform.getOnlinePlayerCount() == 0) {
            return;
        }

        List<String> lines = messageManager.getNextMessageLines();
        if (lines.isEmpty()) {
            return;
        }

        byte[] data = ProxyMessagePacket.encode(lines);
        if (data.length == 0) {
            platform.warning("Uma mensagem excedeu o limite do canal e não foi enviada.");
            return;
        }
        platform.broadcastPluginMessage(data);
    }
}
