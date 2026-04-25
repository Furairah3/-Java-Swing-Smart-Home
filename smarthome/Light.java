package smarthome;

// I made this class to represent a light bulb in the smart home.
// I never make one with "new Light(...)" myself - I always use the DeviceFactory.
public class Light extends SmartDevice {

    // I just hand the name and room over to the parent class.
    public Light(String name, String room) {
        super(name, room);
    }

    @Override
    public String getDeviceType() {
        // I return "Light" so the rest of the app knows I'm a light.
        return "Light";
    }
}
