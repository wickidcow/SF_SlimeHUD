package io.github.schntgaispock.slimehud.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class SlimeHUDTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            addMatching(suggestions, args[0], "toggle", "display", "status", "bossbar", "actionbar");
            if (sender.hasPermission("slimehud.reload")) {
                addMatching(suggestions, args[0], "reload");
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("display") || args[0].equalsIgnoreCase("type"))) {
            addMatching(suggestions, args[1], "bossbar", "actionbar");
        }

        return suggestions;
    }

    private void addMatching(List<String> output, String input, String... values) {
        String prefix = input.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value.startsWith(prefix)) {
                output.add(value);
            }
        }
    }
}
