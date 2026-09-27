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
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class HudController {

    private final Map<Class<?>, Function<HudRequest, String>> defaultHandlers = new LinkedHashMap<>();
    private final Map<Class<?>, Function<HudRequest, String>> customHandlers = new LinkedHashMap<>();
    private final Set<String> warnedHandlerFailures = ConcurrentHashMap.newKeySet();

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
        EnergyNetComponent component = (EnergyNetComponent) request.getSlimefunItem();
        EnergyNetComponentType type = component.getEnergyComponentType();
        StringBuilder text = new StringBuilder();

        // Some generators (notably SolarGenerator and addon generators) are not
        // MachineProcessHolders. Preserve their HUD output through the broader
        // EnergyNetComponent path using APIs available on the oldest supported
        // Slimefun compile floor.
        if (type == EnergyNetComponentType.GENERATOR
                && SlimeHUD.getInstance().getConfig().getBoolean("waila.show-generator-generation", true)) {
            appendPart(text, getGeneratorInfo(request.getSlimefunItem()));
        }

        if (SlimeHUD.getInstance().getConfig().getBoolean("waila.show-energy-stored", true)
                && (type == EnergyNetComponentType.CAPACITOR
                        || type == EnergyNetComponentType.GENERATOR
                        || type == EnergyNetComponentType.CONSUMER)
                && component.getCapacity() > 0) {
            appendPart(
                    text,
                    HudBuilder.formatEnergyStored(
                            component.getCharge(request.getLocation()), component.getCapacity()));
        }

        return text.toString();
    }

    @SuppressWarnings("unchecked")
    private String processMachine(HudRequest request) {
        StringBuilder text = new StringBuilder();

        if (SlimeHUD.getInstance().getConfig().getBoolean("waila.show-machine-progress", true)) {
            MachineProcessHolder<MachineOperation> machine =
                    (MachineProcessHolder<MachineOperation>) request.getSlimefunItem();
            MachineOperation operation = machine.getMachineProcessor().getOperation(request.getLocation());

            if (operation == null) {
                appendPart(text, "Idle");
            } else {
                appendPart(text, HudBuilder.formatProgressBar(operation.getProgress(), operation.getTotalTicks()));
            }
        }

        if (SlimeHUD.getInstance().getConfig().getBoolean("waila.show-generator-generation", true)) {
            appendPart(text, getGeneratorInfo(request.getSlimefunItem()));
        }

        if (request.getSlimefunItem() instanceof EnergyNetComponent) {
            EnergyNetComponent component = (EnergyNetComponent) request.getSlimefunItem();
            if (SlimeHUD.getInstance().getConfig().getBoolean("waila.show-energy-stored", true)
                    && component.getCapacity() > 0) {
                appendPart(
                        text,
                        HudBuilder.formatEnergyStored(
                                component.getCharge(request.getLocation()), component.getCapacity()));
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

    private void appendPart(StringBuilder text, String part) {
        if (part == null || part.isEmpty()) {
            return;
        }
        if (!text.isEmpty()) {
            text.append(" &7| ");
        }
        text.append(part);
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
        return network == null ? -1 : network.getSize();
    }

    private Function<HudRequest, String> tryGetHandler(
            SlimefunItem item, Map<Class<?>, Function<HudRequest, String>> handlers) {
        for (Map.Entry<Class<?>, Function<HudRequest, String>> entry : handlers.entrySet()) {
            if (entry.getKey().isInstance(item)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String processRequest(HudRequest request) {
        SlimefunItem item = request.getSlimefunItem();
        Function<HudRequest, String> customHandler = tryGetHandler(item, customHandlers);

        if (customHandler != null) {
            try {
                String result = customHandler.apply(request);
                return result == null ? "" : result;
            } catch (RuntimeException | LinkageError error) {
                warnHandlerFailure(item, "custom", error, true);
            }
        }

        Function<HudRequest, String> defaultHandler = tryGetHandler(item, defaultHandlers);
        if (defaultHandler == null) {
            return "";
        }

        try {
            String result = defaultHandler.apply(request);
            return result == null ? "" : result;
        } catch (RuntimeException | LinkageError error) {
            warnHandlerFailure(item, "default", error, false);
            return "";
        }
    }

    private void warnHandlerFailure(SlimefunItem item, String handlerType, Throwable error, boolean fallbackAvailable) {
        String warningKey = handlerType + ':' + item.getClass().getName();
        if (!warnedHandlerFailures.add(warningKey)) {
            return;
        }

        SlimeHUD.getInstance().getLogger().warning(
                "SlimeHUD " + handlerType + " handler failed for " + item.getId() + " ("
                        + item.getClass().getName() + "). "
                        + (fallbackAvailable
                                ? "SlimeHUD will try its generic handler and will keep retrying the custom callback."
                                : "The block name will remain visible and SlimeHUD will keep retrying the handler.")
                        + " Further warnings for this handler are suppressed until restart. Cause: " + error);
    }

    private void registerDefaultHandler(Class<?> type, Function<HudRequest, String> handler) {
        defaultHandlers.put(type, handler);
    }

    public void registerCustomHandler(Class<?> type, Function<HudRequest, String> handler) {
        customHandlers.put(type, handler);
    }
}
