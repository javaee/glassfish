/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.security;

import org.glassfish.api.statistics.CountStatistic;
import org.glassfish.api.statistics.TimeStatistic;
import org.glassfish.api.statistics.impl.CountStatisticImpl;
import org.glassfish.api.statistics.impl.TimeStatisticImpl;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.probe.provider.annotations.ProbeListener;
import org.glassfish.probe.provider.annotations.ProbeParam;

/**
 *
 * @author nithyasubramanian
 */
@ManagedObject
@Description( "Web application Security Deployment statistics" )
public class WebSecurityDeployerStatsProvider {
    
    TimeStatisticImpl deploymentTime = null;
    
    TimeStatisticImpl generationTime = null;
        
    TimeStatisticImpl undeploymentTime = null;
    
    TimeStatisticImpl removalTime = null;
    
    CountStatisticImpl secMgrCount = null;
    
    CountStatisticImpl policyConfCount = null;
    
    @ManagedAttribute(id="DepolymentTime")
    public TimeStatistic getDeploymentTime() {
        deploymentTime = new TimeStatisticImpl(0, 0, 0, 0, "deploymentTime", "milliseconds", "Deployment Time", 0, 0);
        return deploymentTime.getStatistic();
    }
        
    @ManagedAttribute(id="GenerationTime")
    public TimeStatistic getGenerationTime() {
        generationTime = new TimeStatisticImpl(0, 0, 0, 0, "generationTime", "milliseconds", "Generation Time", 0, 0);
        return generationTime.getStatistic();
    }
    
    @ManagedAttribute(id="UndepolymentTime")
    public TimeStatistic getUndeploymentTime() {
        undeploymentTime = new TimeStatisticImpl(0, 0, 0, 0, "undeploymentTime", "milliseconds", "Undeployment Time", 0, 0);
        return undeploymentTime.getStatistic();
    }
    
    
    @ManagedAttribute(id="RemovalTime")
    public TimeStatistic getRemovalTime() {
        removalTime = new TimeStatisticImpl(0, 0, 0, 0, "removalTime", "milliseconds", "Removal Time", 0, 0);
        return removalTime.getStatistic();
    }
    
    @ManagedAttribute(id="WebSecurityManagerCount")
    public CountStatistic getWebSMCount() {
        secMgrCount = new CountStatisticImpl("WebSecurityManagerCount", "count", "No of Web security managers");
        return secMgrCount.getStatistic();
       
    }
    
    @ManagedAttribute(id="WebPolicyConfigurationCount")
    public CountStatistic getPCCount() {
        policyConfCount= new CountStatisticImpl("WebPolicyConfigurationCount", "count", "No of Policy Configuration Objects");
        return policyConfCount.getStatistic();
    }    
    
    @ProbeListener("glassfish:core:web:webDeploymentStartedEvent")
    public void webDeploymentStartedEvent(@ProbeParam("appName")String appName){
       deploymentTime.setStartTime(System.currentTimeMillis());
    }
    
    @ProbeListener("glassfish:core:web:webDeploymentEndedEvent")
    public void webDeploymentEndedEvent(@ProbeParam("appName")String appName){
       
    }
    
    @ProbeListener("glassfish:core:web:securityManagerCreationEvent")
    public void securityManagerCreationEvent(
            @ProbeParam("appName") String appName) {
        secMgrCount.increment();
    }

    @ProbeListener("glassfish:core:web:securityManagerDestructionEvent")
    public void securityManagerDestructionEvent(
            @ProbeParam("appName") String appName) {
        secMgrCount.decrement();
    }

    @ProbeListener("glassfish:core:web:policyConfigurationCreationEvent")
    public void policyConfirationCreationEvent(
            @ProbeParam("contextId") String contextId) {
        policyConfCount.increment();
    }

    @ProbeListener("glassfish:core:web:policyConfigurationDestructionEvent")
    public void policyConfigurationDestructionEvent(
            @ProbeParam("contextId") String contextId) {
        policyConfCount.decrement();
    }
    
    
    


}
