package org.glassfish.webservices.monitoring;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedObject;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


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
    private final ConcurrentHashMap<String, DeployedEndpointData> endpoints =
            new ConcurrentHashMap<String, DeployedEndpointData>();

    // 109 endpoint deployment
    @ProbeListener("glassfish:webservices:deployment-109:deploy")
    public void eeDeploy(@ProbeParam("app")Application app,
                       @ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        String path = endpoint.getEndpointAddressPath();
        if (!endpoints.containsKey(path)) {
            DeployedEndpointData data = new DeployedEndpointData(path, app, endpoint);
            endpoints.put(path, data);
        }
    }

    // 109 enpoint undeployment
    @ProbeListener("glassfish:webservices:deployment-109:undeploy")
    public void eeUndeploy(@ProbeParam("path")String path) {
        endpoints.remove(path);
    }

    // sun-jaxws.xml deployment
    @ProbeListener("glassfish:webservices:deployment-ri:deploy")
    public void riDeploy(@ProbeParam("adapter")ServletAdapter adapter) {
        String path = adapter.getServletContext().getContextPath()+adapter.getValidPath();
        if (!endpoints.containsKey(path)) {
            DeployedEndpointData data = new DeployedEndpointData(path, adapter);
            endpoints.put(path, data);
        }
    }

    // sun-jaxws.xml undeployment
    @ProbeListener("glassfish:webservices:deployment-ri:undeploy")
    public void riUndeploy(@ProbeParam("adapter")ServletAdapter adapter) {
        ServletContext ctxt = adapter.getServletContext();
        String name = ctxt.getContextPath()+adapter.getValidPath();
        endpoints.remove(name);
    }

    // admin CLI doesn't pick-up Collection<DeployedEndpointData>. Hence
    // implementing "Stats"
    @ManagedAttribute
    @Description("Deployed Web Service Endpoints")
    public MyStats getEndpoints() {
        return new MyStats(endpoints);
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
