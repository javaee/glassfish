package com.sun.enterprise.security.auth.realm;

import org.glassfish.api.statistics.CountStatistic;
import org.glassfish.api.statistics.impl.CountStatisticImpl;
import org.glassfish.probe.provider.annotations.ProbeListener;
import org.glassfish.probe.provider.annotations.ProbeParam;


import org.glassfish.gmbal.*;
/**
 *
 * @author nithyasubramanian
 */
@ManagedObject
@Description( "Security Realm Statistics" )
public class RealmStatsProvider {

    private CountStatisticImpl realmCount;
    
    
    public RealmStatsProvider() {
        realmCount = new CountStatisticImpl("realmCount", "realm","No of Realms");
    }

    @ManagedAttribute
    @Description( "Security Realm Count" )
    public CountStatistic getRealmCount() {
        return realmCount.getStatistic();
    }
    
    @ProbeListener("glassfish:core:realm:realmAddedEvent")
    public void realmAddedEvent(@ProbeParam("realmName")String realmName){
       realmCount.increment();
    }
    
   @ProbeListener("glassfish:core:realm:realmRemovedEvent")
    public void realmRemovedEvent(@ProbeParam("realmName")String realmName){
       realmCount.decrement();
    }
   
    
    
}
