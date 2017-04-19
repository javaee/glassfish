package com.sun.s1asdev.ejb.allowedmethods.remove;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;


public interface HereLocalHome
    extends EJBLocalHome
{
    HereLocal create()
        throws CreateException;
}
