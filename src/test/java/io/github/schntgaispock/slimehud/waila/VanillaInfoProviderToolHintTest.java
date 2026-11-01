package io.github.schntgaispock.slimehud.waila;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Regression coverage for the contributor's #15 correction.
 * Block#isPreferredTool alone is not a family check on easily broken blocks.
 */
class VanillaInfoProviderToolHintTest {

    private final YamlConfiguration enabled = new YamlConfiguration();

    @Test
    void aStickCannotBeReportedAsTheHeldShovelForDirt() {
        Block dirt = preferred(Material.DIRT, true);
        assertEquals("Tool: Shovel (held ✗)",
                VanillaInfoProvider.getToolInfo(dirt, new ItemStack(Material.STICK), enabled));
    }

    @Test
    void aMatchingShovelIsAcceptedOnDirtWithoutToolRequirements() {
        Block dirt = preferred(Material.DIRT, true);
        assertEquals("Tool: Shovel (held ✓)",
                VanillaInfoProvider.getToolInfo(dirt, new ItemStack(Material.IRON_SHOVEL), enabled));
    }

    @Test
    void theWrongToolFamilyIsRejectedEvenIfTheBlockAcceptsTheItem() {
        Block stone = preferred(Material.STONE, true);
        assertEquals("Tool: Pickaxe (held ✗)",
                VanillaInfoProvider.getToolInfo(stone, new ItemStack(Material.IRON_AXE), enabled));
    }

    @Test
    void miningTierStillMattersAfterTheToolFamilyMatches() {
        Block obsidian = mock(Block.class);
        when(obsidian.getType()).thenReturn(Material.OBSIDIAN);
        when(obsidian.isPreferredTool(any(ItemStack.class))).thenAnswer(call ->
                ((ItemStack) call.getArgument(0)).getType() == Material.DIAMOND_PICKAXE);

        assertEquals("Tool: Pickaxe (Diamond+) (held ✗)",
                VanillaInfoProvider.getToolInfo(obsidian, new ItemStack(Material.IRON_PICKAXE), enabled));
        assertEquals("Tool: Pickaxe (Diamond+) (held ✓)",
                VanillaInfoProvider.getToolInfo(obsidian, new ItemStack(Material.DIAMOND_PICKAXE), enabled));
    }

    @Test
    void spawnersStillRequireTheExactSlimefunContainmentItem() {
        Block spawner = preferred(Material.SPAWNER, true);
        ItemStack plain = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemStack special = new ItemStack(Material.NETHERITE_PICKAXE);
        SlimefunItem specialItem = mock(SlimefunItem.class);
        when(specialItem.getId()).thenReturn("PICKAXE_OF_CONTAINMENT");
        try (MockedStatic<SlimefunItem> lookup = mockStatic(SlimefunItem.class)) {
            lookup.when(() -> SlimefunItem.getByItem(special)).thenReturn(specialItem);
            assertEquals("Tool: Pickaxe of Containment (held ✗)",
                    VanillaInfoProvider.getToolInfo(spawner, plain, enabled));
            assertEquals("Tool: Pickaxe of Containment (held ✓)",
                    VanillaInfoProvider.getToolInfo(spawner, special, enabled));
        }
    }

    @Test
    void disabledAndEmptyHandSettingsKeepTheirOriginalDisplay() {
        Block dirt = preferred(Material.DIRT, true);
        assertEquals("Tool: Shovel", VanillaInfoProvider.getToolInfo(dirt, null, enabled));
        assertEquals("Tool: Shovel",
                VanillaInfoProvider.getToolInfo(dirt, new ItemStack(Material.AIR), enabled));
        enabled.set("vanilla.show-tool", false);
        assertEquals("", VanillaInfoProvider.getToolInfo(dirt, new ItemStack(Material.STICK), enabled));
    }

    private static Block preferred(Material type, boolean acceptsAnyItem) {
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(type);
        when(block.isPreferredTool(any(ItemStack.class))).thenReturn(acceptsAnyItem);
        return block;
    }
}
