package smarthome;

// I made this command for when the user wants to flip just one device on or off.
// I save the previous state so if the user clicks undo, I can put it back.
public class ToggleDeviceCommand implements Command {

    // I keep the device I'm toggling and the state it had before I changed it.
    private final SmartDevice device;
    private boolean previousState;

    // I take the device when someone makes a new ToggleDeviceCommand.
    public ToggleDeviceCommand(SmartDevice device) {
        this.device = device;
    }

    @Override
    public void execute() {
        // I save the current state first so I can restore it later.
        previousState = device.isOn();
        // Then I flip the device.
        device.toggle();
    }

    @Override
    public void undo() {
        // I put the device back to whatever state it was in before I touched it.
        device.setState(previousState, DeviceEvent.Type.UNDO);
    }

    @Override
    public String getDescription() {
        // I made this so the GUI tooltip can say something like "Toggle Ceiling Light".
        return "Toggle " + device.getName();
    }
}
