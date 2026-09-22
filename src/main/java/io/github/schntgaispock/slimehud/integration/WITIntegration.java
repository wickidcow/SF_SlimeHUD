package io.github.schntgaispock.slimehud.integration;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.waila.HudRequest;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiFunction;
import java.util.logging.Level;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 * Optional bridge for the What Is That? (WIT) plugin.
 *
 * <p>The bridge deliberately uses reflection so WIT never becomes a hard
 * dependency. When WIT is available, its own display loop remains responsible
 * for the visible BossBar/ActionBar and SF_SlimeHUD contributes Slimefun
 * machine information through WIT's public block-handler API.</p>
 */
public final class WITIntegration implements Listener {

    private static final String WIT_PLUGIN_NAME = "WIT";
    private static WITIntegration instance;

    private final SlimeHUD plugin;

    private Plugin witPlugin;
    private BiFunction<Block, Player, Boolean> blockHandler;
    private Object witAddonProxy;

    private Constructor<?> infoConstructor;
    private Method infoSetName;
    private Method infoAddSuffix;
    private Method apiAddBlockHandler;
    private Method apiRemoveBlockHandler;
    private Method apiUpdateBar;
    private Method apiRegisterAddon;
    private Method apiUnregisterAddon;
    private Method witGetSettings;
    private Method witIsDisabledWorld;
    private Method witIsHidden;
    private Field settingsDisabled;
    private Field witSneakingMode;

    private boolean hooked;

    private WITIntegration(SlimeHUD plugin) {
        this.plugin = plugin;
    }

    public static void setup() {
        if (instance != null) {
            return;
        }

        instance = new WITIntegration(SlimeHUD.getInstance());
        Bukkit.getPluginManager().registerEvents(instance, SlimeHUD.getInstance());
        instance.tryHook();
    }

    public static void shutdown() {
        if (instance == null) {
            return;
        }

        instance.unhook();
        HandlerList.unregisterAll(instance);
        instance = null;
    }

    public static void refresh() {
        if (instance == null) {
            return;
        }

        instance.unhook();
        instance.tryHook();
    }

    public static boolean isHooked() {
        return instance != null && instance.hooked;
    }

    /**
     * Returns true only while WIT is actively responsible for this player's
     * display. If WIT is hidden, disabled for the player/world, or unavailable,
     * SF_SlimeHUD keeps rendering its own HUD.
     */
    public static boolean shouldDelegate(Player player) {
        return instance != null && instance.shouldDelegateInternal(player);
    }

    private boolean integrationEnabled() {
        return plugin.getConfig().getBoolean("integrations.what-is-that.enabled", true);
    }

    private void tryHook() {
        if (hooked || !integrationEnabled()) {
            return;
        }

        Plugin candidate = Bukkit.getPluginManager().getPlugin(WIT_PLUGIN_NAME);
        if (candidate == null || !candidate.isEnabled()) {
            return;
        }

        try {
            ClassLoader loader = candidate.getClass().getClassLoader();
            Class<?> apiClass = loader.loadClass("com.github.darksoulq.wit.api.API");
            Class<?> infoClass = loader.loadClass("com.github.darksoulq.wit.api.Info");
            Class<?> witPluginClass = loader.loadClass("com.github.darksoulq.wit.api.WITPlugin");
            Class<?> witListenerClass = loader.loadClass("com.github.darksoulq.wit.WITListener");
            Class<?> settingsClass = loader.loadClass("com.github.darksoulq.wit.WITListener$PlayerSettings");

            infoConstructor = infoClass.getConstructor();
            infoSetName = infoClass.getMethod("setName", Component.class);
            infoAddSuffix = infoClass.getMethod("addSuffix", Component.class);

            apiAddBlockHandler = apiClass.getMethod("addBlockHandler", BiFunction.class);
            apiRemoveBlockHandler = apiClass.getMethod("removeBlockHandler", BiFunction.class);
            apiUpdateBar = apiClass.getMethod("updateBar", infoClass, Player.class);
            apiRegisterAddon = apiClass.getMethod("registerAddon", witPluginClass);
            apiUnregisterAddon = apiClass.getMethod("unregisterAddon", witPluginClass);

            witGetSettings = witListenerClass.getMethod("getSettings", Player.class);
            witIsDisabledWorld = witListenerClass.getMethod("isDisabledWorld", World.class);
            witIsHidden = witListenerClass.getMethod("isHidden");
            settingsDisabled = settingsClass.getField("disabled");
            witSneakingMode = witListenerClass.getField("SNEAKING_MODE");

            witPlugin = candidate;
            blockHandler = this::handleBlock;
            apiAddBlockHandler.invoke(null, blockHandler);

            witAddonProxy = Proxy.newProxyInstance(
                    loader,
                    new Class<?>[] {witPluginClass},
                    (proxy, method, args) -> {
                        return switch (method.getName()) {
                            case "onWITReload" -> {
                                reRegisterHandler();
                                yield null;
                            }
                            case "toString" -> "SF_SlimeHUD WIT bridge";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                            default -> null;
                        };
                    });
            apiRegisterAddon.invoke(null, witAddonProxy);

            hooked = true;
            plugin.getLogger().info(
                    "What Is That? bridge enabled. WIT will display Slimefun machine data supplied by SF_SlimeHUD.");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            clearBridgeReferences();
            plugin.getLogger().log(
                    Level.WARNING,
                    "What Is That? was found, but its API could not be linked. SF_SlimeHUD will keep using its native HUD.",
                    error);
        }
    }

