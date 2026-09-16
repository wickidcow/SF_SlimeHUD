package io.github.schntgaispock.slimehud.waila;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Optional;
import org.bukkit.Location;

public final class SlimefunResolver {

    private SlimefunResolver() {}

    public static SlimefunItem at(Location location) {
        try {
            var controller = Slimefun.getDatabaseManager().getBlockDataController();
            SlimefunBlockData data = controller.getBlockDataFromCache(location);
            if (data != null) {
                return SlimefunItem.getById(data.getSfId());
            }

            Optional<?> universal = controller.getUniversalBlockDataFromCache(location);
            if (universal.isPresent()) {
                Object value = universal.get();
                if (value instanceof com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunUniversalBlockData universalData) {
                    return SlimefunItem.getById(universalData.getSfId());
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
            // HUD lookups must never destabilize a server tick. A later update will try again.
        }
        return null;
    }
}
