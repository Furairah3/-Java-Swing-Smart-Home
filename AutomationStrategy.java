package smarthome;

import java.util.Collection;

/**
 * AutomationStrategy — Strategy Pattern Interface
 *
 * DESIGN PATTERN: Strategy
 *
 * Encapsulates an automation algorithm behind a common interface so the
 * SmartHomeHub can execute any mode without knowing the details.  New
 * modes (e.g., "Party Mode", "Morning Routine") are added by creating a
 * new class — no modification to SmartHomeHub or the GUI.
 */
public interface AutomationStrategy {

    /** Human-readable name shown in the GUI. */
    String getName();

    /**
     * Execute this automation across the given rooms.
     * Each strategy decides which devices to affect and how.
     */
    void execute(Collection<Room> rooms);
}
