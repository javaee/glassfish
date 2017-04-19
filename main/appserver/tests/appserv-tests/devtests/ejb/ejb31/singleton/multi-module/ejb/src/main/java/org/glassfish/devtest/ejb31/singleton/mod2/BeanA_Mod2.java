/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.devtest.ejb31.singleton.multimodule.mod2;

import org.glassfish.devtest.ejb31.singleton.multimodule.servlet.RemoteInitTracker;
import org.glassfish.devtest.ejb31.singleton.multimodule.servlet.LocalInitTracker;

import javax.ejb.Singleton;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;

/**
 *
 * @author Mahesh Kannan
 */
@Singleton
public class BeanA_Mod2 {

    @EJB
    LocalInitTracker tracker;
    
    @PostConstruct
    public void afterInit() {
        tracker.add(this.getClass().getName());
    }
 
}
