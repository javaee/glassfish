/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.ejb.security.application;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 *
 * @author nithyasubramanian
 */
@ProbeProvider(moduleProviderName="glassfish",moduleName="ejb-container", probeProviderName="ejb")
public class EjbSecurityProbeProvider {
    
    @Probe(name="ejbSecDeploymentStartedEvent")
    public void ejbSecDeploymentStartedEvent(
            @ProbeParam("appName") String appName){}
    
    @Probe(name="ejbSecDeploymentEndedEvent")
    public void ejbSecDeploymentEndedEvent( 
            @ProbeParam("appName") String appName
            ) {}
    
    
    @Probe(name="ejbPCCreationStartEvent")
    public void ejbPCCreationStartEvent(
            @ProbeParam("contextId") String contextId
            ) {}
    
    @Probe(name="ejbPCDestructionStartEvent")
    public void ejbPCDestructionStartEvent(
            @ProbeParam("contextId") String contextId
            ) {}
    
    
}
