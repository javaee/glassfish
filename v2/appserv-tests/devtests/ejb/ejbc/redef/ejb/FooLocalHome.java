package com.sun.s1asdev.ejb.ejbc.redef;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;


public interface FooLocalHome extends EJBLocalHome, FooLocalHomeSuper {
    FooLocal create () throws CreateException;
    void remove(Object o) throws RemoveException;
}
