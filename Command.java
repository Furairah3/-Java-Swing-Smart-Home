package smarthome;

/**
 * Command — Command Pattern Interface
 *
 * DESIGN PATTERN: Command
 *
 * Encapsulates a reversible action as an object.  Each Command stores
 * enough state (a Memento of previous device states) to undo itself.
 * The {@link CommandManager} maintains the undo/redo stacks.
 */
public interface Command {

    /** Execute the action and capture the before-state for undo. */
    void execute();

    /** Reverse the action, restoring the captured before-state. */
    void undo();

    /** Human-readable description for tooltip / log. */
    String getDescription();
}
