package com.sun.s1asdev.jdbc.contauth.ejb;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;

public interface SimpleBMPHome
    extends EJBLocalHome
{
    SimpleBMP create()
        throws CreateException;

}
