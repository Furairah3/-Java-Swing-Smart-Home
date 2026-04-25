package smarthome;

import java.util.ArrayDeque;
import java.util.Deque;

// I made this class to handle the undo and redo buttons.
// I keep two stacks: one for things I already did, one for things I undid.
// When the user does something new, I clear the redo stack so it doesn't get confusing.
public final class CommandManager {

    // I picked 50 as the max so the app doesn't use too much memory if the user clicks forever.
    private static final int MAX_HISTORY = 50;

    // I push every command I run onto this stack so I can undo it later.
    private final Deque<Command> undoStack = new ArrayDeque<>();
    // I push undone commands here in case the user wants to redo them.
    private final Deque<Command> redoStack = new ArrayDeque<>();

    // I run the command, then I save it for undo and clear the redo stack.
    public void execute(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();           // I clear redo because new actions break the redo chain.
        trimStack(undoStack);
    }

    // I take the last command and undo it, then I move it to the redo stack.
    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        Command cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return true;
    }

    // I take the last undone command and redo it, then I put it back on the undo stack.
    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        Command cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return true;
    }

    // I made these so the GUI knows when to enable or disable the undo and redo buttons.
    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    // I made these to show what the next undo or redo would actually do (for the tooltip).
    public String peekUndoDescription() {
        return undoStack.isEmpty() ? "" : undoStack.peek().getDescription();
    }
    public String peekRedoDescription() {
        return redoStack.isEmpty() ? "" : redoStack.peek().getDescription();
    }

    // If the stack gets too big, I throw away the oldest stuff so memory stays okay.
    private void trimStack(Deque<Command> stack) {
        while (stack.size() > MAX_HISTORY) {
            ((ArrayDeque<Command>) stack).removeLast();
        }
    }
}
