package org.glassfish.webservices.monitoring;

import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;
import org.glassfish.gmbal.*;
import org.glassfish.webservices.deployment.DeployedEndpointData;

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

    // Only RI endpoints
    private final Map<String, List<DeployedEndpointData>> riEndpoints =
            new HashMap<String, List<DeployedEndpointData>>();

    // sun-jaxws.xml deployment
    @ProbeListener("glassfish:webservices:deployment-ri:deploy")
    public synchronized void riDeploy(@ProbeParam("adapter")ServletAdapter adapter) {
        String contextPath = adapter.getServletContext().getContextPath();
        String path = contextPath+adapter.getValidPath();
        DeployedEndpointData data = endpoints.get(path);
        if (data == null) {
            data = new DeployedEndpointData(path, adapter);
            endpoints.put(path, data);
        }

        List<DeployedEndpointData> ri = riEndpoints.get(contextPath);
        if (ri == null) {
            ri = new ArrayList<DeployedEndpointData>();
            riEndpoints.put(contextPath, ri);
        }
        ri.add(data);
    }

    // sun-jaxws.xml undeployment
    @ProbeListener("glassfish:webservices:deployment-ri:undeploy")
    public synchronized void riUndeploy(@ProbeParam("adapter")ServletAdapter adapter) {
        ServletContext ctxt = adapter.getServletContext();
        String name = ctxt.getContextPath()+adapter.getValidPath();
        DeployedEndpointData data = endpoints.remove(name);

        String contextPath = adapter.getServletContext().getContextPath();
        List<DeployedEndpointData> ri = riEndpoints.get(contextPath);
        if (ri != null) {
            ri.remove(data);
            if (ri.isEmpty()) {
                riEndpoints.remove(contextPath);
            }
        }
    }

    // admin CLI doesn't pick-up Collection<DeployedEndpointData>. Hence
    // implementing "Stats"
    @ManagedAttribute
    @Description("Deployed Web Service Endpoints")
    public synchronized MyStats getEndpoints() {
        return new MyStats(endpoints);
    }

    // Returns all the RI endpoints for context root
    @ManagedOperation
    public synchronized List<Map<String, String>> getRiEndpoint(String contextPath) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        List<DeployedEndpointData> ri = riEndpoints.get(contextPath);
        if (ri != null) {
            for(DeployedEndpointData de : ri) {
                list.add(de.getStaticAsMap());
            }
        }
        return list;
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
