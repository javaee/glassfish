package org.glassfish.webservices.deployment;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.gmbal.*;
import org.glassfish.webservices.deployment.DeployedEndpointData;

import java.util.*;


/**
 * MBean that provides deployed Web Service endpoints.
 *
 * Keeps track of 109 deployed applications.
 *
 * @author Jitendra Kotamraju
 */
@AMXMetadata(type="web-service-mon", group="monitoring")
@ManagedObject
@Description("Deployed Web Services")
public class WebServicesDeploymentMBean {

    // All 109 endpoints
    // appName+moduleName+endpointName --> deployed data
    private final Map<String, DeployedEndpointData> endpoints =
            new HashMap<String, DeployedEndpointData>();

    // 109 Servlet Endpoints
    private final Map<Servlet109Endpoint, DeployedEndpointData> servletEndpoints =
            new HashMap<Servlet109Endpoint, DeployedEndpointData>();

    // 109 EJB Endpoints
    private final Map<EJB109Endpoint, DeployedEndpointData> ejbEndpoints =
            new HashMap<EJB109Endpoint, DeployedEndpointData>();

    private static class Endpoint {
        final String appName;
        final String moduleName;
        final String endpointName;

        Endpoint(String appName, String moduleName, String endpointName) {
            this.appName = appName;
            this.moduleName = moduleName;
            this.endpointName = endpointName;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Endpoint) {
                Endpoint other = (Endpoint)obj;
                if (appName.equals(other.appName) && moduleName.equals(other.moduleName) && endpointName.equals(other.endpointName))
                    return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return appName.hashCode()+moduleName.hashCode()+endpointName.hashCode();
        }

        @Override
        public String toString() {
            return appName+"#"+moduleName+"#"+endpointName;
        }
    }

    private static class Servlet109Endpoint {
        final String appName;
        final String moduleName;
        final String servletLink;

        Servlet109Endpoint(String appName, String moduleName, String servletLink) {
            this.appName = appName;
            this.moduleName = moduleName;
            this.servletLink = servletLink;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Servlet109Endpoint) {
                Servlet109Endpoint other = (Servlet109Endpoint)obj;
                if (appName.equals(other.appName) && moduleName.equals(other.moduleName) && servletLink.equals(other.servletLink))
                    return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return appName.hashCode()+moduleName.hashCode()+servletLink.hashCode();
        }
    }

    private static class EJB109Endpoint {
        final String appName;
        final String moduleName;
        final String ejbLink;

        EJB109Endpoint(String appName, String moduleName, String ejbLink) {
            this.appName = appName;
            this.moduleName = moduleName;
            this.ejbLink = ejbLink;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EJB109Endpoint) {
                EJB109Endpoint other = (EJB109Endpoint)obj;
                if (appName.equals(other.appName) && moduleName.equals(other.moduleName) && ejbLink.equals(other.ejbLink))
                    return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return appName.hashCode()+moduleName.hashCode()+ejbLink.hashCode();
        }
    }

    public synchronized void deploy(@ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        // add to [appName+moduleName+endpointName --> deployed data]
        Application app = endpoint.getBundleDescriptor().getApplication();
        String appName = app.getAppName();
        String moduleName = endpoint.getBundleDescriptor().getModuleName();
        String endpointName = endpoint.getEndpointName();

        // path (context path+url-pattern) --> deployed data
        String id = new Endpoint(appName, moduleName, endpointName).toString();
        String path = endpoint.getEndpointAddressPath();
        DeployedEndpointData data = endpoints.get(id);
        if (data == null) {
            data = new DeployedEndpointData(path, app, endpoint);
            endpoints.put(id, data);
        }

        // store in servletEndpoints, ejbEndpoints
        if (endpoint.getWebComponentLink() != null) {
            Servlet109Endpoint se = new Servlet109Endpoint(appName, moduleName, endpoint.getWebComponentLink());
            if (!servletEndpoints.containsKey(se)) {
                servletEndpoints.put(se, data);
            }
        } else if (endpoint.getEjbLink() != null) {
            EJB109Endpoint ee = new EJB109Endpoint(appName, moduleName, endpoint.getEjbLink());
            if (!ejbEndpoints.containsKey(ee)) {
                ejbEndpoints.put(ee, data);
            }
        } else {
            throw new RuntimeException("Both servlet-link and ejb-link are null for appName="
                    +appName+" moduleName="+moduleName+" endpointName="+endpointName);
        }
    }

    public synchronized void undeploy(@ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        // remove from [appName+moduleName+endpointName --> deployed data]
        Application app = endpoint.getBundleDescriptor().getApplication();
        String appName = app.getAppName();
        String moduleName = endpoint.getBundleDescriptor().getModuleName();
        String endpointName = endpoint.getEndpointName();
        String id = new Endpoint(appName, moduleName, endpointName).toString();
        endpoints.remove(id);

        // remove from servletEndpoints, ejbEndpoints
        if (endpoint.getWebComponentLink() != null) {
            Servlet109Endpoint se = new Servlet109Endpoint(appName, moduleName, endpoint.getWebComponentLink());
            servletEndpoints.remove(se);
        } else if (endpoint.getEjbLink() != null) {
            EJB109Endpoint ee = new EJB109Endpoint(appName, moduleName, endpoint.getEjbLink());
            ejbEndpoints.remove(ee);
        } else {
            throw new RuntimeException("Both servlet-link and ejb-link are null for appName="
                    +appName+" moduleName="+moduleName+" endpointName="+endpointName);
        }
    }

    @ManagedAttribute
    @Description("Deployed Web Service Endpoints")
    public synchronized Map<String, DeployedEndpointData> getEndpoints() {
        // Give a snapshot of all the endpoints
        return new HashMap<String, DeployedEndpointData>(endpoints);
    }

    // Returns the 109 servlet endpoint for appName+moduleName+servletLink
    @ManagedOperation
    public synchronized Map<String, String> getServlet109Endpoint(String appName, String moduleName, String servletLink) {
        Servlet109Endpoint endpoint = new Servlet109Endpoint(appName, moduleName, servletLink);
        DeployedEndpointData data = servletEndpoints.get(endpoint);
        return (data == null) ? Collections.<String, String>emptyMap() : data.getStaticAsMap();
    }

    // Returns all the 109 EJB endpoint for appName+moduleName+ejbLink
    @ManagedOperation
    public synchronized Map<String, String> getEjb109Endpoint(String appName, String moduleName, String ejbLink) {
        EJB109Endpoint endpoint = new EJB109Endpoint(appName, moduleName, ejbLink);
        DeployedEndpointData data = ejbEndpoints.get(endpoint);
        return (data == null) ? Collections.<String, String>emptyMap() : data.getStaticAsMap();
    }

}