package smarthome;

// I made this interface so any action in my app can be undone or redone.
// Every command needs an execute method to do the action and an undo to take it back.
// I also added a description so I can show the user what the command does.
public interface Command {

    // I call this when I want to do the action.
    void execute();

    // I call this when I want to take the action back.
    void undo();

    // I use this to get a short text describing what the command did.
    String getDescription();
}
