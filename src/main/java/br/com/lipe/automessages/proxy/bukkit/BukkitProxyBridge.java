package br.com.lipe.automessages.proxy.bukkit;

import br.com.lipe.automessages.config.ConfigManager;
import br.com.lipe.automessages.message.MessageManager;
import br.com.lipe.automessages.proxy.message.ProxyMessagePacket;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.List;

public final class BukkitProxyBridge implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private boolean registered;

    public BukkitProxyBridge(JavaPlugin plugin, ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    public void restart() {
        stop();
        if (!configManager.isProxyEnabled()) {
            return;
        }

        plugin.getServer().getMessenger().registerIncomingPluginChannel(
                plugin,
                ProxyMessagePacket.CHANNEL,
                this
        );
        registered = true;
    }

    public void stop() {
        if (!registered) {
            return;
        }
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(
                plugin,
                ProxyMessagePacket.CHANNEL,
                this
        );
        registered = false;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        if (!configManager.isProxyEnabled()
                || !configManager.isSystemEnabled()
                || !ProxyMessagePacket.CHANNEL.equals(channel)) {
            return;
        }

        List<String> lines = ProxyMessagePacket.decode(data);
        if (!lines.isEmpty()) {
            messageManager.broadcastLines(lines);
        }
    }
}
