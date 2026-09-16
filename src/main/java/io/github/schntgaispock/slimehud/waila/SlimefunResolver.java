package io.github.schntgaispock.slimehud.waila;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.lang.reflect.Method;
import java.util.Optional;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;

public final class SlimefunResolver {

    private static final LegacyCacheApi CACHE_API = LegacyCacheApi.detect();

    private SlimefunResolver() {}

    public static SlimefunItem at(Location location) {
        if (CACHE_API.available()) {
            try {
                String id = CACHE_API.findId(location);
                return id == null ? null : SlimefunItem.getById(id);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return null;
            }
        }

        // Compatibility fallback for older Slimefun builds. Slimefun Legacy uses the
        // cache-only path above, so normal HUD polling never needs to trigger a DB read.
        return BlockStorage.check(location.getBlock());
    }

    private record LegacyCacheApi(
            Object controller,
            Method getBlockDataFromCache,
            Method getUniversalBlockDataFromCache,
            Method getSfId,
            boolean available) {

        static LegacyCacheApi detect() {
            try {
                Class<?> slimefunClass = Class.forName("io.github.thebusybiscuit.slimefun4.implementation.Slimefun");
                Object databaseManager = slimefunClass.getMethod("getDatabaseManager").invoke(null);
                Object controller = databaseManager.getClass().getMethod("getBlockDataController").invoke(databaseManager);
                Method blockLookup = controller.getClass().getMethod("getBlockDataFromCache", Location.class);
                Method universalLookup = controller.getClass().getMethod("getUniversalBlockDataFromCache", Location.class);

                Class<?> blockDataClass = Class.forName("com.xzavier0722.mc.plugin.slimefun4.storage.controller.ASlimefunDataContainer");
                Method getSfId = blockDataClass.getMethod("getSfId");
                return new LegacyCacheApi(controller, blockLookup, universalLookup, getSfId, true);
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                return new LegacyCacheApi(null, null, null, null, false);
            }
        }

        String findId(Location location) throws ReflectiveOperationException {
            Object data = getBlockDataFromCache.invoke(controller, location);
            if (data != null) {
                return String.valueOf(getSfId.invoke(data));
            }

            Object universalResult = getUniversalBlockDataFromCache.invoke(controller, location);
            if (universalResult instanceof Optional<?> optional && optional.isPresent()) {
                return String.valueOf(getSfId.invoke(optional.get()));
            }
            return null;
        }
    }
}
