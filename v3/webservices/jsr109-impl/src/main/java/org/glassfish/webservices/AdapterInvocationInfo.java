package org.glassfish.webservices;

import org.glassfish.api.invocation.ComponentInvocation;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;

/**
 * This class stores information for EjbRuntimeInfo
 * It stores the invocation object and the servlet Adapter
 */
public class AdapterInvocationInfo {


    private ComponentInvocation inv;

    private  ServletAdapter adapter;

    public void setAdapter(ServletAdapter adapter) {
        this.adapter = adapter;
    }

    public void setInv(ComponentInvocation inv) {
        this.inv = inv;
    }



    public ServletAdapter getAdapter() {
        return adapter;
    }

    public ComponentInvocation getInv() {
        return inv;
    }




}
