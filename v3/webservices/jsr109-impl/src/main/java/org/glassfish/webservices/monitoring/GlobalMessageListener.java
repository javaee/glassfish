package org.glassfish.webservices.monitoring;

import org.glassfish.webservices.SOAPMessageContext;


/**
 * This interface permits implementors to register a global message listener
 * which will be notified for all the web services requests and responses
 * on installed and enabled Web Services. Each invocation will be notified
 * through founr callbacks (preProcessRequest, processRequest, processResponse,
 * postProcessResponse).
 *
 * @author Jerome Dochez
 */
public interface GlobalMessageListener {

    /**
     * Callback when a web service request entered the web service container
     * and before any system processing is done.
     * @param endpoint is the endpoint the web service request is targeted to
     * @return a message ID to trace the request in the subsequent callbacks
     * or null if this invocation should not be traced further.
     */
    public String preProcessRequest(Endpoint endpoint);

    /**
     * Callback when a 1.X web service request is about the be delivered to the
     * Web Service Implementation Bean.
     * @param mid message ID returned by preProcessRequest call
     * @param ctx the jaxrpc message trace, transport dependent
     */
    public void processRequest(String mid, com.sun.xml.rpc.spi.runtime.SOAPMessageContext ctx, TransportInfo info);

    /**
     * Callback when a 1.X web service response was returned by the Web Service
     * Implementation Bean
     * @param mid message ID returned by the preProcessRequest call
     * @param ctx jaxrpc message trace, transport dependent.
     */
    public void processResponse(String mid, com.sun.xml.rpc.spi.runtime.SOAPMessageContext ctx);
   
    /**
     * Callback when a 2.X web service request is about the be delivered to the
     * Web Service Implementation Bean.
     * @param mid message ID returned by preProcessRequest call
     * @param ctx the jaxrpc message trace, transport dependent
     */
    public void processRequest(String mid, SOAPMessageContext ctx, TransportInfo info);

    /**
     * Callback when a 2.X web service response was returned by the Web Service
     * Implementation Bean
     * @param mid message ID returned by the preProcessRequest call
     * @param ctx jaxrpc message trace, transport dependent.
     */
    public void processResponse(String mid, SOAPMessageContext ctx);

    /**
     * Callback when a web service response has finished being processed
     * by the container and was sent back to the client
     * @param mid returned by the preProcessRequest call
     * @param info the response transport dependent information
     */
    public void postProcessResponse(String mid, TransportInfo info);

}

