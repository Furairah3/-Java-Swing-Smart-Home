package smarthome;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DeviceEvent — Observer Push-Model Payload
 * 
 * Instead of passing a raw String through the Observer chain, we pass a
 * rich, immutable event object.  This decouples the observer from having
 * to parse strings and lets each listener react to structured data.
 *
 * DESIGN PATTERN: Observer (Push Model)
 * The subject (SmartDevice) pushes this object to every registered observer.
 */
public final class DeviceEvent {

    /** Types of events that can flow through the system. */
    public enum Type { TOGGLE, AUTOMATION, ROOM_MASTER, UNDO, REDO }

    private final String   deviceName;
    private final String   deviceType;   // "Light", "Thermostat", "DoorLock"
    private final String   roomName;
    private final boolean  newState;      // true = ON, false = OFF
    private final Type     eventType;
    private final LocalDateTime timestamp;

    public DeviceEvent(String deviceName, String deviceType, String roomName,
                       boolean newState, Type eventType) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.roomName   = roomName;
        this.newState   = newState;
        this.eventType  = eventType;
        this.timestamp  = LocalDateTime.now();
    }

    /* ── Getters (immutable — no setters) ───────────────────────────── */

    public String   getDeviceName() { return deviceName; }
    public String   getDeviceType() { return deviceType; }
    public String   getRoomName()   { return roomName; }
    public boolean  isOn()          { return newState; }
    public Type     getEventType()  { return eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Pre-formatted timestamp for log display. */
    public String getFormattedTime() {
        return timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    /** Human-readable emoji indicator based on device type. */
    public String getDeviceIcon() {
        switch (deviceType) {
            case "Light":      return "\uD83D\uDCA1"; // 💡
            case "Thermostat": return "\uD83C\uDF21"; // 🌡️
            case "DoorLock":   return "\uD83D\uDD12"; // 🔒
            default:           return "⚙";
        }
    }

    /** Status emoji: green circle for ON, red circle for OFF. */
    public String getStateIcon() {
        return newState ? "\uD83D\uDFE2" : "\uD83D\uDD34"; // 🟢 / 🔴
    }

    /** Full log line ready for display. */
    public String toLogString() {
        return String.format("[%s] %s %s %s → %s  %s",
                getFormattedTime(), getDeviceIcon(), roomName, deviceName,
                newState ? "ON" : "OFF", getStateIcon());
    }

    @Override
    public String toString() {
        return toLogString();
    }
}
