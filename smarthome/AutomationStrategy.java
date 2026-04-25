package smarthome;

import java.util.Collection;

// I made this interface so I can have different "modes" like Night Mode or Vacation Mode.
// Each mode is its own class but they all promise to have a name and an execute method.
// If I want to add a new mode later, I just make a new class that implements this.
public interface AutomationStrategy {

    // I use this to get the name of the mode so I can show it on a button.
    String getName();

    // I call this to actually run the mode on all the rooms.
    // Each mode decides on its own which devices to touch.
    void execute(Collection<Room> rooms);
}
