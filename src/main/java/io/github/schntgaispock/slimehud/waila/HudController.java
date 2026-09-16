package io.github.schntgaispock.slimehud.waila;

import io.github.schntgaispock.slimehud.SlimeHUD;
import io.github.schntgaispock.slimehud.util.HudBuilder;
import io.github.schntgaispock.slimehud.util.Util;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.network.Network;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import io.github.thebusybiscuit.slimefun4.core.networks.cargo.CargoNet;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNet;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.items.cargo.CargoConnectorNode;
import io.github.thebusybiscuit.slimefun4.implementation.items.cargo.CargoManager;
import io.github.thebusybiscuit.slimefun4.implementation.items.cargo.CargoNode;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.EnergyConnector;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.EnergyRegulator;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.generators.SolarGenerator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AGenerator;
import org.bukkit.Location;
import org.bukkit.World;

public final class HudController {

    private final Map<Class<?>, Function<HudRequest, String>> defaultHandlers = new LinkedHashMap<>();
    private final Map<Class<?>, Function<HudRequest, String>> customHandlers = new LinkedHashMap<>();

    public HudController() {
        registerDefaultHandler(MachineProcessHolder.class, this::processMachine);
        registerDefaultHandler(AGenerator.class, this::processGenerator);
        registerDefaultHandler(SolarGenerator.class, this::processSolarGenerator);
        registerDefaultHandler(EnergyRegulator.class, this::processEnergyNode);
        registerDefaultHandler(EnergyConnector.class, this::processEnergyNode);
        registerDefaultHandler(EnergyNetComponent.class, this::processCapacitor);
        registerDefaultHandler(CargoNode.class, this::processCargoNode);
        registerDefaultHandler(CargoConnectorNode.class, this::processCargoManagerConnector);
        registerDefaultHandler(CargoManager.class, this::processCargoManagerConnector);
    }

    private String processEnergyNode(HudRequest request) {
        if (!config("waila.slimefun.show-energy-size")) {
            return "";
        }
        Network network = EnergyNet.getNetworkFromLocation(request.getLocation());
        int size = networkSize(network);
        return size < 0 ? "" : "Network: " + HudBuilder.getCommaNumber(size) + " nodes";
    }

