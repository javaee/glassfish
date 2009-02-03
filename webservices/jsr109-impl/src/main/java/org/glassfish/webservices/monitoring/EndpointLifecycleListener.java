package org.glassfish.webservices.monitoring;


/**
 * This listener interface provides facility to receive notifications
 * when a new Web Service endpoint has been added/removed to/from the
 * appserver runtime.
 *
 * @author Jerome Dochez
 */
public interface EndpointLifecycleListener {

    /**
     * Notification of a new Web Service endpoint installation in the
     * appserver.
     * @param endpoint endpoint to register SOAPMessageListener if needed.
     */
    public void endpointAdded(Endpoint endpoint);

    /**
     * Notification of a Web Service endpoint removal from the appserver
     * @param endpoint handler to register SOAPMessageListener if needed.
     */
    public void endpointRemoved(Endpoint endpoint);

}