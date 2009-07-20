/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.security;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 *
 * @author nithyasubramanian
 */
@ProbeProvider(moduleProviderName = "glassfish", moduleName = "core", probeProviderName = "web")
public class WebSecurityDeployerProbeProvider {

    @Probe(name = "webDeploymentStartedEvent")
    public void webDeploymentStartedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "webDeploymentEndedEvent")
    public void webDeploymentEndedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "policyGenerationStartedEvent")
    public void policyGenerationStartedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "policyGenerationEndedEvent")
    public void policyGenerationEndedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "webUndeploymentStartedEvent")
    public void webUndeploymentStartedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "webUndeploymentEndedEvent")
    public void webUndeploymentEndedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "policyRemovalStartedEvent")
    public void policyRemovalStartedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "policyRemovalEndedEvent")
    public void policyRemovalEndedEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "securityManagerCreationEvent")
    public void securityManagerCreationEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "securityManagerDestructionEvent")
    public void securityManagerDestructionEvent(
            @ProbeParam("appName") String appName) {
    }

    @Probe(name = "policyConfigurationCreationEvent")
    public void policyConfirationCreationEvent(
            @ProbeParam("contextId") String appName) {
    }

    @Probe(name = "policyConfigurationDestructionEvent")
    public void policyConfigurationDestructionEvent(
            @ProbeParam("contextId") String appName) {
    }
}
