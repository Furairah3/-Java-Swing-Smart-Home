package smarthome;

import java.util.List;

/**
 * DeviceFactory — Centralized Device Creation
 *
 * DESIGN PATTERN: Factory Pattern
 *
 * The GUI and SmartHomeHub never call `new Light(...)` directly.
 * Instead they call {@code DeviceFactory.create("Light", "Lamp", "Bedroom")}.
 *
 * Internally this delegates to {@link DeviceRegistry}, which uses Java
 * Reflection to instantiate the correct subclass.  This eliminates the
 * classic if/else or switch anti-pattern and satisfies the Open/Closed
 * Principle: new device types require zero changes to this class.
 *
 * This class is deliberately stateless — a pure utility facade over the
 * registry — so all methods are static.
 */
public final class DeviceFactory {

    private DeviceFactory() { /* utility class — no instances */ }

    /**
     * Create a device by type name using reflection.
     *
     * @param type  One of the registered type names (e.g. "Light").
     * @param name  Human-readable device name (e.g. "Desk Lamp").
     * @param room  Room the device belongs to (e.g. "Study").
     * @return      A new SmartDevice instance, ready to receive observers.
     */
    public static SmartDevice create(String type, String name, String room) {
        return DeviceRegistry.getInstance().createInstance(type, name, room);
    }

    /** Convenience: returns all type names the UI dropdown should show. */
    public static List<String> getAvailableTypes() {
        return DeviceRegistry.getInstance().getRegisteredTypes();
    }
}
