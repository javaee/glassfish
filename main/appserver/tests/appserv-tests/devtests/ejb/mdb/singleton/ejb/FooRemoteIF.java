package com.sun.s1asdev.ejb.mdb.singleton;

import javax.ejb.*;
import java.util.*;

@javax.ejb.Remote
public interface FooRemoteIF {
    public String foo();
    public List<String> getMessageBeanInstances();
}
