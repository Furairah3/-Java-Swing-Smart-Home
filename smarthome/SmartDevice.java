package smarthome;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// I made this class as the base for every device in my smart home.
// All my real devices (Light, Thermostat, DoorLock) extend this one.
// I put the toggle logic and observer logic in here so I don't have to repeat myself.
public abstract class SmartDevice {

    // I save the device's name, the room it's in, and whether it's currently on.
    private final String name;
    private String room;
    private boolean on;

    // I use a CopyOnWriteArrayList because it's safe to use even if observers are added while looping.
    private final List<DeviceObserver> observers = new CopyOnWriteArrayList<>();

    // I take the name and room when a child class makes a new device.
    // Devices always start as OFF.
    protected SmartDevice(String name, String room) {
        this.name = name;
        this.room = room;
        this.on   = false;
    }

    // I make my child classes tell me what type they are (like "Light" or "DoorLock").
    public abstract String getDeviceType();

    // I made these so observers can sign up or leave.
    public void addObserver(DeviceObserver o)    { if (o != null) observers.add(o); }
    public void removeObserver(DeviceObserver o) { observers.remove(o); }

    // I build a DeviceEvent and send it to every observer that signed up.
    protected void notifyObservers(DeviceEvent.Type eventType) {
        DeviceEvent event = new DeviceEvent(
                name, getDeviceType(), room, on, eventType);
        for (DeviceObserver o : observers) {
            o.onDeviceEvent(event);
        }
    }

    // I flip the on/off state and tell my observers something happened.
    public void toggle() {
        this.on = !this.on;
        notifyObservers(DeviceEvent.Type.TOGGLE);
    }

    // I set the device to a specific state but only tell observers if it actually changed.
    // No point in spamming the log if I tell a light that's already off to turn off.
    public void setState(boolean desiredOn, DeviceEvent.Type eventType) {
        if (this.on != desiredOn) {
            this.on = desiredOn;
            notifyObservers(eventType);
        }
    }

    // I made this special method to load saved state without putting noise in the log on startup.
    public void restoreState(boolean state) {
        this.on = state;
    }

    // Simple getters and setters I added so other classes can read and update my fields.
    public String  getName()    { return name; }
    public String  getRoom()    { return room; }
    public boolean isOn()       { return on; }
    public void    setRoom(String room) { this.room = room; }

    // I made this for the GUI so each button can show the right emoji and ON/OFF status.
    public String getDisplayLabel() {
        String icon;
        switch (getDeviceType()) {
            case "Light":      icon = "💡"; break;
            case "Thermostat": icon = "🌡"; break;
            case "DoorLock":   icon = "🔒"; break;
            default:           icon = "⚙"; break;
        }
        return String.format("%s %s [%s]", icon, name, on ? "ON" : "OFF");
    }

    @Override
    public String toString() {
        // I just use the display label so printing a device looks nice.
        return getDisplayLabel();
    }
}
