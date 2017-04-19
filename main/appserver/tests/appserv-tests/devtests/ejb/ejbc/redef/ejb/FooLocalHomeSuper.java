package com.sun.s1asdev.ejb.ejbc.redef;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;


public interface FooLocalHomeSuper {
    void remove(Object o) throws RemoveException;

    FooLocal create () throws CreateException;

}
