package io.github.schntgaispock.slimehud.util;

import io.github.schntgaispock.slimehud.SlimeHUD;
import java.util.Collections;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;

public final class Util {

    private static final HashMap<RGB, BarColor> BAR_COLORS = new HashMap<>();
    private static final HashMap<RGB, BarColor> CACHE = new HashMap<>();

    static {
        BAR_COLORS.put(new RGB(0x00, 0xb9, 0xec), BarColor.BLUE);
        BAR_COLORS.put(new RGB(0x16, 0xb9, 0x00), BarColor.GREEN);
        BAR_COLORS.put(new RGB(0xb9, 0x00, 0x90), BarColor.PINK);
        BAR_COLORS.put(new RGB(0x61, 0x00, 0xb9), BarColor.PURPLE);
        BAR_COLORS.put(new RGB(0xb9, 0x2a, 0x00), BarColor.RED);
        BAR_COLORS.put(new RGB(0xff, 0xff, 0xff), BarColor.WHITE);
        BAR_COLORS.put(new RGB(0xb9, 0xb9, 0x00), BarColor.YELLOW);
    }

    private Util() {}

    public static BarColor pickBarColorFromName(String name) {
        char colorCode = name != null && name.trim().startsWith("§") && name.trim().length() > 1 ? name.trim().charAt(1) : ' ';
        if (colorCode == 'x' && name != null) {
            try {
                String stripped = name.replace("§", "");
                int red = Integer.parseInt(stripped.substring(1, 3), 16);
                int green = Integer.parseInt(stripped.substring(3, 5), 16);
                int blue = Integer.parseInt(stripped.substring(5, 7), 16);
                RGB rgb = new RGB(red, green, blue);
                if (CACHE.containsKey(rgb)) {
                    return CACHE.get(rgb);
                }
                BarColor color = BAR_COLORS.get(Collections.min(BAR_COLORS.keySet(), (a, b) -> Integer.compare(errorSquared(a, rgb), errorSquared(b, rgb))));
                CACHE.put(rgb, color);
                return color;
            } catch (RuntimeException ignored) {
                return BarColor.WHITE;
            }
        }

        return switch (colorCode) {
            case '4', 'c' -> BarColor.RED;
            case '6', 'e' -> BarColor.YELLOW;
            case '2', 'a' -> BarColor.GREEN;
            case '3', 'b' -> BarColor.BLUE;
            case '1', '5', '9' -> BarColor.PURPLE;
            case 'd' -> BarColor.PINK;
            default -> BarColor.WHITE;
        };
    }

    public static BarColor pickBarColorFromColor(String color) {
        String value = color == null ? "inherit" : color.trim().toLowerCase();
        return switch (value) {
            case "red" -> BarColor.RED;
            case "yellow" -> BarColor.YELLOW;
            case "green" -> BarColor.GREEN;
            case "blue" -> BarColor.BLUE;
            case "purple" -> BarColor.PURPLE;
            case "pink" -> BarColor.PINK;
            case "white", "default", "inherit" -> BarColor.WHITE;
            default -> {
                SlimeHUD.getInstance().getLogger().warning("Invalid bossbar color '" + value + "'; using white.");
                yield BarColor.WHITE;
            }
        };
    }

    public static ChatColor getColorFromCargoChannel(int channel) {
        return switch (channel) {
            case 1 -> ChatColor.WHITE;
            case 2 -> ChatColor.GOLD;
            case 3 -> ChatColor.BLUE;
            case 4 -> ChatColor.AQUA;
            case 5 -> ChatColor.YELLOW;
            case 6 -> ChatColor.GREEN;
            case 7 -> ChatColor.LIGHT_PURPLE;
            case 8 -> ChatColor.DARK_GRAY;
            case 9 -> ChatColor.GRAY;
            case 10 -> ChatColor.DARK_AQUA;
            case 11 -> ChatColor.DARK_PURPLE;
            case 12 -> ChatColor.DARK_BLUE;
            case 13 -> ChatColor.RED;
            case 14 -> ChatColor.DARK_GREEN;
            case 15 -> ChatColor.DARK_RED;
            case 16 -> ChatColor.BLACK;
            default -> ChatColor.WHITE;
        };
    }

    private static int errorSquared(RGB a, RGB b) {
        int dr = a.red - b.red;
        int dg = a.green - b.green;
        int db = a.blue - b.blue;
        return dr * dr + dg * dg + db * db;
    }

    private record RGB(int red, int green, int blue) {}
}
