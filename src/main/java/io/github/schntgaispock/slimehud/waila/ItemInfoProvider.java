package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemInfoProvider {

    private ItemInfoProvider() {}

    public record ItemHud(String name, String info) {}

    public static ItemHud describe(ItemStack stack, FileConfiguration config) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(stack);
        String name;
        List<String> info = new ArrayList<>();

        if (slimefunItem != null) {
            name = SlimeHUD.getTranslationManager().getItemName(slimefunItem);
            if (config.getBoolean("items.show-slimefun-id", false)) {
                info.add("ID: " + slimefunItem.getId());
            }
        } else {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                name = meta.getDisplayName();
            } else {
                name = "&f" + humanize(stack.getType().name());
            }
        }

        if (config.getBoolean("items.show-stack-size", true) && stack.getAmount() > 1) {
            info.add("Stack: " + stack.getAmount());
        }

        return new ItemHud(name, String.join(" &8| &7", info));
    }

    private static String humanize(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).replace('-', '_').split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
