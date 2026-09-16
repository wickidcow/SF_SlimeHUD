package io.github.schntgaispock.slimehud.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class SlimeHUDTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("toggle", "display", "status"), args[0]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("display") || args[0].equalsIgnoreCase("mode"))) {
            return filter(List.of("bossbar", "actionbar"), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.regionMatches(true, 0, input, 0, input.length())) {
                result.add(value);
            }
        }
        return result;
    }
}
