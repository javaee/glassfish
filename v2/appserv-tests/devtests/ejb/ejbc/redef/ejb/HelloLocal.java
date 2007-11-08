package com.sun.s1asdev.ejb.ejbc.sameimpl;

import javax.ejb.*;

public interface HelloLocal extends EJBLocalObject {
    void callHello();
    String sayHello();
}
