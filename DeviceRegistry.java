package smarthome;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * DeviceRegistry — Reflection-Driven Type Registry
 *
 * Maintains a map from user-friendly type names ("Light", "Thermostat", …)
 * to their corresponding Class objects.  New device types can be added at
 * runtime without modifying any existing code — this is the essence of the
 * **Open/Closed Principle** (open for extension, closed for modification).
 *
 * HOW IT WORKS:
 *  1. On startup, {@link #scanAndRegister()} registers every known concrete
 *     subclass of SmartDevice.  In a production system you would use
 *     a library like Reflections or Java's ServiceLoader SPI to discover
 *     classes automatically.  Here we list them explicitly but instantiate
 *     via reflection, so the Factory never contains if/else chains.
 *
 *  2. The GUI reads {@link #getRegisteredTypes()} to populate its
 *     "Add Device" dropdown — fully dynamic.
 *
 *  3. {@link #createInstance(String, String, String)} uses
 *     {@code Constructor.newInstance()} — pure reflection.
 *
 * DESIGN PATTERN: Factory + Reflection (eliminates switch/case)
 * SOLID: Open/Closed Principle
 */
public final class DeviceRegistry {

    /**
     * Maps a human-readable type name → the Class that implements it.
     * Using LinkedHashMap to preserve insertion order for the UI dropdown.
     */
    private final Map<String, Class<? extends SmartDevice>> registry =
            new LinkedHashMap<>();

    /** Singleton-style — one registry for the whole app. */
    private static final DeviceRegistry INSTANCE = new DeviceRegistry();

    private DeviceRegistry() {
        scanAndRegister();
    }

    public static DeviceRegistry getInstance() {
        return INSTANCE;
    }

    /* ── Registration ──────────────────────────────────────────────── */

    /**
     * Register built-in device types.
     *
     * To add a NEW device (e.g., "SecurityCamera"), a developer only needs to:
     *   1. Create SecurityCamera.java extending SmartDevice.
     *   2. Add one line here: register("SecurityCamera", SecurityCamera.class);
     *   3. Nothing else changes — the Factory, the GUI dropdown, and the
     *      Observer chain all pick it up automatically.
     *
     * In a framework with classpath scanning this step would also be automatic.
     */
    private void scanAndRegister() {
        register("Light",      Light.class);
        register("Thermostat", Thermostat.class);
        register("DoorLock",   DoorLock.class);
    }

    /** Public registration point for plugins / dynamic extensions. */
    public void register(String typeName, Class<? extends SmartDevice> clazz) {
        registry.put(typeName, clazz);
    }

    /* ── Queries ───────────────────────────────────────────────────── */

    /** Ordered list of type names for the UI combo box. */
    public List<String> getRegisteredTypes() {
        return new ArrayList<>(registry.keySet());
    }

    public boolean isRegistered(String typeName) {
        return registry.containsKey(typeName);
    }

    /* ── Reflection-based instantiation ────────────────────────────── */

    /**
     * Creates a SmartDevice instance entirely through reflection.
     *
     * No if/else, no switch — just Constructor.newInstance().
     * This is the key advantage of the Reflection Factory over a
     * traditional factory: adding a new type never touches this method.
     *
     * @param typeName  Registered type, e.g. "Light"
     * @param name      Device name, e.g. "Ceiling Light"
     * @param room      Room assignment, e.g. "Living Room"
     * @return          A fully constructed SmartDevice subclass instance.
     * @throws IllegalArgumentException if type is not registered.
     */
    public SmartDevice createInstance(String typeName, String name, String room) {
        Class<? extends SmartDevice> clazz = registry.get(typeName);
        if (clazz == null) {
            throw new IllegalArgumentException(
                    "Unknown device type: '" + typeName +
                    "'. Registered types: " + registry.keySet());
        }
        try {
            // All SmartDevice subclasses must have (String name, String room) ctor
            Constructor<? extends SmartDevice> ctor =
                    clazz.getConstructor(String.class, String.class);
            return ctor.newInstance(name, room);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Reflection failed for type '" + typeName + "': " + e.getMessage(), e);
        }
    }
}
