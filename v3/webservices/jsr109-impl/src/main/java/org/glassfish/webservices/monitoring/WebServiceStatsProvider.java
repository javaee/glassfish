package org.glassfish.webservices.monitoring;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;
import org.glassfish.gmbal.*;

import javax.servlet.ServletContext;
import java.util.*;


/**
 * Provides statistics for Web Service endpoints.
 * 
 * For deployment - keeps track of 109 and sun-jaxws.xml style deployed
 * applications.
 *
 * @author Jitendra Kotamraju
 */
@AMXMetadata(type="web-service-mon", group="monitoring")
@ManagedObject
@Description("Stats for Web Services deployed")
public class WebServiceStatsProvider {

    // path (context path+url-pattern) --> deployed data
    private final Map<String, DeployedEndpointData> endpoints =
            new HashMap<String, DeployedEndpointData>();

    // 109 Servlet Endpoints
    private final Map<Servlet109Endpoint, DeployedEndpointData> servletEndpoints =
            new HashMap<Servlet109Endpoint, DeployedEndpointData>();

    // 109 EJB Endpoints
    private final Map<EJB109Endpoint, DeployedEndpointData> ejbEndpoints =
            new HashMap<EJB109Endpoint, DeployedEndpointData>();

    // Only RI endpoints
    private final Map<String, DeployedEndpointData> riEndpoints =
            new HashMap<String, DeployedEndpointData>();

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

    // 109 endpoint deployment
    @ProbeListener("glassfish:webservices:deployment-109:deploy")
    public synchronized void eeDeploy(@ProbeParam("app")Application app,
                       @ProbeParam("endpoint")WebServiceEndpoint endpoint) {

        // path (context path+url-pattern) --> deployed data
        String path = endpoint.getEndpointAddressPath();
        DeployedEndpointData data = endpoints.get(path);
        if (data == null) {
            data = new DeployedEndpointData(path, app, endpoint);
            endpoints.put(path, data);
        }

        // store in servletEndpoints, ejbEndpoints
        String appName = app.getAppName();
        String moduleName = endpoint.getBundleDescriptor().getModuleName();
        String endpointName = endpoint.getEndpointName();
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

    // 109 enpoint undeployment
    @ProbeListener("glassfish:webservices:deployment-109:undeploy")
    public synchronized void eeUndeploy(@ProbeParam("path")String path) {
        endpoints.remove(path);

        // TODO remove from servletEndpoints, ejbEndpoints
    }

    // sun-jaxws.xml deployment
    @ProbeListener("glassfish:webservices:deployment-ri:deploy")
    public synchronized void riDeploy(@ProbeParam("adapter")ServletAdapter adapter) {
        String path = adapter.getServletContext().getContextPath()+adapter.getValidPath();
        if (!endpoints.containsKey(path)) {
            DeployedEndpointData data = new DeployedEndpointData(path, adapter);
            endpoints.put(path, data);
        }
    }

    // sun-jaxws.xml undeployment
    @ProbeListener("glassfish:webservices:deployment-ri:undeploy")
    public synchronized void riUndeploy(@ProbeParam("adapter")ServletAdapter adapter) {
        ServletContext ctxt = adapter.getServletContext();
        String name = ctxt.getContextPath()+adapter.getValidPath();
        endpoints.remove(name);
    }

    // admin CLI doesn't pick-up Collection<DeployedEndpointData>. Hence
    // implementing "Stats"
    @ManagedAttribute
    @Description("Deployed Web Service Endpoints")
    public synchronized MyStats getEndpoints() {
        return new MyStats(endpoints);
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

    // Returns all the RI endpoints for context root
    @ManagedOperation
    public synchronized List<Map<String, String>> getRiEndpoint(String contextPath) {
        return null; // TODO
    }

    @ManagedData
    private static class MyStats implements Stats {

        final Map<String, DeployedEndpointData> endpoints = new HashMap<String, DeployedEndpointData>();
        final DeployedEndpointData[] data;

        MyStats(Map<String, DeployedEndpointData> curEndpoints) {
            endpoints.putAll(curEndpoints);     // Take a snapshot of current endpoints
            data = this.endpoints.values().toArray(new DeployedEndpointData[endpoints.size()]);
        }

        public Statistic getStatistic(String s) {
            return endpoints.get(s);
        }

        public String[] getStatisticNames() {
            Set<String> names = endpoints.keySet();
            return names.toArray(new String[names.size()]);
        }

        @ManagedAttribute
        public DeployedEndpointData[] getStatistics() {
            return data;
        }
    }

}
