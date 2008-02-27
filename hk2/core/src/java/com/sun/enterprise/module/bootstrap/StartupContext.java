/*
 * StartupContext.java
 *
 * Created on October 26, 2006, 11:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.module.bootstrap;

import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains important information about the startup process
 * @author dochez
 */
@Service
public class StartupContext {
    
    final File root;
    final Map<String, String> args;
    final long timeZero = System.currentTimeMillis();

    /** Creates a new instance of StartupContext */
    public StartupContext(File root, String[] args) {
        this.root = absolutize(root);
        this.args = ArgumentManager.argsToMap(args);
    }

    /**
     * Gets the "root" directory where the data files are stored.
     *
     * <p>
     * This path is always absolutized.
     *
     * TODO: in case of Glassfish, this is the domain directory?
     */
    public File getRootDirectory() {
        return root;
    }
        
    public Map<String, String> getArguments() {
        return args;
    }

    /**
     * Returns the time at which this StartupContext instance was created.
     * This is roughly the time at which the hk2 program started.
     *
     * @return the instanciation time
     */
    public long getCreationTime() {
        return timeZero;
    }
    
    private File absolutize(File f)
    {
        try 
        { 
            return f.getCanonicalFile(); 
        }
        catch(Exception e)
        {
            return f.getAbsoluteFile();
        }
    }
}
