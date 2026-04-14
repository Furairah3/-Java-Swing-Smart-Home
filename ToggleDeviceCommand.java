package smarthome;

/**
 * ToggleDeviceCommand — Toggles one device, stores previous state.
 *
 * DESIGN PATTERNS: Command + Memento (hybrid)
 * The "memento" here is simply the boolean {@code previousState}.
 */
public class ToggleDeviceCommand implements Command {

    private final SmartDevice device;
    private boolean previousState;

    public ToggleDeviceCommand(SmartDevice device) {
        this.device = device;
    }

    @Override
    public void execute() {
        previousState = device.isOn();
        device.toggle();
    }

    @Override
    public void undo() {
        // Restore the state that existed before execute()
        device.setState(previousState, DeviceEvent.Type.UNDO);
    }

    @Override
    public String getDescription() {
        return "Toggle " + device.getName();
    }
}
