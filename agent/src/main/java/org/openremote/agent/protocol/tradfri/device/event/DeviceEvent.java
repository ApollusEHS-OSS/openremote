package org.openremote.agent.protocol.tradfri.device.event;

import org.openremote.agent.protocol.tradfri.device.Device;
import org.openremote.agent.protocol.tradfri.device.event.Event;

/**
 * The class that represents a device event that occurred to an IKEA TRÅDFRI device
 * @author Stijn Groenen
 * @version 1.0.0
 */
public class DeviceEvent extends Event {

    /**
     * The device for which the event occurred
     */
    private Device device;

    /**
     * Construct the DeviceEvent class
     * @param device The device for which the event occurred
     * @since 1.0.0
     */
    public DeviceEvent(Device device) {
        super();
        this.device = device;
    }

    /**
     * Get the device for which the event occurred
     * @return The device for which the event occurred
     * @since 1.0.0
     */
    public Device getDevice(){
        return this.device;
    }

}
