/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.devtest.ejb31.singleton.mod1;

import org.glassfish.devtest.ejb31.singleton.servlet.RemoteInitTracker;
import org.glassfish.devtest.ejb31.singleton.servlet.LocalInitTracker;

import javax.ejb.Singleton;
import javax.ejb.EJB;
import javax.ejb.DependsOn;
import javax.annotation.PostConstruct;

/**
 *
 * @author mk
 */
@Singleton
@DependsOn("ejb-ejb31-singleton-threemodules-ejb.jar#InitOrderTrackerBean")
public class BeanA_Mod1 {

    @EJB
    LocalInitTracker tracker;

    @PostConstruct
    public void afterInit() {
        tracker.add(this.getClass().getName());
    }

}
