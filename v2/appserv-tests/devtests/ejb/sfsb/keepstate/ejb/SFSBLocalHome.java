package com.sun.s1asdev.ejb.sfsb.keepstate.ejb;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;

public interface SFSBLocalHome
    extends EJBLocalHome
{
	public SFSBLocal create(String sfsbName)
            throws CreateException;
}
