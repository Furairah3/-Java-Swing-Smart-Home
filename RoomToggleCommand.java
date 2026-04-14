package smarthome;

import java.util.*;

/**
 * RoomToggleCommand — Flips an entire room ON or OFF.
 *
 * Stores a snapshot (memento) of every device's state in the room
 * so we can precisely restore the mixed state that existed before.
 */
public class RoomToggleCommand implements Command {

    private final Room    room;
    private final boolean targetState;   // true = turn all ON, false = all OFF

    /** Memento: device → its state before we changed anything. */
    private final Map<SmartDevice, Boolean> snapshot = new LinkedHashMap<>();

    /**
     * @param room         The room to control.
     * @param targetState  true = all ON, false = all OFF.
     */
    public RoomToggleCommand(Room room, boolean targetState) {
        this.room        = room;
        this.targetState = targetState;
    }

    @Override
    public void execute() {
        // Capture memento
        snapshot.clear();
        for (SmartDevice d : room.getDevices()) {
            snapshot.put(d, d.isOn());
        }
        // Apply
        if (targetState) room.allOn(); else room.allOff();
    }

    @Override
    public void undo() {
        // Restore each device to its individual previous state
        for (Map.Entry<SmartDevice, Boolean> e : snapshot.entrySet()) {
            e.getKey().setState(e.getValue(), DeviceEvent.Type.UNDO);
        }
    }

    @Override
    public String getDescription() {
        return "Room " + room.getName() + " → " + (targetState ? "ALL ON" : "ALL OFF");
    }
}
