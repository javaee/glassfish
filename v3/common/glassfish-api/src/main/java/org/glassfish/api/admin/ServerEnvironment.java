package org.glassfish.api.admin;

import com.sun.enterprise.module.bootstrap.StartupContext;

import org.jvnet.hk2.annotations.Contract;

import java.io.File;

/**
 * Allow access to the environment under which GlassFish operates.
 *
 * @author Jerome Dochez
 */
@Contract
public interface ServerEnvironment {

    /**
     * return the startup context used to initialize this runtime
     */
    public StartupContext getStartupContext();

    /**
     *
     */
    public File getConfigDirPath();    
}
