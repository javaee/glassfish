package com.sun.s1asdev.ejb.mdb.singleton;

import javax.ejb.*;

@Stateless
public class FooBean implements FooRemoteIF {
    public String foo() {
        return this.toString();
    }
}
