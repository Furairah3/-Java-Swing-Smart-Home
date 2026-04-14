package smarthome;

import java.util.*;

/**
 * SmartHomeHub — Central Coordinator (Bill Pugh Singleton)
 *
 * DESIGN PATTERN: Singleton (Bill Pugh / Inner Static Helper)
 *
 * Why Bill Pugh over double-checked locking?
 *  - The inner class {@code Holder} is not loaded by the JVM until
 *    {@link #getInstance()} is called, providing lazy initialization
 *    with no synchronization overhead — the JVM class-loading mechanism
 *    itself guarantees thread safety.
 *  - Simpler, more elegant, and recommended by Joshua Bloch.
 *
 * Responsibilities:
 *  - Owns the canonical Map of Room objects.
 *  - Owns the CommandManager (undo/redo).
 *  - Coordinates AutomationStrategies.
 *  - Handles device preferences persistence.
 */
public final class SmartHomeHub {

    /* ── Bill Pugh inner static helper ─────────────────────────────── */

    /**
     * This inner class is not loaded until getInstance() is referenced,
     * giving us lazy initialization with inherent thread safety.
     */
    private static class Holder {
        private static final SmartHomeHub INSTANCE = new SmartHomeHub();
    }

    /** Private constructor — enforces Singleton constraint. */
    private SmartHomeHub() {
        commandManager = new CommandManager();
    }

    /**
     * Global access point.
     * @return The one and only SmartHomeHub instance.
     */
    public static SmartHomeHub getInstance() {
        return Holder.INSTANCE;
    }

    /* ── State ─────────────────────────────────────────────────────── */

    /** Rooms indexed by name; LinkedHashMap preserves insertion order. */
    private final Map<String, Room> rooms = new LinkedHashMap<>();

    /** The system-wide command manager for undo/redo. */
    private final CommandManager commandManager;

    /** Registry of available automation strategies. */
    private final Map<String, AutomationStrategy> strategies = new LinkedHashMap<>();

    /* ── Room management ───────────────────────────────────────────── */

    /** Get or create a room by name. */
    public Room getOrCreateRoom(String name) {
        return rooms.computeIfAbsent(name, Room::new);
    }

    public Room getRoom(String name) {
        return rooms.get(name);
    }

    /** Ordered collection of all rooms (for GUI iteration). */
    public Collection<Room> getRooms() {
        return Collections.unmodifiableCollection(rooms.values());
    }

    public List<String> getRoomNames() {
        return new ArrayList<>(rooms.keySet());
    }

    /* ── Device lifecycle ──────────────────────────────────────────── */

    /**
     * Create a device via the Factory, add it to the specified room,
     * and return it so the caller can attach observers.
     */
    public SmartDevice addDevice(String type, String name, String roomName) {
        SmartDevice device = DeviceFactory.create(type, name, roomName);
        Room room = getOrCreateRoom(roomName);
        room.addDevice(device);
        return device;
    }

    /** Flat list of every device in the system. */
    public List<SmartDevice> getAllDevices() {
        List<SmartDevice> all = new ArrayList<>();
        for (Room room : rooms.values()) {
            all.addAll(room.getDevices());
        }
        return all;
    }

    /* ── Command management (Undo / Redo) ──────────────────────────── */

    public CommandManager getCommandManager() {
        return commandManager;
    }

    /** Convenience: execute a command through the hub. */
    public void executeCommand(Command cmd) {
        commandManager.execute(cmd);
    }

    /* ── Strategy management ───────────────────────────────────────── */

    public void registerStrategy(AutomationStrategy strategy) {
        strategies.put(strategy.getName(), strategy);
    }

    public AutomationStrategy getStrategy(String name) {
        return strategies.get(name);
    }

    public Collection<AutomationStrategy> getStrategies() {
        return Collections.unmodifiableCollection(strategies.values());
    }

    /** Run an automation strategy through the Command pattern. */
    public void runAutomation(AutomationStrategy strategy) {
        AutomationCommand cmd = new AutomationCommand(strategy, rooms.values());
        commandManager.execute(cmd);
    }

    /* ── Preferences persistence ───────────────────────────────────── */

    /**
     * Save all device states to java.util.prefs.Preferences.
     * Called on application shutdown via a window-closing hook.
     */
    public void savePreferences() {
        java.util.prefs.Preferences prefs =
                java.util.prefs.Preferences.userNodeForPackage(SmartHomeHub.class);
        for (SmartDevice d : getAllDevices()) {
            String key = d.getRoom() + "::" + d.getName();
            prefs.putBoolean(key, d.isOn());
        }
        try { prefs.flush(); } catch (Exception ignored) { }
    }

    /**
     * Restore device states from Preferences.
     * Uses silent {@code restoreState()} to avoid flooding the log on startup.
     */
    public void loadPreferences() {
        java.util.prefs.Preferences prefs =
                java.util.prefs.Preferences.userNodeForPackage(SmartHomeHub.class);
        for (SmartDevice d : getAllDevices()) {
            String key = d.getRoom() + "::" + d.getName();
            boolean savedState = prefs.getBoolean(key, false);
            d.restoreState(savedState);
        }
    }
}
