package smarthome;

import java.util.Collection;

/**
 * VacationModeStrategy — Turns off all Thermostats, locks all DoorLocks.
 * Lights are left as-is (can simulate presence with timers).
 */
public class VacationModeStrategy implements AutomationStrategy {

    @Override
    public String getName() {
        return "Vacation Mode";
    }

    @Override
    public void execute(Collection<Room> rooms) {
        for (Room room : rooms) {
            for (SmartDevice d : room.getDevices()) {
                switch (d.getDeviceType()) {
                    case "Thermostat":
                        d.setState(false, DeviceEvent.Type.AUTOMATION);
                        break;
                    case "DoorLock":
                        d.setState(true, DeviceEvent.Type.AUTOMATION);
                        break;
                    // Lights intentionally left unchanged
                }
            }
        }
    }
}
