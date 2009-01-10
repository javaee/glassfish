package org.glassfish.api.admin;

import com.sun.enterprise.module.bootstrap.StartupContext;

import org.jvnet.hk2.annotations.Contract;

import java.io.File;

/**
 * Allow access to the environment under which GlassFish operates.
 *
 * TODO : dochez : this needs to be reconciled with ServerContext and simplified...
 *
 * @author Jerome Dochez
 */
@Contract
public interface ServerEnvironment {

    /** folder where the compiled JSP pages reside */
    public static final String kCompileJspDirName = "jsp";
    String DEFAULT_INSTANCE_NAME = "default-instance-name";

    public File getDomainRoot();    

    /**
     * return the startup context used to initialize this runtime
     */
    public StartupContext getStartupContext();

    /**
     *
     */
    public File getConfigDirPath();

    /**
     * Gets the directory for hosting user-provided jar files.
     * Normally {@code ROOT/lib}
     */
    public File getLibPath();

    /**
     * Gets the directory to store deployed applications
     * Normally {@code ROOT/applications}
     */
    public File getApplicationRepositoryPath();

    /**
     * Gets the directory to store generated stuff.
     * Normally {@code ROOT/generated}
     */
    public File getApplicationStubPath();

    /**
     * Returns the path for compiled JSP Pages from an J2EE application
     * that is deployed on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getApplicationCompileJspPath();

    /**
     * Returns the path for compiled JSP Pages from an Web application
     * that is deployed standalone on this instance. By default all such compiled JSPs
     * should lie in the same folder.
     */
    public File getWebModuleCompileJspPath();

    public String getModuleStubPath();

    public String getApplicationGeneratedXMLPath();    
}
