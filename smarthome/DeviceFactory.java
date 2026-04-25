package smarthome;

import java.util.List;

// I made this class so I never have to write "new Light(...)" all over my code.
// Instead I just call DeviceFactory.create("Light", "Lamp", "Bedroom") and it gives me one back.
// I made it static because I don't need to make objects out of this class.
public final class DeviceFactory {

    // I made the constructor private so nobody can accidentally make an instance of this.
    private DeviceFactory() { }

    // I call the registry here to actually build the device using reflection.
    // The good thing is if I add a new device type later, I don't need to touch this method.
    public static SmartDevice create(String type, String name, String room) {
        return DeviceRegistry.getInstance().createInstance(type, name, room);
    }

    // I made this so the GUI can fill the dropdown menu with all the device types I support.
    public static List<String> getAvailableTypes() {
        return DeviceRegistry.getInstance().getRegisteredTypes();
    }
}
