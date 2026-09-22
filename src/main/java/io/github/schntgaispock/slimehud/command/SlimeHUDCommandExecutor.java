package io.github.schntgaispock.slimehud.command;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.integration.WITIntegration;
import io.github.schntgaispock.slimehud.waila.DisplayMode;
import io.github.schntgaispock.slimehud.waila.PlayerWAILA;
import io.github.schntgaispock.slimehud.waila.WAILAManager;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SlimeHUDCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("slimehud.reload")) {
                sender.sendMessage("§a§lSlimeHUD§7> §cYou do not have permission to reload SlimeHUD.");
                return true;
            }
            SlimeHUD.getInstance().reloadConfig();
            WAILAManager.getInstance().reloadAll();
            sender.sendMessage("§a§lSlimeHUD§7> §aConfiguration reloaded.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("SlimeHUD player settings can only be changed in game.");
            return true;
        }

        if (args.length == 0) {
            sendInfo(player);
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "toggle" -> toggle(player);
            case "display", "type" -> changeDisplay(player, args);
            case "bossbar" -> setDisplay(player, DisplayMode.BOSSBAR);
            case "actionbar", "hotbar" -> setDisplay(player, DisplayMode.ACTIONBAR);
            case "status" -> status(player);
            default -> {
                sendInfo(player);
                yield true;
            }
        };
    }

    private boolean toggle(Player player) {
        if (!player.hasPermission("slimehud.togglewaila")) {
            player.sendMessage("§a§lSlimeHUD§7> §cYou do not have permission to toggle the HUD.");
            return true;
        }
        if (SlimeHUD.getInstance().getConfig().getBoolean("waila.disabled", false)) {
            player.sendMessage("§a§lSlimeHUD§7> §cThe HUD is disabled globally.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        boolean enabled = SlimeHUD.getInstance().getPlayerData().getBoolean(uuid + ".waila", true);
        SlimeHUD.getInstance().getPlayerData().set(uuid + ".waila", !enabled);
        SlimeHUD.getInstance().getPlayerData().save();
        WAILAManager.getInstance().refreshPlayer(player);

        player.sendMessage("§a§lSlimeHUD§7> HUD toggled " + (enabled ? "§coff" : "§aon") + "§7.");
        return true;
    }

    private boolean changeDisplay(Player player, String[] args) {
        if (!player.hasPermission("slimehud.display")) {
            player.sendMessage("§a§lSlimeHUD§7> §cYou do not have permission to change the display type.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("§a§lSlimeHUD§7> §7Use §f/slimehud display bossbar §7or §f/slimehud display actionbar§7.");
            return true;
        }

        DisplayMode mode = DisplayMode.from(args[1], null);
        if (mode == null) {
            player.sendMessage("§a§lSlimeHUD§7> §cUnknown display type. Use bossbar or actionbar.");
            return true;
        }
        return setDisplay(player, mode);
    }

    private boolean setDisplay(Player player, DisplayMode mode) {
        if (!player.hasPermission("slimehud.display")) {
            player.sendMessage("§a§lSlimeHUD§7> §cYou do not have permission to change the display type.");
            return true;
        }

        SlimeHUD.getInstance().getPlayerData().set(player.getUniqueId() + ".display", mode.id());
        SlimeHUD.getInstance().getPlayerData().save();
        WAILAManager.getInstance().refreshPlayer(player);

        PlayerWAILA waila = WAILAManager.getInstance().getWaila(player);
        if (waila != null) {
            waila.setDisplayMode(mode);
        }

        String pretty = mode == DisplayMode.BOSSBAR ? "BossBar" : "ActionBar";
        player.sendMessage("§a§lSlimeHUD§7> Display set to §f" + pretty + "§7.");
        if (WITIntegration.shouldDelegate(player)) {
            player.sendMessage("§a§lSlimeHUD§7> §7WIT currently owns the visible HUD; use WIT's display setting while its bridge is active.");
        }
        return true;
    }

    private boolean status(Player player) {
        boolean enabled = SlimeHUD.getInstance().getPlayerData().getBoolean(player.getUniqueId() + ".waila", true);
        String defaultDisplay = SlimeHUD.getInstance().getConfig().getString(
                "waila.default-display",
                SlimeHUD.getInstance().getConfig().getString("waila.location", "bossbar"));
        DisplayMode mode = DisplayMode.from(
                SlimeHUD.getInstance().getPlayerData().getString(player.getUniqueId() + ".display", defaultDisplay),
                DisplayMode.BOSSBAR);

        player.sendMessage("§a§lSlimeHUD§7> HUD: " + (enabled ? "§aenabled" : "§cdisabled")
                + "§7, display: §f" + mode.id() + "§7.");
        player.sendMessage("§a§lSlimeHUD§7> WIT bridge: "
                + (WITIntegration.shouldDelegate(player) ? "§aactive §7(WIT owns display)" :
                WITIntegration.isHooked() ? "§eavailable §7(SlimeHUD currently owns display)" : "§8not active"));
        return true;
    }

    private void sendInfo(Player player) {
        player.sendMessage(
                "",
                "§a§lSF_SlimeHUD §7- §fVersion " + SlimeHUD.getInstance().getPluginVersion(),
                "§7Slimefun Legacy + vanilla block HUD",
                "§f/slimehud toggle §7- enable/disable your HUD",
                "§f/slimehud display bossbar §7- use BossBar",
                "§f/slimehud display actionbar §7- use ActionBar",
                "§f/slimehud status §7- show HUD and WIT bridge status",
                "");
    }
}
