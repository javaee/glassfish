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
    private final Map<String, DeployedEndpointData> endpoints =
            new HashMap<String, DeployedEndpointData>();

    // appName --> module name --> endpoint name --> deployed data
    private final Map<String, Map<String, Map<String, DeployedEndpointData>>> oneONine =
        new HashMap<String, Map<String, Map<String, DeployedEndpointData>>>();

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

        // store as appName --> module name --> endpoint name --> deployed data
        String appName = app.getAppName();
        String moduleName = endpoint.getBundleDescriptor().getModuleName();
        String endpointName = endpoint.getEndpointName();
        Map<String, Map<String, DeployedEndpointData>> module = oneONine.get(appName);
        if (module == null) {
            module = new ConcurrentHashMap<String, Map<String, DeployedEndpointData>>();
            oneONine.put(appName, module);
        }
        Map<String, DeployedEndpointData> moduleData = module.get(moduleName);
        if (moduleData == null) {
            moduleData = new ConcurrentHashMap<String, DeployedEndpointData>();
            module.put(moduleName, moduleData);
        }
        DeployedEndpointData endpointData = moduleData.get(endpointName);
        if (endpointData == null) {
            moduleData.put(endpointName, data);
        }
    }

    // 109 enpoint undeployment
    @ProbeListener("glassfish:webservices:deployment-109:undeploy")
    public synchronized void eeUndeploy(@ProbeParam("path")String path) {
        endpoints.remove(path);
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

    // Returns all the 109 endpoints in a given application's module
    @ManagedOperation
    public synchronized List<Map<String, String>> get109Endpoints(String appName, String moduleName) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        Map<String, Map<String, DeployedEndpointData>> app = oneONine.get(appName);
        if (app != null) {
            Map<String, DeployedEndpointData> module=  app.get(moduleName);
            if (module != null) {
                for(DeployedEndpointData data : module.values()) {
                    list.add(data.getStaticAsMap());
                }
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
