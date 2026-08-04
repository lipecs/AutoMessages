package br.com.lipe.automessages.proxy.config;

import br.com.lipe.automessages.proxy.ProxyLogger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ProxyConfigManager {

    private static final long DEFAULT_INTERVAL_SECONDS = 60L;
    private static final long MAX_INTERVAL_SECONDS = Long.MAX_VALUE / 20L;

    private final Path dataDirectory;
    private final Path configPath;
    private final ClassLoader resourceLoader;
    private final ProxyLogger logger;
    private Map<String, Object> root = new LinkedHashMap<String, Object>();
    private boolean proxyEnabled;
    private boolean systemEnabled;
    private long intervalSeconds;
    private boolean randomOrder;
    private boolean skipWhenEmpty;
    private String prefix;

    public ProxyConfigManager(Path dataDirectory, ClassLoader resourceLoader, ProxyLogger logger) {
        this.dataDirectory = dataDirectory;
        this.configPath = dataDirectory.resolve("config.yml");
        this.resourceLoader = resourceLoader;
        this.logger = logger;
    }

    public void initialize() {
        createDefaultConfig();
        loadConfig();
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public boolean isSystemEnabled() {
        return systemEnabled;
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

    public String getPrefix() {
        return prefix;
    }

    public List<String> getMessageIds() {
        return new ArrayList<String>(getMessagesSection().keySet());
    }

    public boolean isMessageEnabled(String id) {
        Map<String, Object> message = getSection(getMessagesSection(), id);
        return getBoolean(message.get("enabled"), true);
    }

    public List<String> getMessageLines(String id) {
        Map<String, Object> message = getSection(getMessagesSection(), id);
        Object configuredText = message.get("text");
        if (configuredText instanceof String) {
            return Collections.singletonList((String) configuredText);
        }
        if (!(configuredText instanceof List)) {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<String>();
        for (Object line : (List<?>) configuredText) {
            if (line instanceof String) {
                lines.add((String) line);
            }
        }
        return lines;
    }

    private void createDefaultConfig() {
        try {
            Files.createDirectories(dataDirectory);
            if (Files.exists(configPath)) {
                return;
            }

            InputStream defaultConfig = resourceLoader.getResourceAsStream("config.yml");
            if (defaultConfig == null) {
                logger.warning("O config.yml padrão não foi encontrado dentro da JAR.");
                return;
            }

            try (InputStream input = defaultConfig) {
                Files.copy(input, configPath);
            }
        } catch (IOException exception) {
            logger.warning("Não foi possível criar o config.yml: " + exception.getMessage());
        }
    }

    private void loadConfig() {
        if (!Files.exists(configPath)) {
            loadDefaults();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            Object loaded = new Yaml(new SafeConstructor(new LoaderOptions())).load(reader);
            root = toMap(loaded);
        } catch (Exception exception) {
            logger.warning("Não foi possível carregar o config.yml: " + exception.getMessage());
            root = new LinkedHashMap<String, Object>();
        }
        loadSettings();
    }

    private void loadDefaults() {
        root = new LinkedHashMap<String, Object>();
        loadSettings();
    }

    private void loadSettings() {
        proxyEnabled = getBoolean(getSection(root, "proxy").get("enabled"), false);
        systemEnabled = getBoolean(root.get("enabled"), true);
        intervalSeconds = readInterval(root.get("interval-seconds"));
        randomOrder = getBoolean(root.get("random-order"), false);
        skipWhenEmpty = getBoolean(root.get("skip-when-empty"), true);
        prefix = getString(root.get("prefix"), "");
    }

    private long readInterval(Object value) {
        if (!(value instanceof Number)) {
            logger.warning("interval-seconds é inválido. O valor padrão de 60 segundos será utilizado.");
            return DEFAULT_INTERVAL_SECONDS;
        }

        long interval = ((Number) value).longValue();
        if (interval <= 0L || interval > MAX_INTERVAL_SECONDS) {
            logger.warning("interval-seconds é inválido. O valor padrão de 60 segundos será utilizado.");
            return DEFAULT_INTERVAL_SECONDS;
        }
        return interval;
    }

    private Map<String, Object> getMessagesSection() {
        return getSection(root, "messages");
    }

    private boolean getBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            if ("true".equalsIgnoreCase((String) value)) {
                return true;
            }
            if ("false".equalsIgnoreCase((String) value)) {
                return false;
            }
        }
        return defaultValue;
    }

    private String getString(Object value, String defaultValue) {
        return value instanceof String ? (String) value : defaultValue;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        if (!(value instanceof Map)) {
            return new LinkedHashMap<String, Object>();
        }
        return (Map<String, Object>) value;
    }

    private Map<String, Object> getSection(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (!(value instanceof Map)) {
            return Collections.emptyMap();
        }
        return toMap(value);
    }
}
