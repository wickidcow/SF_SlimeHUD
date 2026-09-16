package io.github.schntgaispock.slimehud.waila;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import javax.annotation.Nonnull;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class HudRequest {

    private final @Nonnull SlimefunItem slimefunItem;
    private final @Nonnull Location location;
    private final @Nonnull Player player;

    public HudRequest(@Nonnull SlimefunItem slimefunItem, @Nonnull Location location, @Nonnull Player player) {
        this.slimefunItem = slimefunItem;
        this.location = location;
        this.player = player;
    }

    public @Nonnull SlimefunItem getSlimefunItem() {
        return slimefunItem;
    }

    public @Nonnull Location getLocation() {
        return location;
    }

    public @Nonnull Player getPlayer() {
        return player;
    }
}
