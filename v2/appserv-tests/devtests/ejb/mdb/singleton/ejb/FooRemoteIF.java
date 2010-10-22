package com.sun.s1asdev.ejb.mdb.singleton;

import javax.ejb.*;

@javax.ejb.Remote
public interface FooRemoteIF {
    public String foo();
}
