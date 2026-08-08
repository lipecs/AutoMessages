package br.com.lipe.automessages.message;

public enum MessageFormat {
    AUTO,
    LEGACY,
    MINI_MESSAGE;

    public static MessageFormat from(String value) {
        if (value == null) {
            return AUTO;
        }
        try {
            return valueOf(value.trim().toUpperCase(java.util.Locale.ENGLISH));
        } catch (IllegalArgumentException exception) {
            return AUTO;
        }
    }
}
