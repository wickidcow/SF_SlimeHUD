package io.github.schntgaispock.slimehud.waila;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Tests the exact policy called by VanillaInfoProvider without loading Bukkit's
 * registry-based Material/Tag classes into a serverless Surefire JVM.
 * Real Paper/Purpur/Leaf/Folia server boot coverage is a separate CI gate.
 */
class VanillaInfoProviderToolHintTest {

    @Test
    void dirtWithStickDoesNotClaimHeldShovelEvenIfDirtDoesNotNeedATool() {
        AtomicInteger preferredCalls = new AtomicInteger();
        assertFalse(HeldToolHintPolicy.allowed("shovel", family -> false, () -> {
            preferredCalls.incrementAndGet();
            return true;
        }));
        assertEquals(0, preferredCalls.get(), "Wrong family must short-circuit Bukkit preferred-tool check");
    }

    @Test
    void dirtWithActualShovelIsAccepted() {
        assertTrue(HeldToolHintPolicy.allowed("shovel", family -> family.equals("shovel"), () -> true));
    }

    @Test
    void stoneWithAnAxeIsRejectedEvenWhenPreferredToolWouldReturnTrue() {
        assertFalse(HeldToolHintPolicy.allowed("pickaxe", family -> false, () -> true));
    }

    @Test
    void rightFamilyCannotOverrideRequiredMiningTier() {
        assertFalse(HeldToolHintPolicy.allowed("pickaxe", family -> true, () -> false),
                "The Bukkit preferred-tool tier check must still be required");
        assertTrue(HeldToolHintPolicy.allowed("pickaxe", family -> true, () -> true));
    }

    @Test
    void containmentItemMustHaveExactCanonicalSlimefunId() {
        assertTrue(HeldToolHintPolicy.isContainmentTool("PICKAXE_OF_CONTAINMENT"));
        assertFalse(HeldToolHintPolicy.isContainmentTool("PICKAXE"));
        assertFalse(HeldToolHintPolicy.isContainmentTool(null));
        assertFalse(HeldToolHintPolicy.isContainmentTool("REPAIRED_SPAWNER"));
    }

    @Test
    void allFourToolFamiliesUseTheirOwnTagKeyAndUnknownHintsPreserveFallback() {
        for (String family : new String[] {"pickaxe", "axe", "shovel", "hoe"}) {
            assertTrue(HeldToolHintPolicy.allowed(family, candidate -> candidate.equals(family), () -> true));
            assertFalse(HeldToolHintPolicy.allowed(family, candidate -> !candidate.equals(family), () -> true));
        }
        AtomicInteger familyChecks = new AtomicInteger();
        assertTrue(HeldToolHintPolicy.allowed("other", candidate -> {
            familyChecks.incrementAndGet();
            return false;
        }, () -> true));
        assertEquals(0, familyChecks.get(), "Legacy unknown hint fallback must not query item tags");
    }
}
