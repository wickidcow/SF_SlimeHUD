package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class WAILAManager implements Listener {

    private static final WAILAManager INSTANCE = new WAILAManager();
    private final Map<UUID, PlayerWAILA> wailas = new ConcurrentHashMap<>();

    private WAILAManager() {}

    public static WAILAManager getInstance() {
        return INSTANCE;
    }

    public static void setup() {
        Bukkit.getPluginManager().registerEvents(INSTANCE, SlimeHUD.getInstance());
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScheduler().run(SlimeHUD.getInstance(), ignored -> INSTANCE.generate(player), null);
        }
    }

    public static void shutdown() {
        INSTANCE.wailas.values().forEach(PlayerWAILA::stop);
        INSTANCE.wailas.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        generate(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    public void generate(Player player) {
        if (SlimeHUD.getInstance().getConfig().getBoolean("waila.disabled", false)) {
            return;
        }
        PlayerWAILA hud = wailas.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerWAILA(player));
        hud.setPaused(false);
        hud.start();
    }

    public void remove(Player player) {
        PlayerWAILA hud = wailas.remove(player.getUniqueId());
        if (hud != null) {
            hud.stop();
        }
    }

    public void refresh(Player player) {
        PlayerWAILA hud = wailas.get(player.getUniqueId());
        if (hud == null) {
            generate(player);
        } else {
            hud.update();
        }
    }

    public Map<UUID, PlayerWAILA> getWailas() {
        return wailas;
    }
}
