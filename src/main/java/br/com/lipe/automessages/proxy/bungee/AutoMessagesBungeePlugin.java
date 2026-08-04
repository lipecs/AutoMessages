package br.com.lipe.automessages.proxy.bungee;

import br.com.lipe.automessages.proxy.ProxyRuntime;
import br.com.lipe.automessages.proxy.message.ProxyMessagePacket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public final class AutoMessagesBungeePlugin extends Plugin implements Listener {

    private ProxyRuntime runtime;

    @Override
    public void onEnable() {
        getProxy().registerChannel(ProxyMessagePacket.CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        runtime = new ProxyRuntime(
                new BungeeProxyPlatform(this),
                getDataFolder().toPath(),
                getClass().getClassLoader()
        );
        runtime.start();
        getProxy().getConsole().sendMessage(
                TextComponent.fromLegacyText(ChatColor.AQUA + "AutoMessages foi criado por Lipe.")
        );
        getLogger().info("AutoMessages foi ativado.");
    }

    @Override
    public void onDisable() {
        if (runtime != null) {
            runtime.stop();
        }
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().unregisterChannel(ProxyMessagePacket.CHANNEL);
        getLogger().info("AutoMessages foi desativado.");
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (ProxyMessagePacket.CHANNEL.equals(event.getTag())) {
            event.setCancelled(true);
        }
    }
}
