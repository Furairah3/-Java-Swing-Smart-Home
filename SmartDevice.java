package smarthome;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SmartDevice — Abstract Base for Every Controllable Device
 *
 * Acts as the **Subject** in the Observer pattern.  Maintains a thread-safe
 * list of observers and notifies them on every state transition.
 *
 * Subclasses (Light, Thermostat, DoorLock) only need to provide their
 * type label; all toggle/observer logic lives here (Template Method idea).
 *
 * DESIGN PATTERN: Observer (Subject role)
 */
public abstract class SmartDevice {

    private final String name;
    private String room;
    private boolean on;

    /** Thread-safe observer list — safe to iterate while modifying. */
    private final List<DeviceObserver> observers = new CopyOnWriteArrayList<>();

    protected SmartDevice(String name, String room) {
        this.name = name;
        this.room = room;
        this.on   = false;
    }

    /* ── Abstract hook for subclasses ───────────────────────────────── */

    /** Returns human-readable type, e.g. "Light", "Thermostat". */
    public abstract String getDeviceType();

    /* ── Observer management ───────────────────────────────────────── */

    public void addObserver(DeviceObserver o)    { if (o != null) observers.add(o); }
    public void removeObserver(DeviceObserver o) { observers.remove(o); }

    /** Push a DeviceEvent to every registered observer. */
    protected void notifyObservers(DeviceEvent.Type eventType) {
        DeviceEvent event = new DeviceEvent(
                name, getDeviceType(), room, on, eventType);
        for (DeviceObserver o : observers) {
            o.onDeviceEvent(event);
        }
    }

    /* ── State control ─────────────────────────────────────────────── */

    /** Toggle and notify — the primary public API for device control. */
    public void toggle() {
        this.on = !this.on;
        notifyObservers(DeviceEvent.Type.TOGGLE);
    }

    /** Set to a specific state; notifies only if state actually changes. */
    public void setState(boolean desiredOn, DeviceEvent.Type eventType) {
        if (this.on != desiredOn) {
            this.on = desiredOn;
            notifyObservers(eventType);
        }
    }

    /**
     * Silently restore state without firing observers.
     * Used by the Preferences restore on startup so the log stays clean.
     */
    public void restoreState(boolean state) {
        this.on = state;
    }

    /* ── Getters / setters ─────────────────────────────────────────── */

    public String  getName()    { return name; }
    public String  getRoom()    { return room; }
    public boolean isOn()       { return on; }
    public void    setRoom(String room) { this.room = room; }

    /** Convenience label for UI buttons: "💡 Living Room Light [ON]" */
    public String getDisplayLabel() {
        String icon;
        switch (getDeviceType()) {
            case "Light":      icon = "\uD83D\uDCA1"; break;
            case "Thermostat": icon = "\uD83C\uDF21"; break;
            case "DoorLock":   icon = "\uD83D\uDD12"; break;
            default:           icon = "⚙"; break;
        }
        return String.format("%s %s [%s]", icon, name, on ? "ON" : "OFF");
    }

    @Override
    public String toString() {
        return getDisplayLabel();
    }
}
