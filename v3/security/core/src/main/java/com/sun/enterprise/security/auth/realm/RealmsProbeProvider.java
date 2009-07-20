/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.security.auth.realm;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;
import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 *
 * @author nithyasubramanian
 */
@ProbeProvider(moduleProviderName="glassfish",moduleName="core",probeProviderName="realm")
public class RealmsProbeProvider {
    
    @Probe(name="realmAddedEvent")
    public void realmAddedEvent(
            @ProbeParam("realmName") String realmName
            )
    {}
    
    @Probe(name="realmRemovedEvent")
    public void realmRemovedEvent(
            @ProbeParam("realmName") String realmName
            )
    {}

}
