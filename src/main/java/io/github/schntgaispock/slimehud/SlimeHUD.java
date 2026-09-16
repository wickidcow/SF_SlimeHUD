package io.github.schntgaispock.slimehud;

import io.github.schntgaispock.slimehud.command.CommandManager;
import io.github.schntgaispock.slimehud.placeholder.PlaceholderManager;
import io.github.schntgaispock.slimehud.translation.TranslationManager;
import io.github.schntgaispock.slimehud.waila.HudController;
import io.github.schntgaispock.slimehud.waila.WAILAManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class SlimeHUD extends JavaPlugin {

    private static SlimeHUD instance;
    private HudController hudController;
    private TranslationManager translationManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getLogger().info("===================================");
        getLogger().info(" SF_SlimeHUD " + getDescription().getVersion());
        getLogger().info(" Slimefun Legacy + vanilla HUD");
        getLogger().info("===================================");

        hudController = new HudController();
        translationManager = new TranslationManager();

        CommandManager.setup();
        PlaceholderManager.setup();
        WAILAManager.setup();

        Metrics metrics = new Metrics(this, 15883);
        metrics.addCustomChart(new SimplePie("disabled", () -> Boolean.toString(getConfig().getBoolean("waila.disabled", false))));
        metrics.addCustomChart(new SimplePie("default_display", () -> getConfig().getString("waila.default-display", "bossbar")));
    }

    @Override
    public void onDisable() {
        WAILAManager.shutdown();
        instance = null;
    }

    public static SlimeHUD getInstance() {
        return instance;
    }

    public static HudController getHudController() {
        return instance.hudController;
    }

    public static TranslationManager getTranslationManager() {
        return instance.translationManager;
    }

    public static NamespacedKey newNamespacedKey(String name) {
        return new NamespacedKey(instance, name);
    }
}
