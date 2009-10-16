/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.devtest.ejb31.singleton.servlet;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.annotation.PostConstruct;
import java.util.List;
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

    private List<String> initOrder = new ArrayList<String>();

    @PostConstruct
    public void afterInit() {
        add(this.getClass().getName());
    }

    public List getInitializedNames() {
        return initOrder;
    }

    public void add(String name) {
        initOrder.add(name);
    }
}