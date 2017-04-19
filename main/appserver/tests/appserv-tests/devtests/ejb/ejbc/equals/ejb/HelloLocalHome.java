package com.sun.s1asdev.ejb.ejbc.equals;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;


public interface HelloLocalHome extends EJBLocalHome {
    HelloLocal create () throws CreateException;
}
