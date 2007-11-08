package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;


public interface HereLocalHome
    extends EJBLocalHome
{
    HereLocal create()
        throws CreateException;
}
