package br.com.lipe.automessages.proxy.bungee;

import br.com.lipe.automessages.proxy.ProxyPlatform;
import br.com.lipe.automessages.proxy.ProxyTask;
import br.com.lipe.automessages.proxy.message.ProxyMessagePacket;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public final class BungeeProxyPlatform implements ProxyPlatform {

    private final Plugin plugin;

    public BungeeProxyPlatform(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getOnlinePlayerCount() {
        return plugin.getProxy().getOnlineCount();
    }

    @Override
    public void broadcastPluginMessage(byte[] data) {
        for (ServerInfo server : plugin.getProxy().getServers().values()) {
            if (!server.getPlayers().isEmpty()) {
                server.sendData(ProxyMessagePacket.CHANNEL, data, false);
            }
        }
    }

    @Override
    public ProxyTask scheduleRepeating(Runnable runnable, long intervalSeconds) {
        final ScheduledTask task = plugin.getProxy().getScheduler().schedule(
                plugin,
                runnable,
                intervalSeconds,
                intervalSeconds,
                TimeUnit.SECONDS
        );
        return new ProxyTask() {
            @Override
            public void cancel() {
                task.cancel();
            }
        };
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warning(String message) {
        plugin.getLogger().warning(message);
    }
}
