package smarthome;

// I made this class to represent a thermostat or AC unit.
// To keep things simple I just made it ON or OFF (no temperature controls yet).
public class Thermostat extends SmartDevice {

    // I just pass the name and room straight to the parent class.
    public Thermostat(String name, String room) {
        super(name, room);
    }

    @Override
    public String getDeviceType() {
        // I return "Thermostat" so the rest of the app knows what type I am.
        return "Thermostat";
    }
}
