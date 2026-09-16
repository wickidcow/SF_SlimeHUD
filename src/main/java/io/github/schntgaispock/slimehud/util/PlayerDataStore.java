package io.github.schntgaispock.slimehud.util;

import io.github.schntgaispock.slimehud.SlimeHUD;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

public final class PlayerDataStore {

    private final SlimeHUD plugin;
    private final File file;
    private final YamlConfiguration config;

    public PlayerDataStore(SlimeHUD plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public synchronized String getString(String path, String def) {
        return config.getString(path, def);
    }

    public synchronized void set(String path, Object value) {
        config.set(path, value);
    }

    public synchronized void save() {
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create SlimeHUD data folder.");
                return;
            }
            config.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Could not save player.yml", ex);
        }
    }
}
