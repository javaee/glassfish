package com.sun.s1asdev.ejb.ejb30.persistence.eem_adapted;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;

public interface AdaptedLocalHome
    extends EJBLocalHome {

    SfulDelegate createDelegate(String name, String data)
        throws CreateException;
}
