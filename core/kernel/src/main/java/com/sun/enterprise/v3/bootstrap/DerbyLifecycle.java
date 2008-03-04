/*
 * DerbyLifecycle.java
 *
 * Created on November 3, 2006, 2:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.bootstrap;

import com.sun.enterprise.module.LifecyclePolicy;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.impl.Utils;
import java.util.logging.Level;

/**
 *
 * @author dochez
 */
public class DerbyLifecycle implements LifecyclePolicy {
    
    /** Creates a new instance of DerbyLifecycle */
    public DerbyLifecycle() {
    }
    
    /**
     * Callback when the module enters the {@link ModuleState#RESOLVED RESOLVED}
     * state (all classloaders dependencies are resolved). 
     * Each submodule is guaranteed to be in at least
     * {@link ModuleState#RESOLVED RESOLVED} state. 
     * @param module the module instance
     */
    public void load(Module module) {
    }
    
    /** 
     * Callback when the module enters the {@link ModuleState#READY READY} state.
     * This is a good time to do any type of one time initialization 
     * or set up access to resources
     * @param module the module instance
     */
    public void start(Module module) {
   
        try {
            final Module myModule = module;
            Thread thread = new Thread() {
                public void run() {
                    try {
                        try {                     
                            Class driverClass = myModule.getClassLoader().loadClass("org.apache.derby.jdbc.EmbeddedDriver");
                            myModule.setSticky(true);
                            driverClass.newInstance();
                        } catch(ClassNotFoundException e) {
                            Utils.getDefaultLogger().log(Level.SEVERE, "Cannot load Derby Driver ",e);
                        } catch(java.lang.InstantiationException e) {
                            Utils.getDefaultLogger().log(Level.SEVERE, "Cannot instantiate Derby Driver", e);          
                        } catch(IllegalAccessException e) {
                            Utils.getDefaultLogger().log(Level.SEVERE, "Cannot instantiate Derby Driver", e);                      
                        }                   
                    }   
                    catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }        

        
    }
    
    /** 
     * Callback before the module starts being unloaded. The runtime will 
     * free all the module resources and returned to a {@link ModuleState#NEW NEW} state.
     * @param module the module instance
     */
    public void stop(Module module) {
    
    }
    
    /**
     * Requests a service offered by this module. 
     * The module's user can pass a context defining the 
     * requested service characteristics. A service initialization routine
     * can also update the context to add accessors to get the 
     * service specific hooks.
     *
     * @param serviceContext the context information
     * @return a flag with the service's initialization success 
     * 
     */ 
    public boolean getService(Object serviceContext) {
        return false;
    }  
        
}