    private void reRegisterHandler() {
        if (!hooked || blockHandler == null || apiAddBlockHandler == null) {
            return;
        }

        try {
            apiAddBlockHandler.invoke(null, blockHandler);
        } catch (ReflectiveOperationException | RuntimeException error) {
            plugin.getLogger().log(Level.WARNING, "Could not re-register the SF_SlimeHUD handler after a WIT reload.", error);
        }
    }

    private boolean handleBlock(Block block, Player player) {
        if (!hooked || !integrationEnabled() || !isSlimeHudEnabledFor(player)) {
            return false;
        }

        try {
            SlimefunItem item = BlockStorage.check(block);
            if (item == null) {
                return false;
            }

            HudRequest request = new HudRequest(item, block.getLocation(), player);
            String name = SlimeHUD.getTranslationManager().getItemName(item);
            String info = SlimeHUD.getHudController().processRequest(request);

            Object witInfo = infoConstructor.newInstance();
            infoSetName.invoke(witInfo, legacyComponent(name));
            if (!info.isBlank()) {
                infoAddSuffix.invoke(witInfo, legacyComponent(info));
            }

            apiUpdateBar.invoke(null, witInfo, player);
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            plugin.getLogger().log(Level.FINE, "WIT could not render a Slimefun block through SF_SlimeHUD.", error);
            return false;
        }
    }

    private Component legacyComponent(String value) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(value.replace('§', '&'));
    }

    private boolean shouldDelegateInternal(Player player) {
        if (!hooked || !integrationEnabled() || !isSlimeHudEnabledFor(player)
                || witPlugin == null || !witPlugin.isEnabled()) {
            return false;
        }

        try {
            Object settings = witGetSettings.invoke(null, player);
            if (settings != null && settingsDisabled.getBoolean(settings)) {
                return false;
            }

            if ((Boolean) witIsDisabledWorld.invoke(null, player.getWorld())) {
                return false;
            }

            if ((Boolean) witIsHidden.invoke(null)) {
                return false;
            }

            return !witSneakingMode.getBoolean(null) || player.isSneaking();
        } catch (ReflectiveOperationException | RuntimeException error) {
            // Fail open to SF_SlimeHUD's native display so an integration issue
            // can never leave the player without a HUD.
            return false;
        }
    }

    private boolean isSlimeHudEnabledFor(Player player) {
        if (plugin.getConfig().getBoolean("waila.disabled", false)) {
            return false;
        }

        boolean disabledWorld = plugin.getConfig()
                .getStringList("waila.disabled-in")
                .stream()
                .anyMatch(world -> world.equalsIgnoreCase(player.getWorld().getName())
                        || world.equalsIgnoreCase(player.getWorld().getKey().toString()));
        if (disabledWorld) {
            return false;
        }

        return plugin.getPlayerData().getBoolean(player.getUniqueId() + ".waila", true);
    }

    private void unhook() {
        if (blockHandler != null && apiRemoveBlockHandler != null) {
            try {
                apiRemoveBlockHandler.invoke(null, blockHandler);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // WIT may already be part-way through disabling.
            }
        }

        if (witAddonProxy != null && apiUnregisterAddon != null) {
            try {
                apiUnregisterAddon.invoke(null, witAddonProxy);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // WIT may already be part-way through disabling.
            }
        }

        if (hooked) {
            plugin.getLogger().info("What Is That? bridge disabled; SF_SlimeHUD native display is active again.");
        }
        clearBridgeReferences();
    }

    private void clearBridgeReferences() {
        hooked = false;
        witPlugin = null;
        blockHandler = null;
        witAddonProxy = null;
        infoConstructor = null;
        infoSetName = null;
        infoAddSuffix = null;
        apiAddBlockHandler = null;
        apiRemoveBlockHandler = null;
        apiUpdateBar = null;
        apiRegisterAddon = null;
        apiUnregisterAddon = null;
        witGetSettings = null;
        witIsDisabledWorld = null;
        witIsHidden = null;
        settingsDisabled = null;
        witSneakingMode = null;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (WIT_PLUGIN_NAME.equalsIgnoreCase(event.getPlugin().getName())) {
            tryHook();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (WIT_PLUGIN_NAME.equalsIgnoreCase(event.getPlugin().getName())) {
            unhook();
        }
    }
}
