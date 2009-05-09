package org.glassfish.webservices;

import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import com.sun.grizzly.http.servlet.ServletAdapter;


/**
 * This class extends the ServletAdapter and sets the servletInstance to the EjbWebServiceServlet
 * so that its service method is invoked whenever a request maps to this Adapter
 */
public class EjbWSAdapter extends ServletAdapter {

    public EjbWSAdapter() {
        this.setServletInstance(new EjbWebServiceServlet());
    }


}

