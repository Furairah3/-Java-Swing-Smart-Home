package smarthome;

import java.util.*;

// I made this command for when the user wants to flip a whole room ON or OFF at once.
// I save what every device looked like before so I can undo it later.
// That way if the room had mixed states (some ON, some OFF), I can put it back exactly.
public class RoomToggleCommand implements Command {

    // I keep the room I'm changing and a true/false for what state I want everything to go to.
    private final Room    room;
    private final boolean targetState;   // I use true for "all ON" and false for "all OFF".

    // I made this map to remember each device's old state so I can restore them later.
    private final Map<SmartDevice, Boolean> snapshot = new LinkedHashMap<>();

    // I take the room and what state I want when someone makes a RoomToggleCommand.
    public RoomToggleCommand(Room room, boolean targetState) {
        this.room        = room;
        this.targetState = targetState;
    }

    @Override
    public void execute() {
        // I clear the old snapshot first so I start fresh.
        snapshot.clear();
        // I save the current ON/OFF state of every device.
        for (SmartDevice d : room.getDevices()) {
            snapshot.put(d, d.isOn());
        }
        // Now I either flip everything on or everything off depending on the target.
        if (targetState) room.allOn(); else room.allOff();
    }

    @Override
    public void undo() {
        // I go through the snapshot and put each device back to how I found it.
        for (Map.Entry<SmartDevice, Boolean> e : snapshot.entrySet()) {
            e.getKey().setState(e.getValue(), DeviceEvent.Type.UNDO);
        }
    }

    @Override
    public String getDescription() {
        // I made this so the GUI tooltip can say something like "Room Bedroom → ALL ON".
        return "Room " + room.getName() + " → " + (targetState ? "ALL ON" : "ALL OFF");
    }
}
