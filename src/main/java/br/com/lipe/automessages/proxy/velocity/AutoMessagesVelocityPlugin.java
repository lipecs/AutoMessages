package br.com.lipe.automessages.proxy.velocity;

import br.com.lipe.automessages.proxy.ProxyRuntime;
import br.com.lipe.automessages.proxy.message.ProxyMessagePacket;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "automessages",
        name = "AutoMessages",
        version = "1.0.0",
        description = "Distribui mensagens automáticas entre servidores conectados ao proxy.",
        authors = {"Lipe"}
)
public final class AutoMessagesVelocityPlugin {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final MinecraftChannelIdentifier channelIdentifier;
    private ProxyRuntime runtime;

    @Inject
    public AutoMessagesVelocityPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        channelIdentifier = MinecraftChannelIdentifier.from(ProxyMessagePacket.CHANNEL);
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        proxyServer.getChannelRegistrar().register(channelIdentifier);
        runtime = new ProxyRuntime(
                new VelocityProxyPlatform(this, proxyServer, logger, channelIdentifier),
                dataDirectory,
                getClass().getClassLoader()
        );
        runtime.start();
        logger.info("\u001B[36mAutoMessages foi criado por Lipe.\u001B[0m");
        logger.info("AutoMessages foi ativado.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (runtime != null) {
            runtime.stop();
        }
        proxyServer.getChannelRegistrar().unregister(channelIdentifier);
        logger.info("AutoMessages foi desativado.");
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (channelIdentifier.equals(event.getIdentifier())) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }
}
