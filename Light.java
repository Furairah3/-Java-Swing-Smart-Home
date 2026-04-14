package smarthome;

/**
 * Light — Concrete SmartDevice
 *
 * Created exclusively through {@link DeviceFactory} (Factory Pattern).
 * Package-private constructor enforces this constraint.
 */
public class Light extends SmartDevice {

    public Light(String name, String room) {
        super(name, room);
    }

    @Override
    public String getDeviceType() {
        return "Light";
    }
}
