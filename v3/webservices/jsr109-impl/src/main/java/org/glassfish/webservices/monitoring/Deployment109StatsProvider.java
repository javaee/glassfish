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


/**
 * @author Jitendra Kotamraju
 */
@ManagedObject
@Description("Stats for  Web Services deployed using 109 deployment")
public class Deployment109StatsProvider {

    private final ConcurrentHashMap<String, Deployment109EndpointData> endpoints =
            new ConcurrentHashMap<String, Deployment109EndpointData>();
    private final Logger logger = Logger.getLogger(Deployment109StatsProvider.class.getName());

    public Deployment109StatsProvider() {
        endpoints.put("name1", new Deployment109EndpointData("namespace1", "serviceName1",
            "portName1", "implClass1", "address1", "address1?wsdl"));
        endpoints.put("name2", new Deployment109EndpointData("namespace2", "serviceName2",
            "portName2", "implClass2", "address2", "address2?wsdl"));
    }

    @ProbeListener("glassfish:webservices:109:deploy")
    public void deploy(@ProbeParam("name")String name,
                       @ProbeParam("address")String address,
                       @ProbeParam("serviceName")String serviceName,
                       @ProbeParam("portName")String portName,
                       @ProbeParam("namespace")String namespace,
                       @ProbeParam("implClass")String implClass,
                       @ProbeParam("wsdl")String wsdl) {
        endpoints.put(name, new Deployment109EndpointData(namespace, serviceName,
            portName, implClass, address, wsdl));
    }

    @ProbeListener("glassfish:webservices:109:undeploy")
    public void undeploy(@ProbeParam("name")String appName) {
        endpoints.remove(appName);
    }

    @ManagedAttribute
    @Description("Endpoints with 109 deployment")
    public Collection<Deployment109EndpointData> getEndpoints() {
        return Collections.unmodifiableCollection(endpoints.values());
    }

    /*package*/ Deployment109EndpointData getEndpoint(String appName){
        return endpoints.get(appName);
    }

}
