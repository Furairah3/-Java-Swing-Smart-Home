package smarthome;

/**
 * DeviceObserver — Observer Pattern Interface
 *
 * Any class that wants to react to device state changes implements this.
 * The GUI log panel, room status labels, and analytics modules can all
 * be observers without the device knowing who they are.
 *
 * DESIGN PATTERN: Observer
 * - Decouples the Subject (SmartDevice) from its listeners.
 * - Push model: the event object carries all necessary context.
 */
@FunctionalInterface
public interface DeviceObserver {
    /**
     * Called by a SmartDevice whenever its state changes.
     * @param event  Immutable snapshot of the change that occurred.
     */
    void onDeviceEvent(DeviceEvent event);
}
