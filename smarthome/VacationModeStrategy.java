package smarthome;

import java.util.Collection;

// I made this class to be the "Vacation Mode" automation.
// When I run it, I turn off all the thermostats (to save power) and lock all the doors.
// I leave the lights alone so the user can still set timers to make it look like someone is home.
public class VacationModeStrategy implements AutomationStrategy {

    @Override
    public String getName() {
        // I send back this name so the GUI knows what to put on the button.
        return "Vacation Mode";
    }

    @Override
    public void execute(Collection<Room> rooms) {
        // I go through every room.
        for (Room room : rooms) {
            // I check every device in that room.
            for (SmartDevice d : room.getDevices()) {
                switch (d.getDeviceType()) {
                    case "Thermostat":
                        // I turn off the thermostat to save energy while I'm away.
                        d.setState(false, DeviceEvent.Type.AUTOMATION);
                        break;
                    case "DoorLock":
                        // I lock the door for security.
                        d.setState(true, DeviceEvent.Type.AUTOMATION);
                        break;
                    // I don't touch the lights on purpose.
                }
            }
        }
    }
}
