package smarthome;

// I made this interface so anything that wants to know when a device changes can listen.
// For example, my GUI uses this so it knows when to update the buttons and the log.
// I marked it @FunctionalInterface so it only has one method and I can use lambdas with it.
@FunctionalInterface
public interface DeviceObserver {
    // I made this method so devices can call it on me whenever something changes.
    // The event has all the info I need (which device, what type, ON or OFF, etc).
    void onDeviceEvent(DeviceEvent event);
}
