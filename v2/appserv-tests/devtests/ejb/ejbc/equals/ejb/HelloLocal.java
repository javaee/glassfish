package com.sun.s1asdev.ejb.ejbc.equals;

import javax.ejb.*;

public interface HelloLocal extends EJBLocalObject {
    void callHello();
    String sayHello();
}
