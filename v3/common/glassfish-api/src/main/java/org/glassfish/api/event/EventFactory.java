package org.glassfish.api.event;

import org.jvnet.hk2.annotations.Contract;

/**
 * User: Jerome Dochez
 * Date: May 22, 2008
 * Time: 4:34:37 PM
 */
@Contract
public interface EventFactory {

    public void register(EventListener listener);

    public void send(EventListener.Event event);
}
