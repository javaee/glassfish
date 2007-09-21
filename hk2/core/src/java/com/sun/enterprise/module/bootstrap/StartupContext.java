/*
 * StartupContext.java
 *
 * Created on October 26, 2006, 11:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.module.bootstrap;

import com.sun.enterprise.module.Module;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains important information about the startup process
 *
 * @author dochez
 */
@Service
public class StartupContext {
    
    final File root;
    final Map<String, String> args;
    final long timeZero = System.currentTimeMillis();

    /** Creates a new instance of StartupContext */
    public StartupContext(File root, String[] args) {
        this.root = root;
        this.args = new HashMap<String, String>();
        for (int i=0;i<args.length;i++) {
            if (args[i].startsWith("-")) {
                if (i+1<args.length) {
                    this.args.put(args[i], args[i+1]);
                    i++;
                }
            } else {
                this.args.put("default", args[i]);
            }
        }
    }

    /**
     * Gets the "root" directory where the data files are stored.
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
}
