/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.devtest.ejb31.singleton.multimodule.servlet;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author Mahesh Kannan
 */
@Singleton
@Startup
@Local(LocalInitTracker.class)
@Remote(RemoteInitTracker.class)
public class InitOrderTrackerBean {

    private volatile int counter = 0;

    private Map<String, Integer> initOrder = new HashMap<String, Integer>();

    @PostConstruct
    public void afterInit() {
        add(this.getClass().getName());
    }

    public Map<String, Integer> getInitializedNames() {
        return initOrder;
    }

    public void add(String name) {
        initOrder.put(name, counter++);
    }
}
