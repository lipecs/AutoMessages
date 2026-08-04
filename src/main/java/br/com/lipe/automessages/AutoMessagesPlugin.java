package br.com.lipe.automessages;

import br.com.lipe.automessages.command.AutoMessagesCommand;
import br.com.lipe.automessages.config.ConfigManager;
import br.com.lipe.automessages.message.MessageManager;
import br.com.lipe.automessages.proxy.bukkit.BukkitProxyBridge;
import br.com.lipe.automessages.task.AutoMessageTask;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoMessagesPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private AutoMessageTask autoMessageTask;
    private BukkitProxyBridge proxyBridge;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.initialize();
        messageManager = new MessageManager(configManager);
        autoMessageTask = new AutoMessageTask(this, configManager, messageManager);
        proxyBridge = new BukkitProxyBridge(this, configManager, messageManager);

        if (!registerCommand()) {
            getLogger().severe("Não foi possível registrar o comando /automessages.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        proxyBridge.restart();
        autoMessageTask.restart();
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "AutoMessages foi criado por Lipe.");
        getLogger().info("AutoMessages foi ativado.");
    }

    @Override
    public void onDisable() {
        if (autoMessageTask != null) {
            autoMessageTask.stop();
        }
        if (proxyBridge != null) {
            proxyBridge.stop();
        }
        getLogger().info("AutoMessages foi desativado.");
    }

    public void reloadPlugin() {
        configManager.reload();
        messageManager.resetSequence();
        proxyBridge.restart();
        autoMessageTask.restart();
    }

    public void setAutomaticMessagesEnabled(boolean enabled) {
        configManager.setSystemEnabled(enabled);
        autoMessageTask.restart();
    }

    private boolean registerCommand() {
        PluginCommand command = getCommand("automessages");
        if (command == null) {
            return false;
        }

        AutoMessagesCommand commandHandler = new AutoMessagesCommand(this, configManager, messageManager);
        command.setExecutor(commandHandler);
        command.setTabCompleter(commandHandler);
        return true;
    }
}
