package com.sun.s1asdev.ejb.ejbc.sameimpl;

import javax.ejb.*;

public interface FooLocal extends EJBLocalObject {
    void callHello();
    String sayHello();
}
