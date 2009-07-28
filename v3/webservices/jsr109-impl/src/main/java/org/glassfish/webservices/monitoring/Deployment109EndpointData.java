package org.glassfish.webservices.monitoring;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.Application;

/**
 * @author Jitendra Kotamraju
 */
@ManagedData
@Description("109 deployed endpoint info")
public class Deployment109EndpointData { // extends EndpointData {
    @ManagedAttribute
    @Description("Application Name")
    public final String appName;

    @ManagedAttribute
    @Description("Target Namespace of the Web Service")
    public final String namespace;

    @ManagedAttribute
    @Description("Web Service name")
    public final String serviceName;

    @ManagedAttribute
    @Description("Web Service port name")
    public final String portName;

    @ManagedAttribute
    @Description("Service Implementation Class")
    public final String implClass;

    @ManagedAttribute
    @Description("Address for Web Service")
    public final String address;

    @ManagedAttribute
    @Description("WSDL for Web Service")
    public final String wsdl;

    @ManagedAttribute
    @Description("Tester for Web Service")
    public final String tester;

    public Deployment109EndpointData(Application app, WebServiceEndpoint endpoint) {
        this.appName = app.getAppName();
        this.namespace = endpoint.getServiceName().getNamespaceURI();
        this.serviceName = endpoint.getServiceName().getLocalPart();
        this.portName = "";
        this.implClass = endpoint.getServletImplClass();
        this.address = endpoint.getEndpointAddressUri();
        this.wsdl = address+"?wsdl";
        this.tester = address+"?Tester";
    }

}
