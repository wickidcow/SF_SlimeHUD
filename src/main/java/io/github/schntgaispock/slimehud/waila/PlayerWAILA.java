package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.util.Util;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public final class PlayerWAILA {

    private final Player player;
    private final BossBar bossBar;
    private final boolean useAutoBossBarColor;
    private final boolean keepTextColors;

    private ScheduledTask task;
    private DisplayMode displayMode;
    private boolean paused;
    private boolean displayVisible;
    private String facing = "";
    private String facingBlock = "";
    private String facingBlockInfo = "";

    public PlayerWAILA(Player player) {
        this.player = player;

        String configuredColor = SlimeHUD.getInstance().getConfig().getString("waila.bossbar-color", "inherit");
        this.useAutoBossBarColor = "inherit".equalsIgnoreCase(configuredColor);
        this.keepTextColors = SlimeHUD.getInstance().getConfig().getBoolean("waila.use-original-colors", true);
        this.bossBar = Bukkit.createBossBar("", Util.pickBarColorFromColor(configuredColor), BarStyle.SOLID);
        this.bossBar.addPlayer(player);
        this.bossBar.setVisible(false);

        String defaultDisplay = SlimeHUD.getInstance().getConfig().getString(
                "waila.default-display",
                SlimeHUD.getInstance().getConfig().getString("waila.location", "bossbar"));
        String storedDisplay = SlimeHUD.getInstance().getPlayerData().getString(
                player.getUniqueId() + ".display", defaultDisplay);
        this.displayMode = DisplayMode.from(storedDisplay, DisplayMode.BOSSBAR);
    }

    public void start() {
        if (task != null) {
            return;
        }

        long period = Math.max(1L, SlimeHUD.getInstance().getConfig().getLong("waila.tick-rate", 5L));
        task = player.getScheduler().runAtFixedRate(
                SlimeHUD.getInstance(),
                scheduledTask -> update(),
                this::clearDisplay,
                1L,
                period);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        clearDisplay();
        bossBar.removeAll();
    }

    private void update() {
        if (paused) {
            return;
        }
        if (!player.isOnline() || !player.isValid()) {
            clearDisplay();
            return;
        }

        updateFacing();

        if (displayMode == DisplayMode.BOSSBAR) {
            showBossBar();
        } else {
            showActionBar();
        }
    }

    private void updateFacing() {
        int maxDistance = Math.max(1, SlimeHUD.getInstance().getConfig().getInt("waila.max-distance", 8));
        boolean showItems = SlimeHUD.getInstance().getConfig().getBoolean("items.enabled", true);

        Location eye = player.getEyeLocation();
        RayTraceResult trace = player.getWorld().rayTrace(
                eye,
                eye.getDirection(),
                maxDistance,
                FluidCollisionMode.NEVER,
                true,
                0.2D,
                entity -> showItems && entity instanceof Item);

        if (trace == null) {
            clearFacing();
            return;
        }

        if (trace.getHitEntity() instanceof Item droppedItem) {
            ItemInfoProvider.ItemHud itemHud = ItemInfoProvider.describe(
                    player, droppedItem.getItemStack(), SlimeHUD.getInstance().getConfig());
            facingBlock = itemHud.name();
            facingBlockInfo = itemHud.info();
            buildFacingText();
            return;
        }

        Block targetBlock = trace.getHitBlock();
        if (targetBlock == null || targetBlock.getType().isAir()) {
            clearFacing();
            return;
        }

        SlimefunItem slimefunItem = BlockStorage.check(targetBlock);
        if (slimefunItem != null) {
            Location target = targetBlock.getLocation();
            HudRequest request = new HudRequest(slimefunItem, target, player);
            facingBlock = SlimeHUD.getTranslationManager().getItemName(player, slimefunItem);
            facingBlockInfo = SlimeHUD.getHudController().processRequest(request);
        } else if (SlimeHUD.getInstance().getConfig().getBoolean("vanilla.enabled", true)) {
            facingBlock = "&f" + VanillaInfoProvider.getName(targetBlock);
            facingBlockInfo = VanillaInfoProvider.getInfo(targetBlock, player, SlimeHUD.getInstance().getConfig());
        } else {
            clearFacing();
            return;
        }

        buildFacingText();
    }

    private void buildFacingText() {
        facing = ChatColor.translateAlternateColorCodes(
                '&', facingBlock + (facingBlockInfo.isEmpty() ? "" : " &7| " + facingBlockInfo));
    }

    private void showBossBar() {
        if (facing.isEmpty()) {
            clearDisplay();
            return;
        }

        bossBar.setVisible(true);
        bossBar.setTitle(keepTextColors ? facing : ChatColor.stripColor(facing));
        if (useAutoBossBarColor) {
            bossBar.setColor(Util.pickBarColorFromName(facing));
        }
        displayVisible = true;
    }

    private void showActionBar() {
        bossBar.setVisible(false);
        if (facing.isEmpty()) {
            clearDisplay();
            return;
        }

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(keepTextColors ? facing : ChatColor.stripColor(facing)));
        displayVisible = true;
    }

    private void clearDisplay() {
        bossBar.setVisible(false);
        if (!displayVisible) {
            return;
        }
        if (player.isOnline()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
        }
        displayVisible = false;
    }

    private void clearFacing() {
        facingBlock = "";
        facingBlockInfo = "";
        facing = "";
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

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            clearFacing();
            clearDisplay();
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        if (this.displayMode == displayMode) {
            return;
        }
        clearDisplay();
        this.displayMode = displayMode;
    }
}
