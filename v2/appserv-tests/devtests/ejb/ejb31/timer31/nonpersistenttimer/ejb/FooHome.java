package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

import javax.ejb.*;

public interface FooHome extends EJBLocalHome {

    Foo create() throws CreateException, EJBException;
}
