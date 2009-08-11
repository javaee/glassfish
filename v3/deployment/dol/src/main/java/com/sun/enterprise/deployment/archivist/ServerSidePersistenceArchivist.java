package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.util.XModuleType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;


/**
 * Archivist that reads persitence.xml for ejb jars and appclient while running on server side
 */
@Service
public class ServerSidePersistenceArchivist extends PersistenceArchivist {
    @Inject
    private ProcessEnvironment env;

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
        // Reads persitence.xml for ejb jars
        return XModuleType.EJB == moduleType ||
                // Or App client modules if running inside server
                (XModuleType.CAR == moduleType && env.getProcessType().isServer());
    }

    @Override
    protected String getPuRoot(ReadableArchive archive) {
        //PU root for ejb jars and acc (while on server) is the current exploded archive on server side  
        return "";
    }

}