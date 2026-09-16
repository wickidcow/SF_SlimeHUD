package io.github.schntgaispock.slimehud;

import io.github.schntgaispock.slimehud.command.CommandManager;
import io.github.schntgaispock.slimehud.placeholder.PlaceholderManager;
import io.github.schntgaispock.slimehud.translation.TranslationManager;
import io.github.schntgaispock.slimehud.util.PlayerDataStore;
import io.github.schntgaispock.slimehud.waila.HudController;
import io.github.schntgaispock.slimehud.waila.WAILAManager;
import java.util.logging.Level;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SlimeHUD extends JavaPlugin {

    private static SlimeHUD instance;

    private PlayerDataStore playerData;
    private HudController hudController;
    private TranslationManager translationManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        playerData = new PlayerDataStore(this);
        translationManager = new TranslationManager();
        hudController = new HudController();

        WAILAManager.setup();
        CommandManager.setup();
        PlaceholderManager.setup();

        getLogger().info("SF_SlimeHUD 2.0.1 enabled with Slimefun Legacy and vanilla block HUD support.");
        getLogger().info("Paper/Purpur/Leaf/Folia compatibility target: Minecraft 1.21.11+.");
    }

    @Override
    public void onDisable() {
        WAILAManager.shutdown();
        if (playerData != null) {
            playerData.save();
        }
        instance = null;
    }

    public static SlimeHUD getInstance() {
        return instance;
    }

    public PlayerDataStore getPlayerData() {
        return playerData;
    }

    public static HudController getHudController() {
        return instance.hudController;
    }

    public static TranslationManager getTranslationManager() {
        return instance.translationManager;
    }

    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    public static NamespacedKey newNamespacedKey(String name) {
        return new NamespacedKey(instance, name);
    }

    public static void log(Level level, String... messages) {
        if (instance == null) {
            return;
        }
        for (String message : messages) {
            instance.getLogger().log(level, message);
        }
    }
}
