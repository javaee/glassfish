package org.glassfish.webservices;

import org.glassfish.api.invocation.ComponentInvocation;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.rpc.spi.runtime.Handler;

/**
 * This class stores information for EjbRuntimeInfo
 * It stores the invocation object and the servlet Adapter
 *
 * @author Bhakti Mehta
 */
public class AdapterInvocationInfo {

    /**
     * This will store information about the inv which needs to
     * be started and ended  by the StatelessSessionContainer
     */
    private ComponentInvocation inv;

    /**
     * This will store information about the ServletAdapter which
     * wil be used to publish the wsdl
     */
    private  ServletAdapter adapter;

    /**
     * This has the JAXRPC handler stored for alter use
     * @param handler
     */
    private Handler handler;

    public void setAdapter(ServletAdapter adapter) {
        this.adapter = adapter;
    }

    public void setInv(ComponentInvocation inv) {
        this.inv = inv;
    }

    public void setHandler(Handler h) {
        this.handler = h;
    }

    public ServletAdapter getAdapter() {
        return adapter;
    }

    public ComponentInvocation getInv() {
        return inv;
    }

    public Handler getHandler() {
        return handler;
    }
}
