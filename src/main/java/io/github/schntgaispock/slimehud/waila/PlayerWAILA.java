package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.util.Util;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public final class PlayerWAILA {

    private final Player player;
    private final BossBar bossBar;
    private final boolean autoBossBarColor;
    private final boolean keepTextColors;
    private SchedulerAdapter.TaskHandle task;
    private boolean paused;
    private String facing = "";
    private String facingBlock = "";
    private String facingBlockInfo = "";
    private String previousBossText = "";
    private HudDisplayMode previousMode;

    public PlayerWAILA(Player player) {
        this.player = player;
        String configuredColor = SlimeHUD.getInstance().getConfig().getString("waila.bossbar-color", "inherit");
        this.autoBossBarColor = "inherit".equalsIgnoreCase(configuredColor);
        this.keepTextColors = SlimeHUD.getInstance().getConfig().getBoolean("waila.use-original-colors", true);
        this.bossBar = Bukkit.createBossBar("", Util.pickBarColorFromColor(configuredColor), BarStyle.SOLID);
        this.bossBar.addPlayer(player);
        this.bossBar.setVisible(false);
    }

    public void start() {
        if (task == null || task.isCancelled()) {
            long rate = Math.max(1L, SlimeHUD.getInstance().getConfig().getLong("waila.tick-rate", 10L));
            task = SchedulerAdapter.runPlayerRepeating(player, rate, this::update);
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        bossBar.setVisible(false);
        bossBar.removeAll();
    }

    public void update() {
        if (!player.isOnline() || paused || !HudPreferences.isEnabled(player) || isWorldDisabled()) {
            clearFacing();
            hideDisplay();
            return;
        }

        updateFacing();
        HudDisplayMode mode = HudPreferences.getDisplayMode(player);
        if (previousMode != mode) {
            bossBar.setVisible(false);
            previousBossText = "";
            previousMode = mode;
        }

        if (facing.isEmpty()) {
            hideDisplay();
            return;
        }

        if (mode == HudDisplayMode.ACTIONBAR) {
            bossBar.setVisible(false);
            player.sendActionBar(toComponent(facing));
            return;
        }

        if (!facing.equals(previousBossText)) {
            String title = keepTextColors ? facing : ChatColor.stripColor(facing);
            bossBar.setTitle(title == null ? "" : title);
            if (autoBossBarColor) {
                bossBar.setColor(Util.pickBarColorFromName(facing));
            }
            previousBossText = facing;
        }
        bossBar.setVisible(true);
    }

    private void updateFacing() {
        double range = Math.max(1.0D, SlimeHUD.getInstance().getConfig().getDouble("waila.range", 6.0D));
        Location eye = player.getEyeLocation();
        var direction = eye.getDirection();

        RayTraceResult blockTrace = player.getWorld().rayTraceBlocks(eye, direction, range, FluidCollisionMode.NEVER, true);
        RayTraceResult entityTrace = SlimeHUD.getInstance().getConfig().getBoolean("waila.entities.enabled", true)
                ? player.getWorld().rayTraceEntities(eye, direction, range, 0.15D, entity -> entity != player)
                : null;

        double blockDistance = blockTrace == null || blockTrace.getHitPosition() == null
                ? Double.MAX_VALUE
                : blockTrace.getHitPosition().distance(eye.toVector());
        double entityDistance = entityTrace == null || entityTrace.getHitPosition() == null
                ? Double.MAX_VALUE
                : entityTrace.getHitPosition().distance(eye.toVector());

        if (entityTrace != null && entityTrace.getHitEntity() != null && entityDistance < blockDistance) {
            updateEntity(entityTrace.getHitEntity());
            return;
        }

        if (blockTrace == null || blockTrace.getHitBlock() == null) {
            clearFacing();
            return;
        }
        updateBlock(blockTrace.getHitBlock());
    }

    private void updateBlock(Block block) {
        SlimefunItem slimefunItem = SlimefunResolver.at(block.getLocation());
        if (slimefunItem != null) {
            HudRequest request = new HudRequest(slimefunItem, block.getLocation(), player);
            facingBlock = SlimeHUD.getTranslationManager().getItemName(player, slimefunItem);
            facingBlockInfo = SlimeHUD.getHudController().processRequest(request);
            facing = format(facingBlock, facingBlockInfo);
            return;
        }

        if (!SlimeHUD.getInstance().getConfig().getBoolean("waila.vanilla.enabled", true)) {
            clearFacing();
            return;
        }

        VanillaHudInfo.HudText text = VanillaHudInfo.forBlock(player, block);
        facingBlock = text.name();
        facingBlockInfo = text.details();
        facing = format("&f" + facingBlock, facingBlockInfo);
    }

    private void updateEntity(Entity entity) {
        VanillaHudInfo.HudText text = VanillaHudInfo.forEntity(entity);
        facingBlock = text.name();
        facingBlockInfo = text.details();
        facing = format("&f" + facingBlock, facingBlockInfo);
    }

    private String format(String name, String info) {
        String raw = name + (info == null || info.isEmpty() ? "" : " &7| " + info);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    private Component toComponent(String text) {
        String value = keepTextColors ? text : ChatColor.stripColor(text);
        return LegacyComponentSerializer.legacySection().deserialize(value == null ? "" : value);
    }

    private void hideDisplay() {
        bossBar.setVisible(false);
        if (previousMode == HudDisplayMode.ACTIONBAR) {
            player.sendActionBar(Component.empty());
        }
    }

    private boolean isWorldDisabled() {
        return SlimeHUD.getInstance().getConfig().getStringList("waila.disabled-in").contains(player.getWorld().getName());
    }

    private void clearFacing() {
        facing = "";
        facingBlock = "";
        facingBlockInfo = "";
        previousBossText = "";
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            clearFacing();
            hideDisplay();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public String getFacing() {
        return facing;
    }

    public String getFacingBlock() {
        return facingBlock;
    }

    public String getFacingBlockInfo() {
        return facingBlockInfo;
    }

    public boolean isPaused() {
        return paused;
    }
}
