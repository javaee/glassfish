package com.sun.s1asdev.ejb.ejbc.redef;

import javax.ejb.*;

public interface FooLocalSuper extends EJBLocalObject {
    void callHello();
    String sayHello();
}
