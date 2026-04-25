package smarthome;

import java.lang.reflect.Constructor;
import java.util.*;

// I made this class to keep a list of all my device types and their classes.
// I matched a name like "Light" to the actual Light.class.
// This way when someone says "make me a Light", I can use reflection to actually build one
// without writing big if/else chains everywhere.
public final class DeviceRegistry {

    // I keep my type names mapped to the class so I can look them up later.
    // I picked LinkedHashMap because I want them to stay in the order I added them.
    private final Map<String, Class<? extends SmartDevice>> registry =
            new LinkedHashMap<>();

    // I made one single registry for the whole app (singleton-style).
    private static final DeviceRegistry INSTANCE = new DeviceRegistry();

    // I made the constructor private and call my register method right away.
    private DeviceRegistry() {
        scanAndRegister();
    }

    // I use this so other classes can grab the one and only registry.
    public static DeviceRegistry getInstance() {
        return INSTANCE;
    }

    // Here I register all the device types I made.
    // If I want to add a new one (like SecurityCamera), I just add one more line here.
    private void scanAndRegister() {
        register("Light",      Light.class);
        register("Thermostat", Thermostat.class);
        register("DoorLock",   DoorLock.class);
    }

    // I made this method public so other people could add new types from outside if they wanted.
    public void register(String typeName, Class<? extends SmartDevice> clazz) {
        registry.put(typeName, clazz);
    }

    // I send back a list of the type names so the GUI dropdown can use them.
    public List<String> getRegisteredTypes() {
        return new ArrayList<>(registry.keySet());
    }

    // I made this in case I need to check if a type name was already added.
    public boolean isRegistered(String typeName) {
        return registry.containsKey(typeName);
    }

    // I made this method to actually build the device using reflection.
    // I look up the class, find its constructor, and then call newInstance to make one.
    public SmartDevice createInstance(String typeName, String name, String room) {
        Class<? extends SmartDevice> clazz = registry.get(typeName);
        if (clazz == null) {
            // If I don't know the type, I throw an error so the user knows what went wrong.
            throw new IllegalArgumentException(
                    "Unknown device type: '" + typeName +
                    "'. Registered types: " + registry.keySet());
        }
        try {
            // I made all my SmartDevice subclasses use the same (name, room) constructor on purpose.
            Constructor<? extends SmartDevice> ctor =
                    clazz.getConstructor(String.class, String.class);
            return ctor.newInstance(name, room);
        } catch (ReflectiveOperationException e) {
            // If reflection breaks for some reason, I wrap it in a runtime exception.
            throw new RuntimeException(
                    "Reflection failed for type '" + typeName + "': " + e.getMessage(), e);
        }
    }
}
