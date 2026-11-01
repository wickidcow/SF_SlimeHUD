package io.github.schntgaispock.slimehud.waila;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Pure decision boundary for vanilla held-tool hints. Registry-backed item tags
 * are resolved by the caller on the server, not by tests in an unbootstrapped JVM.
 */
final class HeldToolHintPolicy {

    private HeldToolHintPolicy() {}

    static boolean isContainmentTool(String id) {
        return "PICKAXE_OF_CONTAINMENT".equals(id);
    }

    static boolean allowed(String hintKey, Predicate<String> inFamily, BooleanSupplier preferredTool) {
        Objects.requireNonNull(hintKey, "hintKey");
        Objects.requireNonNull(inFamily, "inFamily");
        Objects.requireNonNull(preferredTool, "preferredTool");
        return switch (hintKey) {
            case "pickaxe", "axe", "shovel", "hoe" ->
                    inFamily.test(hintKey) && preferredTool.getAsBoolean();
            default -> preferredTool.getAsBoolean();
        };
    }
}
