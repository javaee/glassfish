/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.util.io;

import java.io.File;
import java.io.IOException;

/**
 * A class for keeping track of the directories that an instance lives in and under.
 * All the methods throw checked exception to avoid the inevitable NPE otherwise - 
 * when working with invalid directories...
 *
 * Example:
 * new InstanceDirs(new File("/glassfishv3/glassfish/nodeagents/mymachine/instance1"));
 *
 * getInstanceDir()   == /glassfishv3/glassfish/nodeagents/mymachine/instance1
 * getNodeAgentDir()  == /glassfishv3/glassfish/nodeagents/mymachine
 * getNodeAgentsDir() == /glassfishv3/glassfish/nodeagents
 * getInstanceName()  == instance1
 *
 *
 * @author Byron Nevins
 * @since 3.1
 * Created: April 19, 2010
 */
public final class InstanceDirs {
    /**
     * This constructor is used when the instance dir is known
     *
     * @param instanceDir The instance's directory
     * @throws IOException If any error including not having a grandparent directory.
     */
    public InstanceDirs(File theInstanceDir) throws IOException {
        dirs = new ServerDirs(theInstanceDir);

        if(dirs.getServerGrandParentDir() == null) {
            throw new IOException(ServerDirs.strings.get("InstanceDirs.noGrandParent", dirs.getServerDir()));
        }
    }

    public final String getInstanceName() {
        return dirs.getServerName();
    }

    public final File getInstanceDir() {
        return dirs.getServerDir();
    }

    public final File getNodeAgentDir() {
        return dirs.getServerParentDir();
    }

    public final File getNodeAgentsDir() {
        return dirs.getServerGrandParentDir();
    }
    ///////////////////////////////////////////////////////////////////////////
    ///////////           All Private Below           /////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    private final ServerDirs dirs;
}
