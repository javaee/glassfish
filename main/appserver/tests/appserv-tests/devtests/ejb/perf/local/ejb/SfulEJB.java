/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local;

import javax.ejb.*;

public class SfulEJB implements SessionBean
{
    public SfulEJB(){}

    public void ejbCreate() {}

    public void notSupported() {}
    public void required() {}
    public void requiresNew() {}
    public void mandatory() {}
    public void never() {}
    public void supports() {}

    public void setSessionContext(SessionContext sc)
    {}

    public void ejbRemove() 
    {}

    public void ejbActivate() 
    {}

    public void ejbPassivate()
    {}
}
