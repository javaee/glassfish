/*
 * ModuleStartup.java
 *
 * Created on October 26, 2006, 11:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.module.bootstrap;

import org.jvnet.hk2.annotations.Contract;

/**
 * Interface server startup need to implement
 *
 * @author dochez
 */
@Contract
public interface ModuleStartup extends Runnable {
    
    public void setStartupContext(StartupContext context);
    
}
