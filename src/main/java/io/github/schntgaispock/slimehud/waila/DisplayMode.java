package io.github.schntgaispock.slimehud.waila;

public enum DisplayMode {
    BOSSBAR("bossbar"),
    ACTIONBAR("actionbar");

    private final String id;

    DisplayMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static DisplayMode from(String value, DisplayMode fallback) {
        if (value == null) {
            return fallback;
        }

        return switch (value.trim().toLowerCase()) {
            case "bossbar", "boss" -> BOSSBAR;
            case "actionbar", "action", "hotbar" -> ACTIONBAR;
            default -> fallback;
        };
    }
}
