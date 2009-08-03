package org.glassfish.webservices.monitoring;

import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.Application;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;


/**
 * Provides statistics for Web Service endpoints.
 * 
 * For deployment - keeps track of 109 and sun-jaxws.xml style deployed
 * applications.
 *
 * @author Jitendra Kotamraju
 */
@ManagedObject
@Description("Stats for Web Services deployed")
public class WebServiceStatsProvider {

    private final ConcurrentHashMap<String, DeployedEndpointData> endpoints =
            new ConcurrentHashMap<String, DeployedEndpointData>();

    // 109 endpoint deployment
    @ProbeListener("glassfish:webservices:109:deploy")
    public void eeDeploy(@ProbeParam("name")String name,
                       @ProbeParam("app")Application app,
                       @ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        if (!endpoints.containsKey(name)) {
            DeployedEndpointData data = new DeployedEndpointData(app, endpoint);
            endpoints.put(name, data);
        }
    }

    // 109 enpoint undeployment
    @ProbeListener("glassfish:webservices:109:undeploy")
    public void eeUndeploy(@ProbeParam("name")String name) {
        endpoints.remove(name);
    }

//    Add it after latest metro is integrated
//
//    // sun-jaxws.xml deployment
//    @ProbeListener("glassfish:webservices:ri:deploy")
//    public void riDeploy(@ProbeParam("adapter")ServletAdapter adapter) {
//        String name = adapter.getName();        // TODO use context+urlpattern
//        if (!endpoints.containsKey(name)) {
//            DeployedEndpointData data = new DeployedEndpointData(adapter);
//            endpoints.put(name, data);
//        }
//    }
//
//    // sun-jaxws.xml undeployment
//    @ProbeListener("glassfish:webservices:ri:undeploy")
//    public void riUndeploy(@ProbeParam("adapter") ServletAdapter adapter) {
//        String name = adapter.getName();        // TODO use context+urlpattern
//        endpoints.remove(name);
//    }

    @ManagedAttribute
    @Description("Deployed Web Service Endpoints")
    public Collection<DeployedEndpointData> getEndpoints() {
        return Collections.unmodifiableCollection(endpoints.values());
    }

}
