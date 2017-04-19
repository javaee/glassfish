/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.devtest.ejb31.singleton.mod1;

import org.glassfish.devtest.ejb31.singleton.servlet.RemoteInitTracker;
import org.glassfish.devtest.ejb31.singleton.servlet.LocalInitTracker;

import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;

/**
 *
 * @author Mahesh Kannan
 */
@Singleton
@Startup
@DependsOn("BeanA_Mod1")
public class RootBean_Mod1 {

    @EJB
    LocalInitTracker tracker;

    @PostConstruct
    public void afterInit() {
        tracker.add(this.getClass().getName());
    }
 
}
