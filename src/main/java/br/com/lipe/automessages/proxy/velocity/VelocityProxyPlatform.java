package br.com.lipe.automessages.proxy.velocity;

import br.com.lipe.automessages.proxy.ProxyPlatform;
import br.com.lipe.automessages.proxy.ProxyTask;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public final class VelocityProxyPlatform implements ProxyPlatform {

    private final Object plugin;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final ChannelIdentifier channelIdentifier;

    public VelocityProxyPlatform(
            Object plugin,
            ProxyServer proxyServer,
            Logger logger,
            ChannelIdentifier channelIdentifier
    ) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.channelIdentifier = channelIdentifier;
    }

    @Override
    public int getOnlinePlayerCount() {
        return proxyServer.getPlayerCount();
    }

    @Override
    public void broadcastPluginMessage(byte[] data) {
        for (RegisteredServer server : proxyServer.getAllServers()) {
            if (!server.getPlayersConnected().isEmpty()) {
                server.sendPluginMessage(channelIdentifier, data);
            }
        }
    }

    @Override
    public ProxyTask schedule(Runnable runnable, long delaySeconds) {
        final ScheduledTask task = proxyServer.getScheduler()
                .buildTask(plugin, runnable)
                .delay(delaySeconds, TimeUnit.SECONDS)
                .schedule();
        return new ProxyTask() {
            @Override
            public void cancel() {
                task.cancel();
            }
        };
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
    }
}
