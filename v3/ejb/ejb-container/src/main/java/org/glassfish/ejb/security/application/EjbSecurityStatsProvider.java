/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.ejb.security.application;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.TimeStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.TimeStatisticImpl;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 *
 * @author nithyasubramanian
 */
@ManagedObject
@Description( "Ejb Security Deployment statistics" )
public class EjbSecurityStatsProvider {

    TimeStatisticImpl deploymentTime = new TimeStatisticImpl(0, 0, 0, 0, "DeploymentTime", "milliseconds", "Deployment Time", 0, 0);
    CountStatisticImpl ejbSMCount = new CountStatisticImpl("SecurityManagerCount", "count", "Count of EJB Security managers");
    CountStatisticImpl ejbPCCount = new CountStatisticImpl("PolicyConfigurationCount", "count", "Count of Policy Configuration");



    @ManagedAttribute(id="depolymenttime")
    public TimeStatistic getDeploymentTime() {
        return deploymentTime.getStatistic();
    }

    @ManagedAttribute(id="securitymanagercount")
    public CountStatistic getSecurityManagerCount() {
        return ejbSMCount.getStatistic();
    }

    @ManagedAttribute(id="policyconfigurationcount")
    public CountStatistic getPolicyConfigurationCount() {
        return ejbPCCount.getStatistic();
    }




    @ProbeListener("glassfish:ejb-container:ejb:ejbSecDeploymentStartedEvent")
    public void ejbSecDeploymentStartedEvent(@ProbeParam("appName")String appName){
       deploymentTime.setStartTime(System.currentTimeMillis());
       ejbSMCount.increment();
    }

    @ProbeListener("glassfish:ejb-container:ejb:ejbSecDeploymentEndedEvent")
    public void ejbSecDeploymentEndedEvent(@ProbeParam("appName")String appName){
      ejbSMCount.decrement();
    }



    @ProbeListener("glassfish:ejb-container:ejb:ejbPCCreationStartEvent")
    public void ejbPCCreationStartEvent(@ProbeParam("contextId")String contextId){
        ejbPCCount.increment();

    }

    @ProbeListener("glassfish:ejb-container:ejb:ejbPCDestructionStartEvent")
    public void ejbPCDestructionStartEvent(@ProbeParam("contextId")String contextId){
       ejbPCCount.decrement();
    }





}
