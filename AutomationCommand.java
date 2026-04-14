package smarthome;

import java.util.*;

/**
 * AutomationCommand — Runs an AutomationStrategy, captures full snapshot.
 *
 * DESIGN PATTERNS: Command + Strategy + Memento
 *
 * Because an automation mode can touch devices across ALL rooms, the
 * memento here is a full snapshot of every device in the system.
 */
public class AutomationCommand implements Command {

    private final AutomationStrategy strategy;
    private final Collection<Room>   rooms;

    /** Full system snapshot before automation ran. */
    private final Map<SmartDevice, Boolean> snapshot = new LinkedHashMap<>();

    public AutomationCommand(AutomationStrategy strategy, Collection<Room> rooms) {
        this.strategy = strategy;
        this.rooms    = rooms;
    }

    @Override
    public void execute() {
        // Capture full memento
        snapshot.clear();
        for (Room room : rooms) {
            for (SmartDevice d : room.getDevices()) {
                snapshot.put(d, d.isOn());
            }
        }
        // Delegate to the strategy
        strategy.execute(rooms);
    }

    @Override
    public void undo() {
        for (Map.Entry<SmartDevice, Boolean> e : snapshot.entrySet()) {
            e.getKey().setState(e.getValue(), DeviceEvent.Type.UNDO);
        }
    }

    @Override
    public String getDescription() {
        return "Automation: " + strategy.getName();
    }
}
