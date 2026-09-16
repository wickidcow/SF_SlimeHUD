package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class VanillaHudInfo {

    private VanillaHudInfo() {}

    public static HudText forBlock(Player player, Block block) {
        String name = prettify(block.getType());
        List<String> details = new ArrayList<>();

        if (enabled("waila.vanilla.show-container-fill") && block.getState() instanceof Container container) {
            Inventory inventory = container.getInventory();
            int used = 0;
            int items = 0;
            for (ItemStack stack : inventory.getStorageContents()) {
                if (stack != null && !stack.getType().isAir()) {
                    used++;
                    items += stack.getAmount();
                }
            }
            details.add("Slots: " + used + "/" + inventory.getStorageContents().length + " (" + items + " items)");
        }

        if (enabled("waila.vanilla.show-redstone") && (block.isBlockPowered() || block.isBlockIndirectlyPowered() || block.getBlockPower() > 0)) {
            details.add("Power: " + block.getBlockPower() + "/15");
        }

        if (enabled("waila.vanilla.show-block-state")) {
            var data = block.getBlockData();
            if (data instanceof org.bukkit.block.data.Ageable ageable) {
                details.add("Growth: " + ageable.getAge() + "/" + ageable.getMaximumAge());
            }
            if (data instanceof Levelled levelled) {
                details.add("Level: " + levelled.getLevel() + "/" + levelled.getMaximumLevel());
            }
            if (data instanceof Powerable powerable) {
                details.add(powerable.isPowered() ? "Powered" : "Unpowered");
            }
            if (data instanceof Lightable lightable) {
                details.add(lightable.isLit() ? "Lit" : "Unlit");
            }
            if (data instanceof Openable openable) {
                details.add(openable.isOpen() ? "Open" : "Closed");
            }
            if (block.getState() instanceof Beehive hive) {
                details.add("Bees: " + hive.getEntityCount() + "/" + hive.getMaxEntities());
            }
            if (block.getState() instanceof CreatureSpawner spawner) {
                details.add("Spawns: " + prettify(spawner.getSpawnedType().name()));
            }
        }

        if (enabled("waila.vanilla.show-tool-hint")) {
            ItemStack held = player.getInventory().getItemInMainHand();
            if (!held.getType().isAir()) {
                details.add(block.isPreferredTool(held) ? "Tool: effective" : "Tool: not preferred");
            }
        }

        if (enabled("waila.vanilla.show-hardness")) {
            float hardness = block.getType().getHardness();
            if (hardness >= 0) {
                details.add("Hardness: " + trimFloat(hardness));
            } else {
                details.add("Unbreakable");
            }
        }

        return new HudText(name, String.join(" &7| ", details));
    }

    public static HudText forEntity(Entity entity) {
        String name = entity.customName() == null ? prettify(entity.getType().name()) : net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(entity.customName());
        List<String> details = new ArrayList<>();

        if (entity instanceof LivingEntity living && enabled("waila.entities.show-health")) {
            var maxHealthAttribute = living.getAttribute(Attribute.MAX_HEALTH);
            double max = maxHealthAttribute == null ? living.getHealth() : maxHealthAttribute.getValue();
            details.add("Health: " + trimFloat((float) living.getHealth()) + "/" + trimFloat((float) max));
        }
        if (entity instanceof Villager villager && enabled("waila.entities.show-villager-info")) {
            details.add("Profession: " + prettify(villager.getProfession().name()));
            details.add("Level: " + villager.getVillagerLevel());
        }
        if (entity instanceof Item item && enabled("waila.entities.show-item-stack")) {
            ItemStack stack = item.getItemStack();
            details.add("Count: " + stack.getAmount());
            name = prettify(stack.getType());
        }

        return new HudText(name, String.join(" &7| ", details));
    }

    public static String prettify(Material material) {
        return prettify(material.name());
    }

    public static String prettify(String value) {
        String[] words = value.toLowerCase(Locale.ROOT).split("_");
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

    private static boolean enabled(String path) {
        return SlimeHUD.getInstance().getConfig().getBoolean(path, true);
    }

    private static String trimFloat(float value) {
        return Math.abs(value - Math.round(value)) < 0.001F ? Integer.toString(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
    }

    public record HudText(String name, String details) {
        public String combined() {
            return details == null || details.isEmpty() ? name : name + " &7| " + details;
        }
    }
}
