package br.com.lipe.automessages.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BroadcastMessage {

    private final String id;
    private final boolean enabled;
    private final BroadcastType type;
    private final MessageFormat format;
    private final List<String> text;
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;
    private final String bossBarColor;
    private final String bossBarStyle;
    private final float progress;
    private final long durationSeconds;
    private final long intervalSeconds;

    private BroadcastMessage(Builder builder) {
        id = builder.id;
        enabled = builder.enabled;
        type = builder.type;
        format = builder.format;
        text = Collections.unmodifiableList(new ArrayList<String>(builder.text));
        title = builder.title;
        subtitle = builder.subtitle;
        fadeIn = builder.fadeIn;
        stay = builder.stay;
        fadeOut = builder.fadeOut;
        bossBarColor = builder.bossBarColor;
        bossBarStyle = builder.bossBarStyle;
        progress = builder.progress;
        durationSeconds = builder.durationSeconds;
        intervalSeconds = builder.intervalSeconds;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public BroadcastType getType() {
        return type;
    }

    public MessageFormat getFormat() {
        return format;
    }

    public List<String> getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public String getBossBarColor() {
        return bossBarColor;
    }

    public String getBossBarStyle() {
        return bossBarStyle;
    }

    public float getProgress() {
        return progress;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public BroadcastMessage withTextPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty() || type != BroadcastType.CHAT) {
            return this;
        }
        Builder builder = builder(id)
                .enabled(enabled)
                .type(type)
                .format(format)
                .title(title)
                .subtitle(subtitle)
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut)
                .bossBarColor(bossBarColor)
                .bossBarStyle(bossBarStyle)
                .progress(progress)
                .durationSeconds(durationSeconds)
                .intervalSeconds(intervalSeconds);
        for (String line : text) {
            builder.addText(prefix + line);
        }
        return builder.build();
    }

    public static final class Builder {

        private final String id;
        private boolean enabled = true;
        private BroadcastType type = BroadcastType.CHAT;
        private MessageFormat format = MessageFormat.AUTO;
        private List<String> text = new ArrayList<String>();
        private String title = "";
        private String subtitle = "";
        private int fadeIn = 10;
        private int stay = 70;
        private int fadeOut = 20;
        private String bossBarColor = "BLUE";
        private String bossBarStyle = "PROGRESS";
        private float progress = 1.0F;
        private long durationSeconds = 10L;
        private long intervalSeconds = 0L;

        private Builder(String id) {
            this.id = id;
        }

        public Builder enabled(boolean value) {
            enabled = value;
            return this;
        }

        public Builder type(BroadcastType value) {
            type = value == null ? BroadcastType.CHAT : value;
            return this;
        }

        public Builder format(MessageFormat value) {
            format = value == null ? MessageFormat.AUTO : value;
            return this;
        }

        public Builder text(List<String> value) {
            text = value == null ? new ArrayList<String>() : new ArrayList<String>(value);
            return this;
        }

        public Builder addText(String value) {
            if (value != null) {
                text.add(value);
            }
            return this;
        }

        public Builder title(String value) {
            title = value == null ? "" : value;
            return this;
        }

        public Builder subtitle(String value) {
            subtitle = value == null ? "" : value;
            return this;
        }

        public Builder fadeIn(int value) {
            fadeIn = Math.max(0, value);
            return this;
        }

        public Builder stay(int value) {
            stay = Math.max(0, value);
            return this;
        }

        public Builder fadeOut(int value) {
            fadeOut = Math.max(0, value);
            return this;
        }

        public Builder bossBarColor(String value) {
            bossBarColor = value == null ? "BLUE" : value.toUpperCase(java.util.Locale.ENGLISH);
            return this;
        }

        public Builder bossBarStyle(String value) {
            bossBarStyle = value == null ? "PROGRESS" : value.toUpperCase(java.util.Locale.ENGLISH);
            return this;
        }

        public Builder progress(float value) {
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                value = 1.0F;
            }
            progress = Math.max(0.0F, Math.min(1.0F, value));
            return this;
        }

        public Builder durationSeconds(long value) {
            durationSeconds = Math.max(0L, value);
            return this;
        }

        public Builder intervalSeconds(long value) {
            intervalSeconds = Math.max(0L, value);
            return this;
        }

        public BroadcastMessage build() {
            return new BroadcastMessage(this);
        }
    }
}
