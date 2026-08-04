package br.com.lipe.automessages.proxy;

public interface ProxyPlatform extends ProxyLogger {

    int getOnlinePlayerCount();

    void broadcastPluginMessage(byte[] data);

    ProxyTask scheduleRepeating(Runnable runnable, long intervalSeconds);
}
