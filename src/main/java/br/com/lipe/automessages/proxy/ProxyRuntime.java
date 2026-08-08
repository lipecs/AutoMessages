package br.com.lipe.automessages.proxy;

import br.com.lipe.automessages.message.BroadcastMessage;
import br.com.lipe.automessages.proxy.config.ProxyConfigManager;
import br.com.lipe.automessages.proxy.message.ProxyMessageManager;
import br.com.lipe.automessages.proxy.message.ProxyMessagePacket;

import java.nio.file.Path;

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
        scheduleNext(configManager.getIntervalSeconds());
        platform.info("Modo proxy do AutoMessages foi ativado.");
    }

    private void scheduleNext(long delaySeconds) {
        stop();
        if (!configManager.isProxyEnabled() || !configManager.isSystemEnabled()) {
            return;
        }
        scheduledTask = platform.schedule(new Runnable() {
            @Override
            public void run() {
                broadcastNextMessage();
            }
        }, delaySeconds);
    }

    private void broadcastNextMessage() {
        if (configManager.shouldSkipWhenEmpty() && platform.getOnlinePlayerCount() == 0) {
            scheduleNext(configManager.getIntervalSeconds());
            return;
        }

        BroadcastMessage message = messageManager.getNextMessage();
        if (message == null) {
            scheduleNext(configManager.getIntervalSeconds());
            return;
        }

        byte[] data = ProxyMessagePacket.encode(message);
        if (data.length == 0) {
            platform.warning("Uma mensagem excedeu o limite do canal e não foi enviada.");
            scheduleNext(configManager.getIntervalSeconds());
            return;
        }
        platform.broadcastPluginMessage(data);
        long nextInterval = message.getIntervalSeconds() > 0L
                ? message.getIntervalSeconds()
                : configManager.getIntervalSeconds();
        scheduleNext(nextInterval);
    }
}
