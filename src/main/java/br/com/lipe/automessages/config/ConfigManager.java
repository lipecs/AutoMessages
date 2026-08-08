package br.com.lipe.automessages.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import br.com.lipe.automessages.message.BroadcastMessage;
import br.com.lipe.automessages.message.BroadcastType;
import br.com.lipe.automessages.message.MessageFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ConfigManager {

    private static final long DEFAULT_INTERVAL_SECONDS = 60L;
    private static final long MAX_INTERVAL_SECONDS = Long.MAX_VALUE / 20L;

    private final JavaPlugin plugin;
    private File messagesFile;
    private FileConfiguration messagesConfiguration;
    private boolean systemEnabled;
    private long intervalSeconds;
    private boolean randomOrder;
    private boolean skipWhenEmpty;
    private boolean proxyEnabled;
    private String prefix;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.saveDefaultConfig();
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    public void reload() {
        ensureMessagesFile();
        plugin.reloadConfig();
        messagesConfiguration = YamlConfiguration.loadConfiguration(messagesFile);
        loadSettings();
    }

    public boolean isSystemEnabled() {
        return systemEnabled;
    }

    public void setSystemEnabled(boolean enabled) {
        systemEnabled = enabled;
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public boolean isRandomOrder() {
        return randomOrder;
    }

    public boolean shouldSkipWhenEmpty() {
        return skipWhenEmpty;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<String> getMessageIds() {
        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection == null) {
            return Collections.emptyList();
        }

        Set<String> keys = messagesSection.getKeys(false);
        return new ArrayList<String>(keys);
    }

    public String findMessageId(String input) {
        if (input == null) {
            return null;
        }

        for (String id : getMessageIds()) {
            if (id.equalsIgnoreCase(input)) {
                return id;
            }
        }
        return null;
    }

    public boolean isMessageEnabled(String id) {
        ConfigurationSection messageSection = getMessageSection(id);
        return messageSection != null && messageSection.getBoolean("enabled", true);
    }

    public BroadcastMessage getBroadcastMessage(String id) {
        String configuredId = findMessageId(id);
        ConfigurationSection section = getMessageSection(configuredId);
        if (section == null) {
            return null;
        }

        return BroadcastMessage.builder(configuredId)
                .enabled(section.getBoolean("enabled", true))
                .type(BroadcastType.from(section.getString("type", "CHAT")))
                .format(MessageFormat.from(section.getString("format", "AUTO")))
                .text(readText(section))
                .title(section.getString("title", ""))
                .subtitle(section.getString("subtitle", ""))
                .fadeIn(section.getInt("fade-in", 10))
                .stay(section.getInt("stay", 70))
                .fadeOut(section.getInt("fade-out", 20))
                .bossBarColor(section.getString("color", "BLUE"))
                .bossBarStyle(section.getString("style", "PROGRESS"))
                .progress((float) section.getDouble("progress", 1.0D))
                .durationSeconds(readNonNegativeLong(section.get("duration-seconds"), 10L))
                .intervalSeconds(readNonNegativeLong(section.get("interval-seconds"), 0L))
                .build();
    }

    public long getMessageIntervalSeconds(String id) {
        BroadcastMessage message = getBroadcastMessage(id);
        if (message == null || message.getIntervalSeconds() <= 0L) {
            return intervalSeconds;
        }
        return message.getIntervalSeconds();
    }

    public boolean createMessage(String id, BroadcastType type, String text) {
        if (!isValidMessageId(id) || findMessageId(id) != null) {
            return false;
        }
        String path = "messages." + id;
        plugin.getConfig().set(path + ".enabled", true);
        plugin.getConfig().set(path + ".type", type.name());
        plugin.getConfig().set(path + ".format", "AUTO");
        plugin.getConfig().set(path + ".text", Collections.singletonList(text));
        plugin.saveConfig();
        return true;
    }

    public boolean editMessageText(String id, String text) {
        String configuredId = findMessageId(id);
        if (configuredId == null) {
            return false;
        }
        plugin.getConfig().set("messages." + configuredId + ".text", Collections.singletonList(text));
        plugin.saveConfig();
        return true;
    }

    public boolean editMessageType(String id, BroadcastType type) {
        String configuredId = findMessageId(id);
        if (configuredId == null) {
            return false;
        }
        plugin.getConfig().set("messages." + configuredId + ".type", type.name());
        plugin.saveConfig();
        return true;
    }

    public boolean deleteMessage(String id) {
        String configuredId = findMessageId(id);
        if (configuredId == null) {
            return false;
        }
        plugin.getConfig().set("messages." + configuredId, null);
        plugin.saveConfig();
        return true;
    }

    public void setMessageEnabled(String id, boolean enabled) {
        String configuredId = findMessageId(id);
        if (configuredId == null) {
            return;
        }

        plugin.getConfig().set("messages." + configuredId + ".enabled", enabled);
        plugin.saveConfig();
    }

    public List<String> getMessageLines(String id) {
        ConfigurationSection messageSection = getMessageSection(id);
        if (messageSection == null) {
            return Collections.emptyList();
        }

        if (messageSection.isList("text")) {
            return new ArrayList<String>(messageSection.getStringList("text"));
        }

        if (messageSection.isString("text")) {
            return Collections.singletonList(messageSection.getString("text", ""));
        }

        return Collections.emptyList();
    }

    public String getRawAdministrativeMessage(String path) {
        List<String> lines = getRawAdministrativeMessages(path);
        if (lines.isEmpty()) {
            return "";
        }
        return lines.get(0);
    }

    public List<String> getFormattedAdministrativeMessages(String path) {
        return getFormattedAdministrativeMessages(path, Collections.<String, String>emptyMap());
    }

    public List<String> getFormattedAdministrativeMessages(String path, Map<String, String> placeholders) {
        List<String> formattedLines = new ArrayList<String>();
        for (String line : getRawAdministrativeMessages(path)) {
            formattedLines.add(colorize(replacePlaceholders(line, placeholders)));
        }
        return formattedLines;
    }

    public String getFormattedAdministrativeMessage(String path) {
        return getFormattedAdministrativeMessage(path, Collections.<String, String>emptyMap());
    }

    public String getFormattedAdministrativeMessage(String path, Map<String, String> placeholders) {
        List<String> lines = getFormattedAdministrativeMessages(path, placeholders);
        if (lines.isEmpty()) {
            return "";
        }
        return lines.get(0);
    }

    public Map<String, String> placeholders(String... values) {
        Map<String, String> placeholders = new LinkedHashMap<String, String>();
        for (int index = 0; index + 1 < values.length; index += 2) {
            placeholders.put(values[index], values[index + 1]);
        }
        return placeholders;
    }

    private void loadSettings() {
        FileConfiguration configuration = plugin.getConfig();
        systemEnabled = configuration.getBoolean("enabled", true);
        intervalSeconds = readInterval(configuration.get("interval-seconds"));
        randomOrder = configuration.getBoolean("random-order", false);
        skipWhenEmpty = configuration.getBoolean("skip-when-empty", true);
        proxyEnabled = configuration.getBoolean("proxy.enabled", false);
        prefix = configuration.getString("prefix", "");
        if (prefix == null) {
            prefix = "";
        }
    }

    private void ensureMessagesFile() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    private long readInterval(Object configuredValue) {
        if (!(configuredValue instanceof Number)) {
            warnInvalidInterval();
            return DEFAULT_INTERVAL_SECONDS;
        }

        long value = ((Number) configuredValue).longValue();
        if (value <= 0L || value > MAX_INTERVAL_SECONDS) {
            warnInvalidInterval();
            return DEFAULT_INTERVAL_SECONDS;
        }
        return value;
    }

    private void warnInvalidInterval() {
        plugin.getLogger().warning("interval-seconds é inválido. O valor padrão de 60 segundos será utilizado.");
    }

    private List<String> readText(ConfigurationSection section) {
        if (section.isList("text")) {
            return new ArrayList<String>(section.getStringList("text"));
        }
        if (section.isString("text")) {
            return Collections.singletonList(section.getString("text", ""));
        }
        return Collections.emptyList();
    }

    private long readNonNegativeLong(Object value, long defaultValue) {
        if (!(value instanceof Number)) {
            return defaultValue;
        }
        return Math.max(0L, ((Number) value).longValue());
    }

    private boolean isValidMessageId(String id) {
        return id != null && id.matches("[A-Za-z0-9_-]{1,32}");
    }

    private ConfigurationSection getMessageSection(String id) {
        String configuredId = findMessageId(id);
        if (configuredId == null) {
            return null;
        }

        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection == null) {
            return null;
        }
        return messagesSection.getConfigurationSection(configuredId);
    }

    private List<String> getRawAdministrativeMessages(String path) {
        if (messagesConfiguration == null) {
            return Collections.singletonList("&cArquivo messages.yml indisponível.");
        }

        if (messagesConfiguration.isList(path)) {
            return new ArrayList<String>(messagesConfiguration.getStringList(path));
        }

        if (messagesConfiguration.isString(path)) {
            return Collections.singletonList(messagesConfiguration.getString(path, ""));
        }

        return Collections.singletonList("&cMensagem administrativa ausente: " + path);
    }

    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        String result = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
