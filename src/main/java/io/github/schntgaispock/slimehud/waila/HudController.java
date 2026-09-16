package io.github.schntgaispock.slimehud.waila;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.AGenerator;

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
        return network == null ? "" : "Network: " + HudBuilder.getCommaNumber(network.getSize()) + " nodes";
    }

    private String processCapacitor(HudRequest request) {
        if (!config("waila.slimefun.show-energy-stored")) {
            return "";
        }
        EnergyNetComponent component = (EnergyNetComponent) request.getSlimefunItem();
        EnergyNetComponentType type = component.getEnergyComponentType();
        long capacity = component.getCapacityLong();
        if ((type == EnergyNetComponentType.CAPACITOR || type == EnergyNetComponentType.GENERATOR || type == EnergyNetComponentType.CONSUMER)
                && capacity > 0) {
            return HudBuilder.formatEnergyStored(component.getChargeLong(request.getLocation()), capacity);
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
        int generation = 0;
        var data = StorageCacheUtils.getDataContainer(request.getLocation());
        if (data != null && data.isDataLoaded() && !data.isPendingRemove()) {
            generation = generator.getGeneratedOutput(request.getLocation(), data);
        }
        String text = generation > 0 ? HudBuilder.formatEnergyGenerated(generation) : "Not generating";
        if (generator instanceof EnergyNetComponent) {
            String energy = processCapacitor(request);
            if (!energy.isEmpty()) {
                text += " &7| " + energy;
            }
        }
        return text;
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
        return network == null ? "" : "Network: " + HudBuilder.getCommaNumber(network.getSize()) + " nodes";
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

    private boolean config(String path) {
        return SlimeHUD.getInstance().getConfig().getBoolean(path, true);
    }
}
