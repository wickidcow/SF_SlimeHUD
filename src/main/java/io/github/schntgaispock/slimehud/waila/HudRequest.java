package io.github.schntgaispock.slimehud.waila;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class HudRequest {

    private final SlimefunItem slimefunItem;
    private final Location location;
    private final Player player;

    public HudRequest(SlimefunItem slimefunItem, Location location, Player player) {
        this.slimefunItem = slimefunItem;
        this.location = location;
        this.player = player;
    }

    public SlimefunItem getSlimefunItem() {
        return slimefunItem;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }
}
