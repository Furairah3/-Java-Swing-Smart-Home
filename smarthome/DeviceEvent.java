package smarthome;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// I made this class to hold all the info about a device change in one neat object.
// Instead of passing around a bunch of strings, I just pass one of these.
// Once I make one, I can't change it (it's immutable, all the fields are final).
public final class DeviceEvent {

    // I made this enum to label what kind of event happened so the log can show it.
    public enum Type { TOGGLE, AUTOMATION, ROOM_MASTER, UNDO, REDO }

    // I save all these things when the event happens so I can show them later.
    private final String   deviceName;
    private final String   deviceType;   // I use this to know if it's a Light, Thermostat, or DoorLock.
    private final String   roomName;
    private final boolean  newState;      // I store true if the device turned ON, false if OFF.
    private final Type     eventType;
    private final LocalDateTime timestamp;

    // I take all the info in the constructor and grab the time right when it's made.
    public DeviceEvent(String deviceName, String deviceType, String roomName,
                       boolean newState, Type eventType) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.roomName   = roomName;
        this.newState   = newState;
        this.eventType  = eventType;
        this.timestamp  = LocalDateTime.now();
    }

    // I made all these getters so other classes can read the data but not change it.

    public String   getDeviceName() { return deviceName; }
    public String   getDeviceType() { return deviceType; }
    public String   getRoomName()   { return roomName; }
    public boolean  isOn()          { return newState; }
    public Type     getEventType()  { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // I format the time as HH:mm:ss so it looks nice in the log.
    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // I picked an emoji for each device type so the log looks more fun.
    public String getDeviceIcon() {
        switch (deviceType) {
            case "Light":      return "💡"; // light bulb emoji
            case "Thermostat": return "🌡"; // thermometer emoji
            case "DoorLock":   return "🔒"; // lock emoji
            default:           return "⚙";
        }
    }

    // I show a green circle when the device is ON and a red one when it's OFF.
    public String getStateIcon() {
        return newState ? "🟢" : "🔴";
    }

    // I built one big string here so the GUI can just append it to the log.
    public String toLogString() {
        return String.format("[%s] %s %s %s → %s  %s",
                getFormattedTime(), getDeviceIcon(), roomName, deviceName,
                newState ? "ON" : "OFF", getStateIcon());
    }

    @Override
    public String toString() {
        // I just send back the log string so printing the event is easy.
        return toLogString();
    }
}
