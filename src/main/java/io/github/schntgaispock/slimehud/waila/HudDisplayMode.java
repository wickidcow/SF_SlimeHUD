package io.github.schntgaispock.slimehud.waila;

import java.util.Locale;

public enum HudDisplayMode {
    BOSSBAR,
    ACTIONBAR;

    public static HudDisplayMode parse(String value, HudDisplayMode fallback) {
        if (value == null) {
            return fallback;
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "bossbar", "boss", "bar" -> BOSSBAR;
            case "actionbar", "action", "hotbar" -> ACTIONBAR;
            default -> fallback;
        };
    }

    public String configName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
