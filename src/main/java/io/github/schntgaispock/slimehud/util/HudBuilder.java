package io.github.schntgaispock.slimehud.util;

import java.text.NumberFormat;

public final class HudBuilder {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private HudBuilder() {}

    public static String formatEnergyStored(int energy) {
        return formatEnergyStored((long) energy);
    }

    public static String formatEnergyStored(long energy) {
        return getAbbreviatedNumber(energy) + " J Stored";
    }

    public static String formatEnergyStored(int energy, int totalEnergy) {
        return formatEnergyStored((long) energy, (long) totalEnergy);
    }

    public static String formatEnergyStored(long energy, long totalEnergy) {
        return getAbbreviatedNumber(energy) + "/" + getAbbreviatedNumber(totalEnergy) + " J";
    }

    public static String formatEnergyGenerated(int energy) {
        return formatEnergyGenerated((long) energy);
    }

    public static String formatEnergyGenerated(long energy) {
        return "&e⚡&7 " + getAbbreviatedNumber(energy) + " J/t";
    }

    public static String getProgressBar(int progress, int total) {
        if (total <= 0) {
            return "";
        }
        return getProgressBar((int) (100L * progress / total));
    }

    public static String getProgressBar(int percentCompleted) {
        int percent = Math.min(Math.max(percentCompleted, 0), 100);
        int filled = Math.min(10, (percent + 9) / 10);
        char color = percent < 15 ? '4'
                : percent < 30 ? 'c'
                : percent < 45 ? '6'
                : percent < 60 ? 'e'
                : percent < 75 ? 'a'
                : '2';

        StringBuilder bar = new StringBuilder("&").append(color);
        for (int i = 0; i < filled; i++) {
            bar.append('|');
        }
        bar.append("&7");
        for (int i = filled; i < 10; i++) {
            bar.append('|');
        }
        return bar.toString();
    }

    public static String formatProgressBar(int progress, int total) {
        if (total <= 0) {
            return "";
        }
        int percent = (int) Math.min(100, Math.max(0, 100L * progress / total));
        return getProgressBar(percent) + " - " + percent + "%";
    }

    public static String getAbbreviatedNumber(long number) {
        long absolute = Math.abs(number);
        if (absolute < 1_000L) {
            return Long.toString(number);
        }

        String[] suffixes = {"K", "M", "B", "T", "Qa", "Qi"};
        double value = number;
        int suffix = -1;
        while (Math.abs(value) >= 1_000D && suffix < suffixes.length - 1) {
            value /= 1_000D;
            suffix++;
        }

        double truncated = Math.floor(Math.abs(value) * 100D) / 100D;
        if (value < 0) {
            truncated = -truncated;
        }
        String text = truncated == Math.rint(truncated)
                ? Long.toString((long) truncated)
                : String.format(java.util.Locale.ROOT, "%.2f", truncated).replaceAll("0+$", "").replaceAll("\\.$", "");
        return text + suffixes[suffix];
    }

    public static String getCommaNumber(long number) {
        return NUMBER_FORMAT.format(number);
    }
}
