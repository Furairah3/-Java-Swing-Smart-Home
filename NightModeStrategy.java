package smarthome;

import java.util.Collection;

/**
 * NightModeStrategy — Turns off all Lights, locks all DoorLocks.
 * Thermostats are left as-is (you still want AC at night).
 */
public class NightModeStrategy implements AutomationStrategy {

    @Override
    public String getName() {
        return "Night Mode";
    }

    @Override
    public void execute(Collection<Room> rooms) {
        for (Room room : rooms) {
            for (SmartDevice d : room.getDevices()) {
                switch (d.getDeviceType()) {
                    case "Light":
                        d.setState(false, DeviceEvent.Type.AUTOMATION);
                        break;
                    case "DoorLock":
                        // ON = Locked (secure state for night)
                        d.setState(true, DeviceEvent.Type.AUTOMATION);
                        break;
                    // Thermostat intentionally left unchanged
                }
            }
        }
    }
}
