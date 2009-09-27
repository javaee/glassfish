package com.sun.s1asdev.connector.rar_accessibility_test.ejb;

import javax.ejb.EJBLocalObject;

public interface SimpleSession extends EJBLocalObject {
    public boolean test1(int expectedCount) ;
}
