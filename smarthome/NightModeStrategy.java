package smarthome;

import java.util.Collection;

// I made this class to be the "Night Mode" automation.
// When I run it, I turn off all the lights and lock all the doors.
// I leave the thermostats alone because I still want the AC running at night.
public class NightModeStrategy implements AutomationStrategy {

    @Override
    public String getName() {
        // I send back this name so the GUI knows what to put on the button.
        return "Night Mode";
    }

    @Override
    public void execute(Collection<Room> rooms) {
        // I go through every room one by one.
        for (Room room : rooms) {
            // For each room I check every device.
            for (SmartDevice d : room.getDevices()) {
                switch (d.getDeviceType()) {
                    case "Light":
                        // I turn the light off.
                        d.setState(false, DeviceEvent.Type.AUTOMATION);
                        break;
                    case "DoorLock":
                        // I lock the door (ON means locked).
                        d.setState(true, DeviceEvent.Type.AUTOMATION);
                        break;
                    // I don't touch the thermostat on purpose.
                }
            }
        }
    }
}
