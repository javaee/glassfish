package com.sun.enterprise.security.auth.realm;

import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;


import org.glassfish.gmbal.*;
/**
 *
 * @author nithyasubramanian
 */
@AMXMetadata(type="security-realm-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description( "Security Realm Statistics" )
public class RealmStatsProvider {

    private CountStatisticImpl realmCount;
    
    
    public RealmStatsProvider() {
        realmCount = new CountStatisticImpl("RealmCount", "realm","No of Realms");
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
