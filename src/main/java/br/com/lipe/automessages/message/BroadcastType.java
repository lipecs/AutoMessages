package br.com.lipe.automessages.message;

public enum BroadcastType {
    CHAT,
    ACTION_BAR,
    TITLE,
    BOSS_BAR;

    public static BroadcastType from(String value) {
        if (value == null) {
            return CHAT;
        }
        try {
            return valueOf(value.trim().toUpperCase(java.util.Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            return CHAT;
        }
    }
}
