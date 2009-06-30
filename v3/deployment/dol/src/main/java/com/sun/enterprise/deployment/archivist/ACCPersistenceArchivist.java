package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.deployment.util.XModuleType;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;


@Service
public class ACCPersistenceArchivist extends PersistenceArchivist {

    @Inject
    private ProcessEnvironment env;

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
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
        //        myClientClient.jar (generated JAR - will never contain PUs)
        //        myClient.jar (developer's original JAR contains PUs)
        //        lib/myLib.jar (developer's original JAR) 
        //
        // PU root is returned as "myClient.jar"

        /*
         * The archive passed to getPuRoot will be the app client JAR that was
         * generated during deployment (and then downloaded to the client
         * system).  But the puRoot needs to be the developer's original
         * app client JAR file, not the generated one, because that's where
         * the META-INF/persistence.xml and the entity classes are.
         */
        final String generatedJARName = Util.getURIName(archive.getURI());
        return generatedJARName.substring(0, 
                generatedJARName.length() - "Client.jar".length()) + ".jar";

    }

}