    private String processCapacitor(HudRequest request) {
        if (!config("waila.slimefun.show-energy-stored")) {
            return "";
        }
        EnergyNetComponent component = (EnergyNetComponent) request.getSlimefunItem();
        EnergyNetComponentType type = component.getEnergyComponentType();
        long capacity = longEnergyValue(component, "getCapacityLong", null, component.getCapacity());
        if ((type == EnergyNetComponentType.CAPACITOR || type == EnergyNetComponentType.GENERATOR || type == EnergyNetComponentType.CONSUMER)
                && capacity > 0) {
            long fallbackCharge = component.getCharge(request.getLocation());
            long charge = longEnergyValue(component, "getChargeLong", request.getLocation(), fallbackCharge);
            return HudBuilder.formatEnergyStored(charge, capacity);
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String processMachine(HudRequest request) {
        if (!config("waila.slimefun.show-machine-progress")) {
            return "";
        }
        MachineProcessHolder<MachineOperation> machine = (MachineProcessHolder<MachineOperation>) request.getSlimefunItem();
        MachineOperation operation = machine.getMachineProcessor().getOperation(request.getLocation());
        if (operation == null) {
            String energy = request.getSlimefunItem() instanceof EnergyNetComponent ? processCapacitor(request) : "";
            return energy.isEmpty() ? "Idle" : "Idle &7| " + energy;
        }
        String text = HudBuilder.formatProgressBar(operation.getProgress(), operation.getTotalTicks());
        if (request.getSlimefunItem() instanceof AGenerator) {
            String generation = processGenerator(request);
            if (!generation.isEmpty()) {
                text += " &7| " + generation;
            }
        }
        return text;
    }

    private String processGenerator(HudRequest request) {
        if (!config("waila.slimefun.show-generator-generation")) {
            return "";
        }
        AGenerator generator = (AGenerator) request.getSlimefunItem();
        int generation = generator.getEnergyProduction();
        String text = generation > 0 ? HudBuilder.formatEnergyGenerated(generation) : "Not generating";
        if (generator instanceof EnergyNetComponent) {
            String energy = processCapacitor(request);
            if (!energy.isEmpty()) {
                text += " &7| " + energy;
            }
        }
        return text;
    }

    private String processSolarGenerator(HudRequest request) {
        if (!config("waila.slimefun.show-generator-generation")) {
            return "";
        }
        SolarGenerator generator = (SolarGenerator) request.getSlimefunItem();
        Location location = request.getLocation();
        World world = location.getWorld();
        int generation = 0;
        if (world.getEnvironment() == World.Environment.NORMAL && location.getBlock().getLightFromSky() >= 15) {
            long time = world.getTime();
            boolean day = !world.hasStorm() && !world.isThundering() && (time < 12300 || time > 23850);
            generation = day ? generator.getDayEnergy() : generator.getNightEnergy();
        }
        return generation > 0 ? HudBuilder.formatEnergyGenerated(generation) : "Not generating";
    }

    private String processCargoNode(HudRequest request) {
        if (!config("waila.slimefun.show-cargo-channel")) {
            return "";
        }
        CargoNode node = (CargoNode) request.getSlimefunItem();
        int channel = node.getSelectedChannel(request.getLocation().getBlock()) + 1;
        return "Channel: " + Util.getColorFromCargoChannel(channel) + channel;
    }

    private String processCargoManagerConnector(HudRequest request) {
        if (!config("waila.slimefun.show-cargo-size")) {
            return "";
        }
        Network network = CargoNet.getNetworkFromLocation(request.getLocation());
        int size = networkSize(network);
        return size < 0 ? "" : "Network: " + HudBuilder.getCommaNumber(size) + " nodes";
    }

    public String processRequest(HudRequest request) {
        Function<HudRequest, String> handler = tryGetHandler(request.getSlimefunItem());
        if (handler == null) {
            return "";
        }
        try {
            String result = handler.apply(request);
            return result == null ? "" : result;
        } catch (RuntimeException | LinkageError ex) {
            SlimeHUD.getInstance().getLogger().fine(() -> "HUD handler skipped for " + request.getSlimefunItem().getId() + ": " + ex.getMessage());
            return "";
        }
    }

    private Function<HudRequest, String> tryGetHandler(SlimefunItem item) {
        for (Map.Entry<Class<?>, Function<HudRequest, String>> entry : customHandlers.entrySet()) {
            if (entry.getKey().isInstance(item)) {
                return entry.getValue();
            }
        }
        for (Map.Entry<Class<?>, Function<HudRequest, String>> entry : defaultHandlers.entrySet()) {
            if (entry.getKey().isInstance(item)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void registerCustomHandler(Class<?> clazz, Function<HudRequest, String> handler) {
        customHandlers.put(clazz, handler);
    }

    private void registerDefaultHandler(Class<?> clazz, Function<HudRequest, String> handler) {
        defaultHandlers.put(clazz, handler);
    }

    private int networkSize(Network network) {
        if (network == null) {
            return -1;
        }

        // Slimefun Legacy exposes a public getSize(). Use it when available.
        try {
            Method method = network.getClass().getMethod("getSize");
            Object result = method.invoke(network);
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall through to the historical field-based implementation.
        }

        try {
            Field connectors = Network.class.getDeclaredField("connectorNodes");
            Field termini = Network.class.getDeclaredField("terminusNodes");
            connectors.setAccessible(true);
            termini.setAccessible(true);
            int connectorCount = ((Set<?>) connectors.get(network)).size();
            int terminusCount = ((Set<?>) termini.get(network)).size();
            return connectorCount + terminusCount + 1;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return -1;
        }
    }

    private long longEnergyValue(Object target, String methodName, Location location, long fallback) {
        try {
            Method method = location == null
                    ? target.getClass().getMethod(methodName)
                    : target.getClass().getMethod(methodName, Location.class);
            Object result = location == null ? method.invoke(target) : method.invoke(target, location);
            if (result instanceof Number number) {
                return number.longValue();
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // The older Slimefun API uses int energy methods, supplied as fallback.
        }
        return fallback;
    }

    private boolean config(String path) {
        var config = SlimeHUD.getInstance().getConfig();
        if (config.contains(path)) {
            return config.getBoolean(path);
        }
        if (path.startsWith("waila.slimefun.")) {
            String legacyPath = "waila." + path.substring("waila.slimefun.".length());
            if (config.contains(legacyPath)) {
                return config.getBoolean(legacyPath);
            }
        }
        return true;
    }
}
