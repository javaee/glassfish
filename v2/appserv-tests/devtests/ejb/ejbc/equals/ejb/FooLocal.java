package com.sun.s1asdev.ejb.ejbc.equals;

import javax.ejb.*;

public interface FooLocal extends EJBLocalObject {
    void callHello();
    String sayHello();
}
