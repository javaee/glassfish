package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import javax.ejb.EJBLocalHome;
import javax.ejb.CreateException;

public interface TimerSLSBHome
    extends EJBLocalHome
{
	TimerSLSB create()
        throws CreateException;
}
