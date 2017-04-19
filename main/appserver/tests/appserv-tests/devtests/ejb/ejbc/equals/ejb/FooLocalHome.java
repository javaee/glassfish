package com.sun.s1asdev.ejb.ejbc.equals;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;


public interface FooLocalHome extends EJBLocalHome {
    FooLocal create () throws CreateException;
}
