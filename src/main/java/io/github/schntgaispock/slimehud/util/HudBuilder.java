package io.github.schntgaispock.slimehud.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class HudBuilder {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private HudBuilder() {}

    public static String formatEnergyStored(long energy) {
        return getAbbreviatedNumber(energy) + " J Stored";
    }

    public static String formatEnergyStored(long energy, long totalEnergy) {
        return getAbbreviatedNumber(energy) + "/" + getAbbreviatedNumber(totalEnergy) + " J";
    }

    public static String formatEnergyGenerated(long energy) {
        return "&e⚡&7 " + getAbbreviatedNumber(energy) + " J/t";
    }

    public static String getProgressBar(int progress, int total) {
        if (total <= 0) {
            return "";
        }
        return getProgressBar((int) Math.round(100.0D * progress / total));
    }

    public static String getProgressBar(int percentCompleted) {
        int percent = Math.min(Math.max(percentCompleted, 0), 100);
        int filled = Math.min(10, Math.max(0, (int) Math.round(percent / 10.0D)));
        char color = percent < 15 ? '4' : percent < 30 ? 'c' : percent < 45 ? '6' : percent < 60 ? 'e' : percent < 75 ? 'a' : '2';
        return "&" + color + "|".repeat(filled) + "&7" + "|".repeat(10 - filled);
    }

    public static String formatProgressBar(int progress, int total) {
        if (total <= 0) {
            return "";
        }
        int percent = Math.min(100, Math.max(0, (int) Math.round(100.0D * progress / total)));
        return getProgressBar(percent) + " - " + percent + "%";
    }

    public static String getAbbreviatedNumber(long value) {
        long absolute = Math.abs(value);
        if (absolute < 1_000L) {
            return Long.toString(value);
        }

        String[] units = {"K", "M", "B", "T", "Qa", "Qi"};
        double scaled = absolute;
        int unit = -1;
        while (scaled >= 1_000.0D && unit < units.length - 1) {
            scaled /= 1_000.0D;
            unit++;
        }
        if (value < 0) {
            scaled = -scaled;
        }
        String rendered = scaled >= 100 ? String.format(Locale.US, "%.0f", scaled)
                : scaled >= 10 ? String.format(Locale.US, "%.1f", scaled)
                : String.format(Locale.US, "%.2f", scaled);
        rendered = rendered.replaceAll("\\.?0+$", "");
        return rendered + units[unit];
    }

    public static String getCommaNumber(long value) {
        synchronized (NUMBER_FORMAT) {
            return NUMBER_FORMAT.format(value);
        }
    }
}
