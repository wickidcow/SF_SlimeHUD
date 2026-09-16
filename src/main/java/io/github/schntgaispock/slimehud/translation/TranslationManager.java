package io.github.schntgaispock.slimehud.translation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.entity.Player;

/**
 * Keeps SlimeHUD independent from translation plugins while preserving a single
 * place for future translation hooks. Slimefun Legacy supplies the canonical
 * registered item name for every core, built-in addon, and external addon item.
 */
public final class TranslationManager {

    public String getItemName(Player player, SlimefunItem item) {
        return item.getItemName();
    }
}
