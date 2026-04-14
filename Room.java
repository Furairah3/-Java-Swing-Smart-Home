package smarthome;

import java.util.*;

/**
 * Room — Logical Grouping of SmartDevices
 *
 * Provides a "master switch" that can turn all contained devices ON or OFF
 * in a single operation.  The Room itself is not an Observer; it is a pure
 * domain model.  The GUI observes each device individually.
 */
public class Room {

    private final String name;
    private final List<SmartDevice> devices = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    /* ── Device management ─────────────────────────────────────────── */

    public void addDevice(SmartDevice device) {
        device.setRoom(name);
        devices.add(device);
    }

    public void removeDevice(SmartDevice device) {
        devices.remove(device);
    }

    /** Unmodifiable view for iteration. */
    public List<SmartDevice> getDevices() {
        return Collections.unmodifiableList(devices);
    }

    /* ── Bulk operations ───────────────────────────────────────────── */

    /** Turn every device in this room ON. */
    public void allOn() {
        for (SmartDevice d : devices) {
            d.setState(true, DeviceEvent.Type.ROOM_MASTER);
        }
    }

    /** Turn every device in this room OFF. */
    public void allOff() {
        for (SmartDevice d : devices) {
            d.setState(false, DeviceEvent.Type.ROOM_MASTER);
        }
    }

    /** Returns true if ANY device in the room is ON. */
    public boolean hasActiveDevice() {
        for (SmartDevice d : devices) {
            if (d.isOn()) return true;
        }
        return false;
    }

    /* ── Queries by type ───────────────────────────────────────────── */

    public List<SmartDevice> getDevicesByType(String type) {
        List<SmartDevice> result = new ArrayList<>();
        for (SmartDevice d : devices) {
            if (d.getDeviceType().equals(type)) result.add(d);
        }
        return result;
    }

    /* ── Accessors ─────────────────────────────────────────────────── */

    public String getName() { return name; }
    public int    size()    { return devices.size(); }

    @Override
    public String toString() {
        return name + " (" + devices.size() + " devices)";
    }
}
