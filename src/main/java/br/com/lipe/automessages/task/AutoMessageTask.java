package br.com.lipe.automessages.task;

import br.com.lipe.automessages.config.ConfigManager;
import br.com.lipe.automessages.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class AutoMessageTask {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private BukkitTask scheduledTask;

    public AutoMessageTask(JavaPlugin plugin, ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    public void restart() {
        stop();
        if (!configManager.isSystemEnabled() || configManager.isProxyEnabled()) {
            return;
        }

        scheduleNext(configManager.getIntervalSeconds());
    }

    private void scheduleNext(long intervalSeconds) {
        long intervalTicks = safeTicks(intervalSeconds);
        scheduledTask = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                sendNextMessage();
            }
        }, intervalTicks);
    }

    public void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel();
            scheduledTask = null;
        }
    }

    private void sendNextMessage() {
        if (configManager.shouldSkipWhenEmpty() && Bukkit.getOnlinePlayers().isEmpty()) {
            scheduleNext(configManager.getIntervalSeconds());
            return;
        }
        messageManager.broadcastNextMessage();
        scheduleNext(messageManager.getLastIntervalSeconds());
    }

    private long safeTicks(long seconds) {
        return seconds > Long.MAX_VALUE / 20L ? Long.MAX_VALUE / 20L : seconds * 20L;
    }
}
