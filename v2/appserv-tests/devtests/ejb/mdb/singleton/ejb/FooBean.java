package com.sun.s1asdev.ejb.mdb.singleton;

import javax.ejb.*;
import java.util.*;

@Stateless
public class FooBean implements FooRemoteIF {
    @EJB
    private StatusBean statusBean;

    public String foo() {
        return this.toString();
    }

    public List<String> getMessageBeanInstances() {
        return statusBean.getMessageBeanInstances();
    }
}
