package br.com.lipe.automessages.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AdventureMessageRenderer {

    private final JavaPlugin plugin;
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;
    private final List<BossBar> activeBossBars = new ArrayList<BossBar>();

    public AdventureMessageRenderer(JavaPlugin plugin) {
        this.plugin = plugin;
        audiences = BukkitAudiences.create(plugin);
        miniMessage = MiniMessage.miniMessage();
    }

    public void send(Player player, BroadcastMessage message) {
        Audience audience = audiences.player(player);
        switch (message.getType()) {
            case ACTION_BAR:
                audience.sendActionBar(deserialize(primaryText(message), message.getFormat(), player));
                break;
            case TITLE:
                sendTitle(audience, message, player);
                break;
            case BOSS_BAR:
                sendBossBarToAll(message);
                break;
            case CHAT:
            default:
                for (String line : message.getText()) {
                    audience.sendMessage(deserialize(line, message.getFormat(), player));
                }
                break;
        }
    }

    public void close() {
        hideActiveBossBars();
        audiences.close();
    }

    private void sendTitle(Audience audience, BroadcastMessage message, Player player) {
        Component title = deserialize(message.getTitle(), message.getFormat(), player);
        Component subtitle = deserialize(message.getSubtitle(), message.getFormat(), player);
        Title.Times times = Title.Times.of(
                durationFromTicks(message.getFadeIn()),
                durationFromTicks(message.getStay()),
                durationFromTicks(message.getFadeOut())
        );
        audience.showTitle(Title.title(title, subtitle, times));
    }

    public void sendBossBarToAll(BroadcastMessage message) {
        Player context = Bukkit.getOnlinePlayers().isEmpty()
                ? null
                : Bukkit.getOnlinePlayers().iterator().next();
        BossBar bossBar = BossBar.bossBar(
                deserialize(primaryText(message), message.getFormat(), context),
                message.getProgress(),
                bossBarColor(message.getBossBarColor()),
                bossBarStyle(message.getBossBarStyle())
        );
        hideActiveBossBars();
        activeBossBars.add(bossBar);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("automessages.bypass")) {
                audiences.player(onlinePlayer).showBossBar(bossBar);
            }
        }

        if (message.getDurationSeconds() > 0L) {
            long ticks = safeTicks(message.getDurationSeconds());
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    hideBossBar(bossBar);
                }
            }, ticks);
        }
    }

    private void hideBossBar(BossBar bossBar) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            audiences.player(player).hideBossBar(bossBar);
        }
        activeBossBars.remove(bossBar);
    }

    private void hideActiveBossBars() {
        for (BossBar bossBar : new ArrayList<BossBar>(activeBossBars)) {
            hideBossBar(bossBar);
        }
    }

    private Component deserialize(String value, MessageFormat format, Player player) {
        String resolved = replacePlaceholders(value == null ? "" : value, player);
        if (format == MessageFormat.MINI_MESSAGE || (format == MessageFormat.AUTO && containsMiniMessage(resolved))) {
            try {
                return miniMessage.deserialize(resolved);
            } catch (RuntimeException exception) {
                return legacyComponent(resolved);
            }
        }
        return legacyComponent(resolved);
    }

    private Component legacyComponent(String value) {
        String translated = ChatColor.translateAlternateColorCodes('&', value);
        return LegacyComponentSerializer.legacySection().deserialize(translated);
    }

    private String replacePlaceholders(String value, Player player) {
        String result = value.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{server}", plugin.getServer().getName());
        if (player != null) {
            result = result.replace("{player}", player.getName()).replace("{name}", player.getName());
        }
        return result;
    }

    private String primaryText(BroadcastMessage message) {
        if (!message.getText().isEmpty()) {
            return joinLines(message.getText());
        }
        return message.getTitle();
    }

    private String joinLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private boolean containsMiniMessage(String value) {
        return value.matches("(?s).*<[!?#/]?[a-zA-Z][^>]*>.*");
    }

    private BossBar.Color bossBarColor(String value) {
        try {
            return BossBar.Color.valueOf(value.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            return BossBar.Color.BLUE;
        }
    }

    private BossBar.Overlay bossBarStyle(String value) {
        try {
            return BossBar.Overlay.valueOf(value.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    private Duration durationFromTicks(int ticks) {
        return Duration.ofMillis(Math.min((long) ticks * 50L, Long.MAX_VALUE / 2L));
    }

    private long safeTicks(long seconds) {
        return seconds > Long.MAX_VALUE / 20L ? Long.MAX_VALUE / 20L : seconds * 20L;
    }
}
