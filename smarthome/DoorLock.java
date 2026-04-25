package smarthome;

// I made this class to represent a door lock.
// I just say ON means locked (safe) and OFF means unlocked.
public class DoorLock extends SmartDevice {

    // I just pass the name and room straight to the parent class.
    public DoorLock(String name, String room) {
        super(name, room);
    }

    @Override
    public String getDeviceType() {
        // I return the string "DoorLock" so the rest of the app knows what type I am.
        return "DoorLock";
    }
}
