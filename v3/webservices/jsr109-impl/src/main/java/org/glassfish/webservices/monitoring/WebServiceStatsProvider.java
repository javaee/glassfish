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

import javax.servlet.ServletContext;


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

    // path (context path+url-pattern) --> deployed data
    private final ConcurrentHashMap<String, DeployedEndpointData> endpoints =
            new ConcurrentHashMap<String, DeployedEndpointData>();

    // 109 endpoint deployment
    @ProbeListener("glassfish:webservices:109:deploy")
    public void eeDeploy(@ProbeParam("app")Application app,
                       @ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        String path = endpoint.getEndpointAddressPath();
        if (!endpoints.containsKey(path)) {
            DeployedEndpointData data = new DeployedEndpointData(app, endpoint);
            endpoints.put(path, data);
        }
    }

    // 109 enpoint undeployment
    @ProbeListener("glassfish:webservices:109:undeploy")
    public void eeUndeploy(@ProbeParam("path")String path) {
        endpoints.remove(path);
    }


//    Uncomment after latest metro is integrated
//
//    // sun-jaxws.xml deployment
//    @ProbeListener("glassfish:webservices:ri:deploy")
//    public void riDeploy(@ProbeParam("adapter")ServletAdapter adapter) {
//        ServletContext ctxt = adapter.getServletContext();
//        String name = ctxt.getContextPath()+adapter.getValidPath();
//        if (!endpoints.containsKey(name)) {
//            DeployedEndpointData data = new DeployedEndpointData(adapter);
//            endpoints.put(name, data);
//        }
//    }
//
//    // sun-jaxws.xml undeployment
//    @ProbeListener("glassfish:webservices:ri:undeploy")
//    public void riUndeploy(@ProbeParam("adapter")ServletAdapter adapter) {
//        ServletContext ctxt = adapter.getServletContext();
//        String name = ctxt.getContextPath()+adapter.getValidPath();
//        endpoints.remove(name);
//    }

    @ManagedAttribute
    @Description("Deployed Web Service Endpoints")
    public Collection<DeployedEndpointData> getEndpoints() {
        return Collections.unmodifiableCollection(endpoints.values());
    }

}
