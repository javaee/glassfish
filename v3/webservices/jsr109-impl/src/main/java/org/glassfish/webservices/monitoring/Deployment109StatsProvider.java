package org.glassfish.webservices.monitoring;

import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.Application;


/**
 * @author Jitendra Kotamraju
 */
@ManagedObject
@Description("Stats for Web Services deployed using 109 deployment")
public class Deployment109StatsProvider {

    private final ConcurrentHashMap<String, Deployment109EndpointData> endpoints =
            new ConcurrentHashMap<String, Deployment109EndpointData>();
    private final Logger logger = Logger.getLogger(Deployment109StatsProvider.class.getName());


    @ProbeListener("glassfish:webservices:109:deploy")
    public void deploy(@ProbeParam("name")String name,
                       @ProbeParam("app")Application app,
                       @ProbeParam("endpoint")WebServiceEndpoint endpoint) {
        Deployment109EndpointData data = new Deployment109EndpointData(app, endpoint);
        endpoints.put(name, data);
    }

    @ProbeListener("glassfish:webservices:109:undeploy")
    public void undeploy(@ProbeParam("name")String name) {
        endpoints.remove(name);
    }

    @ManagedAttribute
    @Description("Endpoints with 109 deployment")
    public Collection<Deployment109EndpointData> getEndpoints() {
        return Collections.unmodifiableCollection(endpoints.values());
    }

}
