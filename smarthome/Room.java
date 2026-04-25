package smarthome;

import java.util.*;

// I made this class so I can group devices that belong together (like all stuff in the kitchen).
// I also added a "master switch" idea so the user can turn everything in the room on or off at once.
public class Room {

    // I save the name of the room (like "Bedroom") and a list of all my devices.
    private final String name;
    private final List<SmartDevice> devices = new ArrayList<>();

    // I just take the room name when someone makes a new Room.
    public Room(String name) {
        this.name = name;
    }

    // I add a device to my list and tell the device which room it belongs to.
    public void addDevice(SmartDevice device) {
        device.setRoom(name);
        devices.add(device);
    }

    // I made this so I can take a device out of the room if I need to.
    public void removeDevice(SmartDevice device) {
        devices.remove(device);
    }

    // I send back a read-only version of my list so nobody can mess with it from outside.
    public List<SmartDevice> getDevices() {
        return Collections.unmodifiableList(devices);
    }

    // I made this to turn every single device in the room ON in one go.
    public void allOn() {
        for (SmartDevice d : devices) {
            d.setState(true, DeviceEvent.Type.ROOM_MASTER);
        }
    }

    // I made this to turn every single device in the room OFF in one go.
    public void allOff() {
        for (SmartDevice d : devices) {
            d.setState(false, DeviceEvent.Type.ROOM_MASTER);
        }
    }

    // I check if any device in the room is on so the master switch knows what to show.
    public boolean hasActiveDevice() {
        for (SmartDevice d : devices) {
            if (d.isOn()) return true;
        }
        return false;
    }

    // I made this so I can grab just the lights, or just the locks, etc.
    public List<SmartDevice> getDevicesByType(String type) {
        List<SmartDevice> result = new ArrayList<>();
        for (SmartDevice d : devices) {
            if (d.getDeviceType().equals(type)) result.add(d);
        }
        return result;
    }

    // Simple getters I added so other classes can read my data.
    public String getName() { return name; }
    public int    size()    { return devices.size(); }

    @Override
    public String toString() {
        // I made this string so it prints nicely, like "Bedroom (3 devices)".
        return name + " (" + devices.size() + " devices)";
    }
}
