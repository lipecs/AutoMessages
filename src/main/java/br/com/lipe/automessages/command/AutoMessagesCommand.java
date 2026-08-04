package br.com.lipe.automessages.command;

import br.com.lipe.automessages.AutoMessagesPlugin;
import br.com.lipe.automessages.config.ConfigManager;
import br.com.lipe.automessages.message.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AutoMessagesCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "automessages.admin";
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "help", "reload", "list", "enable", "disable", "toggle", "send"
    );

    private final AutoMessagesPlugin plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    public AutoMessagesCommand(AutoMessagesPlugin plugin, ConfigManager configManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        if ("help".equals(subcommand)) {
            showHelp(sender, args);
        } else if ("reload".equals(subcommand)) {
            reload(sender, args);
        } else if ("list".equals(subcommand)) {
            list(sender, args);
        } else if ("enable".equals(subcommand)) {
            setSystemEnabled(sender, args, true);
        } else if ("disable".equals(subcommand)) {
            setSystemEnabled(sender, args, false);
        } else if ("toggle".equals(subcommand)) {
            toggle(sender, args);
        } else if ("send".equals(subcommand)) {
            send(sender, args);
        } else {
            sendAdministrativeMessage(sender, "invalid-command");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterByPrefix(getAvailableSubcommands(sender), args[0]);
        }

        if (args.length == 2 && "toggle".equalsIgnoreCase(args[0]) && hasPermission(sender, "automessages.toggle")) {
            return filterByPrefix(configManager.getMessageIds(), args[1]);
        }

        if (args.length == 2 && "send".equalsIgnoreCase(args[0]) && hasPermission(sender, "automessages.send")) {
            return filterByPrefix(configManager.getMessageIds(), args[1]);
        }

        return Collections.emptyList();
    }

    private void showHelp(CommandSender sender) {
        if (!hasAnyAdministrativePermission(sender)) {
            sendAdministrativeMessage(sender, "no-permission");
            return;
        }
        sendAdministrativeMessages(sender, "help");
    }

    private void showHelp(CommandSender sender, String[] args) {
        if (!validateArgumentCount(sender, args, 1, "help-usage")) {
            return;
        }
        showHelp(sender);
    }

    private void reload(CommandSender sender, String[] args) {
        if (!validatePermission(sender, "automessages.reload") || !validateArgumentCount(sender, args, 1, "reload-usage")) {
            return;
        }

        plugin.reloadPlugin();
        Map<String, String> placeholders = configManager.placeholders(
                "interval", String.valueOf(configManager.getIntervalSeconds())
        );
        sendAdministrativeMessage(sender, "reloaded", placeholders);
    }

    private void list(CommandSender sender, String[] args) {
        if (!validatePermission(sender, "automessages.list") || !validateArgumentCount(sender, args, 1, "list-usage")) {
            return;
        }

        List<String> ids = configManager.getMessageIds();
        if (ids.isEmpty()) {
            sendAdministrativeMessage(sender, "no-messages");
            return;
        }

        sendAdministrativeMessage(sender, "list-header", configManager.placeholders("count", String.valueOf(ids.size())));
        for (String id : ids) {
            String statusPath = configManager.isMessageEnabled(id) ? "status-enabled" : "status-disabled";
            Map<String, String> placeholders = configManager.placeholders(
                    "id", id,
                    "status", configManager.getRawAdministrativeMessage(statusPath)
            );
            sendAdministrativeMessage(sender, "list-entry", placeholders);
        }
    }

    private void setSystemEnabled(CommandSender sender, String[] args, boolean enabled) {
        if (!validatePermission(sender, ADMIN_PERMISSION)) {
            return;
        }

        String usagePath = enabled ? "enable-usage" : "disable-usage";
        if (!validateArgumentCount(sender, args, 1, usagePath)) {
            return;
        }

        if (configManager.isSystemEnabled() == enabled) {
            sendAdministrativeMessage(sender, enabled ? "already-enabled" : "already-disabled");
            return;
        }

        plugin.setAutomaticMessagesEnabled(enabled);
        sendAdministrativeMessage(sender, enabled ? "enabled" : "disabled");
    }

    private void toggle(CommandSender sender, String[] args) {
        if (!validatePermission(sender, "automessages.toggle") || !validateArgumentCount(sender, args, 2, "toggle-usage")) {
            return;
        }

        String id = configManager.findMessageId(args[1]);
        if (id == null) {
            sendAdministrativeMessage(sender, "unknown-message");
            return;
        }

        boolean enabled = !configManager.isMessageEnabled(id);
        configManager.setMessageEnabled(id, enabled);
        Map<String, String> placeholders = configManager.placeholders(
                "id", id,
                "status", configManager.getRawAdministrativeMessage(enabled ? "status-enabled" : "status-disabled")
        );
        sendAdministrativeMessage(sender, enabled ? "message-enabled" : "message-disabled", placeholders);
    }

    private void send(CommandSender sender, String[] args) {
        if (!validatePermission(sender, "automessages.send") || !validateArgumentCount(sender, args, 2, "send-usage")) {
            return;
        }

        String id = configManager.findMessageId(args[1]);
        if (id == null) {
            sendAdministrativeMessage(sender, "unknown-message");
            return;
        }

        MessageManager.SendResult result = messageManager.broadcastMessage(id);
        if (result == MessageManager.SendResult.EMPTY_MESSAGE) {
            sendAdministrativeMessage(sender, "empty-message", configManager.placeholders("id", id));
            return;
        }
        sendAdministrativeMessage(sender, "message-sent", configManager.placeholders("id", id));
    }

    private boolean validatePermission(CommandSender sender, String permission) {
        if (hasPermission(sender, permission)) {
            return true;
        }
        sendAdministrativeMessage(sender, "no-permission");
        return false;
    }

    private boolean validateArgumentCount(CommandSender sender, String[] args, int expected, String usagePath) {
        if (args.length == expected) {
            return true;
        }
        sendAdministrativeMessage(sender, usagePath);
        return false;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(ADMIN_PERMISSION) || sender.hasPermission(permission);
    }

    private boolean hasAnyAdministrativePermission(CommandSender sender) {
        return sender.hasPermission(ADMIN_PERMISSION)
                || sender.hasPermission("automessages.reload")
                || sender.hasPermission("automessages.list")
                || sender.hasPermission("automessages.toggle")
                || sender.hasPermission("automessages.send");
    }

    private List<String> getAvailableSubcommands(CommandSender sender) {
        if (!hasAnyAdministrativePermission(sender)) {
            return Collections.emptyList();
        }

        List<String> available = new ArrayList<String>();
        for (String subcommand : SUBCOMMANDS) {
            if (canUseSubcommand(sender, subcommand)) {
                available.add(subcommand);
            }
        }
        return available;
    }

    private boolean canUseSubcommand(CommandSender sender, String subcommand) {
        if ("help".equals(subcommand)) {
            return hasAnyAdministrativePermission(sender);
        }
        if ("enable".equals(subcommand) || "disable".equals(subcommand)) {
            return sender.hasPermission(ADMIN_PERMISSION);
        }
        return hasPermission(sender, "automessages." + subcommand);
    }

    private List<String> filterByPrefix(List<String> values, String input) {
        String normalizedInput = input.toLowerCase(Locale.ENGLISH);
        List<String> matches = new ArrayList<String>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ENGLISH).startsWith(normalizedInput)) {
                matches.add(value);
            }
        }
        return matches;
    }

    private void sendAdministrativeMessages(CommandSender sender, String path) {
        for (String line : configManager.getFormattedAdministrativeMessages(path)) {
            sender.sendMessage(line);
        }
    }

    private void sendAdministrativeMessage(CommandSender sender, String path) {
        sender.sendMessage(configManager.getFormattedAdministrativeMessage(path));
    }

    private void sendAdministrativeMessage(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(configManager.getFormattedAdministrativeMessage(path, placeholders));
    }
}
