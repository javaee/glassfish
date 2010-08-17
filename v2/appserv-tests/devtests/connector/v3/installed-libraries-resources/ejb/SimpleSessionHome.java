package com.sun.s1asdev.connector.installed_libraries_test.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface SimpleSessionHome extends EJBLocalHome {
    SimpleSession create()
            throws CreateException;
}
