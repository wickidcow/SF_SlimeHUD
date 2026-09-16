package io.github.schntgaispock.slimehud.placeholder;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.waila.HudPreferences;
import io.github.schntgaispock.slimehud.waila.PlayerWAILA;
import io.github.schntgaispock.slimehud.waila.WAILAManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public final class PlaceholderHook extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "slimehud";
    }

    @Override
    public String getAuthor() {
        return "wickidcow";
    }

    @Override
    public String getVersion() {
        return SlimeHUD.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        if (params.equalsIgnoreCase("toggle") || params.equalsIgnoreCase("enabled")) {
            return Boolean.toString(HudPreferences.isEnabled(player));
        }
        if (params.equalsIgnoreCase("display")) {
            return HudPreferences.getDisplayMode(player).configName();
        }

        PlayerWAILA hud = WAILAManager.getInstance().getWailas().get(player.getUniqueId());
        if (hud == null) {
            return "";
        }
        return switch (params.toLowerCase()) {
            case "hud" -> hud.getFacing();
            case "hud_block" -> hud.getFacingBlock();
            case "hud_block_info" -> hud.getFacingBlockInfo();
            default -> null;
        };
    }
}
