/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.web.security;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 *
 * @author nithyasubramanian
 */
@AMXMetadata(type="login-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description( "Login Statistics" )
public class LoginStatsProvider {
    
    CountStatisticImpl successLoginCount = new CountStatisticImpl("SuccessLoginCount", "count", "No of successful logins");
    CountStatisticImpl failedLoginCount = new CountStatisticImpl("FailedLoginCount", "count", "No of failed logins");
    
    @ManagedAttribute
    public CountStatistic getSuccessLoginCount() {
        return successLoginCount.getStatistic();
    }
    
    @ManagedAttribute
    public CountStatistic getFailedLoginCount() {
        return failedLoginCount.getStatistic();
    }
    
    @ProbeListener("glassfish:webintegration:login:loginSuccessfulEvent")
    public void loginSuccessfulEvent(@ProbeParam("username")String userName){
       successLoginCount.increment();
    }
        
    @ProbeListener("glassfish:webintegration:login:loginFailedEvent")
    public void loginFailedEvent(@ProbeParam("username")String userName){
       failedLoginCount.increment();
    }
    
    

}
