package org.glassfish.webservices.monitoring;

import org.glassfish.probe.provider.annotations.ProbeProvider;
import org.glassfish.probe.provider.annotations.Probe;
import org.glassfish.probe.provider.annotations.ProbeParam;

/**
 * 109 deployment probe. A registered listener get to listen the emited
 * 109 deployment/undepolyment events.
 *
 * @author Jitendra Kotamraju
 */
@ProbeProvider(moduleProviderName="glassfish", moduleName="webservices", probeProviderName="109")
public class Deployment109ProbeProvider {

    @Probe(name="deploy")
    public void deploy(@ProbeParam("name")String name,
                       @ProbeParam("address")String address,
                       @ProbeParam("serviceName")String serviceName,
                       @ProbeParam("portName")String portName,
                       @ProbeParam("namespace")String namespace,
                       @ProbeParam("implClass")String implClass,
                       @ProbeParam("wsdl")String wsdl) {
        // intentionally left empty.
    }

    @Probe(name="undeploy")
    public void undeploy(@ProbeParam("name")String name) {
        // intentionally left empty.
    }
    
}