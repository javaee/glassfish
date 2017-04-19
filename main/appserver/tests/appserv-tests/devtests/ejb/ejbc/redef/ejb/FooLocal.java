package com.sun.s1asdev.ejb.ejbc.redef;

import javax.ejb.*;

public interface FooLocal extends FooLocalSuper {

    // illegally override ejblocalobject methods.
    void remove() throws RemoveException;
    EJBLocalHome getEJBLocalHome();
    Object getPrimaryKey();
    boolean isIdentical(EJBLocalObject obj);

    void callHello();

}
