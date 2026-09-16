package io.github.schntgaispock.slimehud.translation;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.entity.Player;

public final class TranslationManager {

    public String getItemName(Player player, SlimefunItem sfItem) {
        return sfItem.getItemName();
    }
}
