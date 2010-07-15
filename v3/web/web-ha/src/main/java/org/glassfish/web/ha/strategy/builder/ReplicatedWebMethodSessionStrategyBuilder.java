package org.glassfish.web.ha.strategy.builder;

import com.sun.enterprise.deployment.runtime.web.SessionManager;
import com.sun.enterprise.web.BasePersistenceStrategyBuilder;
import com.sun.enterprise.web.ServerConfigLookup;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.glassfish.web.ha.session.management.FullSessionFactory;
import org.glassfish.web.ha.session.management.HASessionStoreValve;
import org.glassfish.web.ha.session.management.ReplicationStore;
import org.glassfish.web.ha.session.management.ReplicationWebEventPersistentManager;
import org.glassfish.web.valve.GlassFishValve;
import org.jvnet.hk2.annotations.Service;


@Service(name="replicated")
public class ReplicatedWebMethodSessionStrategyBuilder extends BasePersistenceStrategyBuilder {


    public ReplicatedWebMethodSessionStrategyBuilder() {
        
    }

    public void initializePersistenceStrategy(
            Context ctx,
            SessionManager smBean,
            ServerConfigLookup serverConfigLookup)
    {

        super.initializePersistenceStrategy(ctx, smBean, serverConfigLookup);
        super.setPassedInPersistenceType("replicated");
        ReplicationWebEventPersistentManager rwepMgr = new ReplicationWebEventPersistentManager();
        rwepMgr.setSessionFactory(new FullSessionFactory());


        rwepMgr.createBackingStore(this.getPassedInPersistenceType());

        ReplicationStore store = new ReplicationStore(serverConfigLookup);
        
        rwepMgr.setStore(store);
        ctx.setManager(rwepMgr);
        
        HASessionStoreValve haValve = new HASessionStoreValve();
        StandardContext stdCtx = (StandardContext) ctx;
        stdCtx.addValve((GlassFishValve)haValve);

    }

}
