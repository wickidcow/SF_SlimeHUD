package io.github.schntgaispock.slimehud.command;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.waila.HudDisplayMode;
import io.github.schntgaispock.slimehud.waila.HudPreferences;
import io.github.schntgaispock.slimehud.waila.WAILAManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SlimeHUDCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command is player-only.");
            return true;
        }

        if (args.length == 0) {
            sendStatus(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle" -> toggle(player);
            case "display", "mode" -> setDisplay(player, args);
            case "status" -> sendStatus(player);
            default -> player.sendMessage("§a§lSF_SlimeHUD§7> §fUse /slimehud toggle or /slimehud display <bossbar|actionbar>");
        }
        return true;
    }

    private void toggle(Player player) {
        if (!player.hasPermission("slimehud.toggle")) {
            player.sendMessage("§a§lSF_SlimeHUD§7> §cYou do not have permission to toggle the HUD.");
            return;
        }
        if (SlimeHUD.getInstance().getConfig().getBoolean("waila.disabled", false)) {
            player.sendMessage("§a§lSF_SlimeHUD§7> §cThe HUD is disabled server-wide.");
            return;
        }
        boolean enabled = !HudPreferences.isEnabled(player);
        HudPreferences.setEnabled(player, enabled);
        WAILAManager.getInstance().refresh(player);
        player.sendMessage("§a§lSF_SlimeHUD§7> HUD " + (enabled ? "§aenabled" : "§cdisabled") + "§7.");
    }

    private void setDisplay(Player player, String[] args) {
        if (!player.hasPermission("slimehud.display")) {
            player.sendMessage("§a§lSF_SlimeHUD§7> §cYou do not have permission to change display mode.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§a§lSF_SlimeHUD§7> Current display: §f" + HudPreferences.getDisplayMode(player).configName());
            return;
        }
        HudDisplayMode mode = HudDisplayMode.parse(args[1], null);
        if (mode == null) {
            player.sendMessage("§a§lSF_SlimeHUD§7> §cChoose bossbar or actionbar.");
            return;
        }
        HudPreferences.setDisplayMode(player, mode);
        WAILAManager.getInstance().refresh(player);
        player.sendMessage("§a§lSF_SlimeHUD§7> Display set to §f" + mode.configName() + "§7.");
    }

    private void sendStatus(Player player) {
        player.sendMessage(
                "",
                "§a§lSF_SlimeHUD §7- §f" + SlimeHUD.getInstance().getDescription().getVersion(),
                "§7HUD: " + (HudPreferences.isEnabled(player) ? "§aenabled" : "§cdisabled"),
                "§7Display: §f" + HudPreferences.getDisplayMode(player).configName(),
                "§7/slimehud toggle",
                "§7/slimehud display <bossbar|actionbar>",
                "");
    }
}
