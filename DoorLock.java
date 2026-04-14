package smarthome;

/**
 * DoorLock — Concrete SmartDevice
 *
 * ON  = Locked (secure)
 * OFF = Unlocked
 */
public class DoorLock extends SmartDevice {

    public DoorLock(String name, String room) {
        super(name, room);
    }

    @Override
    public String getDeviceType() {
        return "DoorLock";
    }
}
