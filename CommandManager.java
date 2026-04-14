package smarthome;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * CommandManager — Undo / Redo Stack Controller
 *
 * DESIGN PATTERN: Command (Invoker role)
 *
 * Maintains two stacks: one for executed commands (undo), one for
 * undone commands (redo).  Executing a new command clears the redo
 * stack (standard behaviour matching javax.swing.undo.UndoManager).
 *
 * Maximum history depth prevents unbounded memory growth.
 */
public final class CommandManager {

    private static final int MAX_HISTORY = 50;

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /** Execute a command and push it onto the undo stack. */
    public void execute(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();           // new action invalidates redo history
        trimStack(undoStack);
    }

    /** Undo the most recent command. */
    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return true;
    }

    /** Redo the most recently undone command. */
    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return true;
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    /** Description of the next undo/redo action (for tooltip). */
    public String peekUndoDescription() {
        return undoStack.isEmpty() ? "" : undoStack.peek().getDescription();
    }
    public String peekRedoDescription() {
        return redoStack.isEmpty() ? "" : redoStack.peek().getDescription();
    }

    /** Trim the stack to MAX_HISTORY to prevent memory leaks. */
    private void trimStack(Deque<Command> stack) {
        while (stack.size() > MAX_HISTORY) {
            ((ArrayDeque<Command>) stack).removeLast();
        }
    }
}
