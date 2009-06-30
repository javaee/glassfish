package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.deployment.util.XModuleType;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;



/**
 * Archivist that reads persistence.xml for appclient module while running on AppClient 
 */
@Service
public class ACCPersistenceArchivist extends PersistenceArchivist {

    @Inject
    private ProcessEnvironment env;

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
        // On server side, persitence.xml for app client is processed by ServerSidePersistenceArchivist
        // so that it can return differnt pu root
        return (XModuleType.CAR == moduleType) && (env.getProcessType() == ProcessType.ACC) ;
    }

    @Override
    protected String getPuRoot(ReadableArchive archive) {
        // if ear looks like
        //   myEJB.jar
        //   myClient.jar (Contains PUs)
        //   lib/
        //     myLib.jar
        // It is represented on ACC side as
        // retrieved/
        //    myAppClient.jar (generated JAR - will never contain PUs)
        //    myAppClient/
        //        myClient-client.jar (generated JAR - will never contain PUs)
        //        myClient.jar (developer's original JAR contains PUs)
        //        lib/myLib.jar (developer's original JAR) 
        //
        // PU root is returned as "myClient.jar"
        return Util.getURIName(archive.getURI());
    }

}
