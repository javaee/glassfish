package org.glassfish.webservices.monitoring;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.external.statistics.impl.StatisticImpl;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.Application;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * 109 and sun-jaxws.xml style deployed endpoint's info.
 *
 * @author Jitendra Kotamraju
 */
@ManagedData
@Description("109 deployed endpoint info")
public class DeployedEndpointData extends StatisticImpl {

    @ManagedAttribute
    @Description("Application Name")
    public final String appName;

    @ManagedAttribute
    @Description("Endpoint Name")
    public final String endpointName;

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

    @ManagedAttribute
    @Description("Implementation Type: EJB or SERVLET")
    public final String implType;

    @ManagedAttribute
    @Description("Deployment Type: 109 or RI")
    public final String deploymentType;

    // 109 deployed endpoint
    public DeployedEndpointData(String path, Application app, WebServiceEndpoint endpoint) {
        super(path, "", "");
        this.appName = app.getAppName();
        this.endpointName = endpoint.getEndpointName();
        this.namespace = endpoint.getServiceName().getNamespaceURI();
        this.serviceName = endpoint.getServiceName().getLocalPart();
        QName pName = endpoint.getWsdlPort();
        this.portName = (pName != null) ? pName.getLocalPart() : "";
        this.implClass = endpoint.implementedByEjbComponent()
                ? endpoint.getEjbComponentImpl().getEjbImplClassName()
                : endpoint.getServletImplClass();
        this.address = path;
        this.wsdl = address+"?wsdl";
        this.tester = address+"?Tester";
        this.implType = endpoint.implementedByEjbComponent() ? "EJB" : "SERVLET";
        this.deploymentType = "109";
        fillStatMap();
    }

    // sun-jaxws.xml deployed endpoint
    public DeployedEndpointData(String path, ServletAdapter adapter) {
        super(path, "", "");
        WSEndpoint endpoint = adapter.getEndpoint();

        this.appName = "";
        this.endpointName = adapter.getName();
        this.namespace = endpoint.getServiceName().getNamespaceURI();
        this.serviceName = endpoint.getServiceName().getLocalPart();
        this.portName = endpoint.getPortName().getLocalPart();
        this.implClass = endpoint.getImplementationClass().getName();
        this.address = path;
        this.wsdl = address+"?wsdl";
        this.tester = "";
        this.implType = "SERVLET";
        this.deploymentType = "RI";
        fillStatMap();
    }

    @Override
    public Map getStaticAsMap() {
        return statMap;
    }

    private void fillStatMap() {
        Map m = super.getStaticAsMap();

        m.put("appName", appName);
        m.put("endpointName", endpointName);
        m.put("namespace", namespace);
        m.put("serviceName", serviceName);
        m.put("portName", portName);
        m.put("implClass", implClass);
        m.put("address", address);
        m.put("wsdl", wsdl);
        m.put("tester", tester);
        m.put("implType", implType);
        m.put("deploymentType", deploymentType);

    }

}
