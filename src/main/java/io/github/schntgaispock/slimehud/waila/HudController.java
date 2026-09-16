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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class HudController {

    private final Map<Class<?>, Function<HudRequest, String>> defaultHandlers = new LinkedHashMap<>();
    private final Map<Class<?>, Function<HudRequest, String>> customHandlers = new LinkedHashMap<>();

    public HudController() {
        registerDefaultHandler(MachineProcessHolder.class, this::processMachine);
        registerDefaultHandler(EnergyRegulator.class, this::processEnergyNode);
        registerDefaultHandler(EnergyConnector.class, this::processEnergyNode);
        registerDefaultHandler(EnergyNetComponent.class, this::processCapacitor);
        registerDefaultHandler(CargoNode.class, this::processCargoNode);
        registerDefaultHandler(CargoConnectorNode.class, this::processCargoManagerConnector);
        registerDefaultHandler(CargoManager.class, this::processCargoManagerConnector);
    }

    private String processEnergyNode(HudRequest request) {
        if (!SlimeHUD.getInstance().getConfig().getBoolean("waila.show-energy-size", true)) {
            return "";
        }
        Network network = EnergyNet.getNetworkFromLocation(request.getLocation());
        int size = getNetworkSize(network);
        return size < 0 ? "" : "Energy Network: " + HudBuilder.getCommaNumber(size) + " nodes";
    }

    private String processCapacitor(HudRequest request) {
        if (!SlimeHUD.getInstance().getConfig().getBoolean("waila.show-energy-stored", true)) {
            return "";
        }

        EnergyNetComponent component = (EnergyNetComponent) request.getSlimefunItem();
        EnergyNetComponentType type = component.getEnergyComponentType();
        if ((type == EnergyNetComponentType.CAPACITOR
                        || type == EnergyNetComponentType.GENERATOR
                        || type == EnergyNetComponentType.CONSUMER)
                && component.getCapacity() > 0) {
            return HudBuilder.formatEnergyStored(component.getCharge(request.getLocation()), component.getCapacity());
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String processMachine(HudRequest request) {
        if (!SlimeHUD.getInstance().getConfig().getBoolean("waila.show-machine-progress", true)) {
            return "";
        }

        StringBuilder text = new StringBuilder();
        MachineProcessHolder<MachineOperation> machine =
                (MachineProcessHolder<MachineOperation>) request.getSlimefunItem();
        MachineOperation operation = machine.getMachineProcessor().getOperation(request.getLocation());

        if (operation == null) {
            text.append("Idle");
        } else {
            text.append(HudBuilder.formatProgressBar(operation.getProgress(), operation.getTotalTicks()));
        }

        if (SlimeHUD.getInstance().getConfig().getBoolean("waila.show-generator-generation", true)) {
            String generation = getGeneratorInfo(request.getSlimefunItem());
            if (!generation.isEmpty()) {
                text.append(" &7| ").append(generation);
            }
        }

        if (request.getSlimefunItem() instanceof EnergyNetComponent) {
            String stored = processCapacitor(request);
            if (!stored.isEmpty()) {
                text.append(" &7| ").append(stored);
            }
        }

        return text.toString();
    }

    private String getGeneratorInfo(SlimefunItem item) {
        Number production = invokeNumber(item, "getEnergyProduction");
        if (production != null && production.longValue() > 0) {
            return HudBuilder.formatEnergyGenerated(production.longValue());
        }

        Number day = invokeNumber(item, "getDayEnergy");
        Number night = invokeNumber(item, "getNightEnergy");
        if (day != null && day.longValue() > 0) {
            String result = "&e☀&7 " + HudBuilder.getAbbreviatedNumber(day.longValue()) + " J/t";
            if (night != null && night.longValue() > 0) {
                result += " &8/ &9☾&7 " + HudBuilder.getAbbreviatedNumber(night.longValue()) + " J/t";
            }
            return result;
        }
        return "";
    }

    private Number invokeNumber(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value instanceof Number number ? number : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private String processCargoNode(HudRequest request) {
        if (!SlimeHUD.getInstance().getConfig().getBoolean("waila.show-cargo-channel", true)) {
            return "";
        }
        CargoNode node = (CargoNode) request.getSlimefunItem();
        int channel = node.getSelectedChannel(request.getLocation().getBlock()) + 1;
        return "Channel: " + Util.getColorFromCargoChannel(channel) + channel;
    }

    private String processCargoManagerConnector(HudRequest request) {
        if (!SlimeHUD.getInstance().getConfig().getBoolean("waila.show-cargo-size", true)) {
            return "";
        }
        Network network = CargoNet.getNetworkFromLocation(request.getLocation());
        int size = getNetworkSize(network);
        return size < 0 ? "" : "Cargo Network: " + HudBuilder.getCommaNumber(size) + " nodes";
    }

    private int getNetworkSize(Network network) {
        if (network == null) {
            return -1;
        }
        try {
            Field connectorNodes = Network.class.getDeclaredField("connectorNodes");
            Field terminusNodes = Network.class.getDeclaredField("terminusNodes");
            connectorNodes.setAccessible(true);
            terminusNodes.setAccessible(true);
            int connectorCount = ((Set<?>) connectorNodes.get(network)).size();
            int terminusCount = ((Set<?>) terminusNodes.get(network)).size();
            return connectorCount + terminusCount + 1;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return -1;
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

    public String processRequest(HudRequest request) {
        Function<HudRequest, String> handler = tryGetHandler(request.getSlimefunItem());
        if (handler == null) {
            return "";
        }
        String result = handler.apply(request);
        return result == null ? "" : result;
    }

    private void registerDefaultHandler(Class<?> type, Function<HudRequest, String> handler) {
        defaultHandlers.put(type, handler);
    }

    public void registerCustomHandler(Class<?> type, Function<HudRequest, String> handler) {
        customHandlers.put(type, handler);
    }
}
