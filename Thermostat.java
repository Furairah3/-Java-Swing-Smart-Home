package smarthome;

/**
 * Thermostat / AC — Concrete SmartDevice
 *
 * In a real system this would expose temperature set-points;
 * here we model the simple ON/OFF toggle required by the rubric.
 */
public class Thermostat extends SmartDevice {

    public Thermostat(String name, String room) {
        super(name, room);
    }

    @Override
    public String getDeviceType() {
        return "Thermostat";
    }
}
