package org.glassfish.webservices.monitoring;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import com.sun.xml.ws.api.server.WSEndpoint;

/**
 * @author Jitendra Kotamraju
 */

@ManagedData
@Description("sun-jaxws.xml deployed endpoint info")
public class Deployment109EndpointData { // extends EndpointData {
    private final String namespace;
    private final String serviceName;
    private String portName;
    private String implClass;
    private String address;
    private String wsdl;

    public Deployment109EndpointData(String namespace, String serviceName,
                                     String portName, String implClass,
                                     String address, String wsdl) {
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.portName = portName;
        this.implClass = implClass;
        this.address = address;
        this.wsdl = wsdl;
    }

    @ManagedAttribute
    @Description("Target Namespace of the Web Service")
    public String getNamespace() {
        return namespace;
    }

    @ManagedAttribute
    @Description("Web Service name")
    public String getServiceName() {
        return serviceName;
    }

    @ManagedAttribute
    @Description("Web Service port name")
    public String getPortName() {
        return portName;
    }

    @ManagedAttribute
    @Description("Service Implementation Class")
    public String getImplClass() {
        return implClass;
    }

    @ManagedAttribute
    @Description("Address for Web Service")
    public String getAddress() {
        return address;
    }

    @ManagedAttribute
    @Description("WSDL for Web Service")
    public String getWSDL() {
        return wsdl;
    }

}
