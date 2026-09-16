package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class HudPreferences {

    private HudPreferences() {}

    public static boolean isEnabled(Player player) {
        Byte stored = data(player).get(SlimeHUD.newNamespacedKey("hud-enabled"), PersistentDataType.BYTE);
        if (stored != null) {
            return stored != 0;
        }
        return SlimeHUD.getInstance().getConfig().getBoolean("waila.enabled-by-default", true);
    }

    public static void setEnabled(Player player, boolean enabled) {
        data(player).set(SlimeHUD.newNamespacedKey("hud-enabled"), PersistentDataType.BYTE, (byte) (enabled ? 1 : 0));
    }

    public static HudDisplayMode getDisplayMode(Player player) {
        String stored = data(player).get(SlimeHUD.newNamespacedKey("hud-display"), PersistentDataType.STRING);
        String configured = SlimeHUD.getInstance().getConfig().getString(
                "waila.default-display",
                SlimeHUD.getInstance().getConfig().getString("waila.location", "bossbar"));
        HudDisplayMode fallback = HudDisplayMode.parse(configured, HudDisplayMode.BOSSBAR);
        return HudDisplayMode.parse(stored, fallback);
    }

    public static void setDisplayMode(Player player, HudDisplayMode mode) {
        data(player).set(SlimeHUD.newNamespacedKey("hud-display"), PersistentDataType.STRING, mode.configName());
    }

    private static PersistentDataContainer data(Player player) {
        return player.getPersistentDataContainer();
    }
}
