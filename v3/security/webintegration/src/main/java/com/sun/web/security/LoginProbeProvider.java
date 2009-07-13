/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.web.security;

import org.glassfish.probe.provider.annotations.Probe;
import org.glassfish.probe.provider.annotations.ProbeParam;
import org.glassfish.probe.provider.annotations.ProbeProvider;

/**
 *
 * @author nithyasubramanian
 */
@ProbeProvider(moduleProviderName="security",moduleName="webintegration",probeProviderName="login" )
public class LoginProbeProvider {
    
    @Probe(name="loginSuccessfulEvent")
    public void loginSuccessfulEvent(@ProbeParam("username") String username){}
    
    @Probe(name="loginFailedEvent")
    public void loginFailedEvent(@ProbeParam("username") String username){}

}
