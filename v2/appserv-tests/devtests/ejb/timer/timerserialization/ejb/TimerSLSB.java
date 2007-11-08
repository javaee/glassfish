package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import javax.ejb.*;

public interface TimerSLSB
    extends EJBLocalObject
{

	public Timer createTimer(int ms);

}

