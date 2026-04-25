package smarthome;

import java.util.*;

// I made this class as the central place that holds everything in my smart home.
// It only ever has one instance (singleton) because the whole app shares the same data.
// I used the Bill Pugh trick (an inner static class) so it loads only when I need it.
public final class SmartHomeHub {

    // I made this inner class so the JVM only creates the instance the first time I ask for it.
    // The JVM handles all the thread-safety stuff for me automatically.
    private static class Holder {
        private static final SmartHomeHub INSTANCE = new SmartHomeHub();
    }

    // I made the constructor private so nobody else can make a second hub.
    private SmartHomeHub() {
        commandManager = new CommandManager();
    }

    // I use this method to get the one and only hub from anywhere in the app.
    public static SmartHomeHub getInstance() {
        return Holder.INSTANCE;
    }

    // I keep all my rooms here in a map by name.
    // I picked LinkedHashMap because I want them to stay in the order I added them.
    private final Map<String, Room> rooms = new LinkedHashMap<>();

    // I keep one command manager for the whole app so undo and redo work everywhere.
    private final CommandManager commandManager;

    // I keep a list of all my automations (Night Mode, Vacation Mode, etc).
    private final Map<String, AutomationStrategy> strategies = new LinkedHashMap<>();

    // If the room exists I return it, otherwise I make a new one.
    public Room getOrCreateRoom(String name) {
        return rooms.computeIfAbsent(name, Room::new);
    }

    // I made this so I can grab a room by name.
    public Room getRoom(String name) {
        return rooms.get(name);
    }

    // I send back a read-only list of rooms so nobody can mess with my map from outside.
    public Collection<Room> getRooms() {
        return Collections.unmodifiableCollection(rooms.values());
    }

    // I made this so the GUI can fill its room dropdown easily.
    public List<String> getRoomNames() {
        return new ArrayList<>(rooms.keySet());
    }

    // I use the factory to make a new device, then I add it to the right room and return it.
    public SmartDevice addDevice(String type, String name, String roomName) {
        SmartDevice device = DeviceFactory.create(type, name, roomName);
        Room room = getOrCreateRoom(roomName);
        room.addDevice(device);
        return device;
    }

    // I made this to grab every single device across all rooms in one big list.
    public List<SmartDevice> getAllDevices() {
        List<SmartDevice> all = new ArrayList<>();
        for (Room room : rooms.values()) {
            all.addAll(room.getDevices());
        }
        return all;
    }

    // I made this so the GUI can talk to my command manager directly.
    public CommandManager getCommandManager() {
        return commandManager;
    }

    // I added this shortcut so other classes can run a command without grabbing the manager first.
    public void executeCommand(Command cmd) {
        commandManager.execute(cmd);
    }

    // I made this so I can register a new automation strategy at runtime.
    public void registerStrategy(AutomationStrategy strategy) {
        strategies.put(strategy.getName(), strategy);
    }

    // I made this so I can get a strategy by name if I need it.
    public AutomationStrategy getStrategy(String name) {
        return strategies.get(name);
    }

    // I send back a read-only list of strategies so the GUI can make buttons for them.
    public Collection<AutomationStrategy> getStrategies() {
        return Collections.unmodifiableCollection(strategies.values());
    }

    // I wrap the strategy in an AutomationCommand so it can be undone, then I run it.
    public void runAutomation(AutomationStrategy strategy) {
        AutomationCommand cmd = new AutomationCommand(strategy, rooms.values());
        commandManager.execute(cmd);
    }

    // I save the on/off state of every device using Java Preferences.
    // I call this when the user closes the window so their setup is remembered.
    public void savePreferences() {
        java.util.prefs.Preferences prefs =
                java.util.prefs.Preferences.userNodeForPackage(SmartHomeHub.class);
        for (SmartDevice d : getAllDevices()) {
            // I made the key by joining the room name and the device name.
            String key = d.getRoom() + "::" + d.getName();
            prefs.putBoolean(key, d.isOn());
        }
        try { prefs.flush(); } catch (Exception ignored) { }
    }

    // I load the saved states back when the app starts up.
    // I use restoreState (not setState) so it doesn't fire observers and spam the log.
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
