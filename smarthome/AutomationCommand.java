package smarthome;

import java.util.*;

// I made this class so I can run an automation (like Night Mode) and still be able to undo it.
// I save what every device looked like before, then I run the automation.
// If the user clicks undo, I just put everything back the way I found it.
public class AutomationCommand implements Command {

    // I keep the strategy here so I know what automation to run.
    private final AutomationStrategy strategy;
    // I also keep all the rooms because the automation might touch any of them.
    private final Collection<Room>   rooms;

    // I made this map to remember the old ON/OFF state of every device before I changed anything.
    private final Map<SmartDevice, Boolean> snapshot = new LinkedHashMap<>();

    // I take the strategy and the rooms when someone makes a new AutomationCommand.
    public AutomationCommand(AutomationStrategy strategy, Collection<Room> rooms) {
        this.strategy = strategy;
        this.rooms    = rooms;
    }

    @Override
    public void execute() {
        // First I clear out any old snapshot so I start fresh.
        snapshot.clear();
        // Then I go through every room and every device and save its current state.
        for (Room room : rooms) {
            for (SmartDevice d : room.getDevices()) {
                snapshot.put(d, d.isOn());
            }
        }
        // Now I let the strategy do its job (turn things on/off however it wants).
        strategy.execute(rooms);
    }

    @Override
    public void undo() {
        // I go through my saved snapshot and put each device back to how it was.
        for (Map.Entry<SmartDevice, Boolean> e : snapshot.entrySet()) {
            e.getKey().setState(e.getValue(), DeviceEvent.Type.UNDO);
        }
    }

    @Override
    public String getDescription() {
        // I made this so the GUI can show a nice tooltip like "Automation: Night Mode".
        return "Automation: " + strategy.getName();
    }
}
