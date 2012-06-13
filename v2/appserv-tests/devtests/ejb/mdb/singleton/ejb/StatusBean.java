package com.sun.s1asdev.ejb.mdb.singleton;

import javax.ejb.*;
import java.util.*;

/**
 * A Singleton bean to hold the instances (in string form) of MessageBean.
 * The test client calls the remote Stateless bean FooBean, which calls this bean
 * to return the instances to client.
 */
@Singleton
public class StatusBean {
    private List<String> mdbs = new ArrayList<String>();

    public void addMessageBeanInstance(String b) {
        mdbs.add(b);
    }

    public List<String> getMessageBeanInstances() {
        return mdbs;
    }
}
