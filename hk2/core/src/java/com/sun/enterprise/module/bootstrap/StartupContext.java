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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains important information about the startup process
 *
 * @author dochez
 */
public class StartupContext {
    
    final File root;
    final Module mainModule;
    final Map<String, String> args;
    final ClassLoader mainModuleClassLoader;
    
    /** Creates a new instance of StartupContext */
    public StartupContext(File root, Module mainModule, String[] args) {
        this.root = root;
        this.mainModule = mainModule;
        // I am saving the main module class loader as it cannot be garbage collected
        // during the startup sequence...
        this.mainModuleClassLoader = mainModule.getClassLoader();
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
    
    public Module getMainModule() {
        return mainModule;
    }
    
    public Map<String, String> getArguments() {
        return args;
    }
}
