/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local;

import javax.ejb.*;

public class BmpEJB implements EntityBean
{
    public BmpEJB(){}

    public String ejbCreate(String s) {
	return s;
    }

    public void ejbPostCreate(String s) {}

    public void notSupported() {}
    public void required() {}
    public void requiresNew() {}
    public void mandatory() {}
    public void never() {}
    public void supports() {}

    public void setEntityContext(EntityContext c)
    {}

    public void unsetEntityContext()
    {}

    public void ejbRemove() 
    {}

    public void ejbActivate() 
    {}

    public void ejbPassivate()
    {}

    public void ejbLoad()
    {}

    public void ejbStore()
    {}

    public String ejbFindByPrimaryKey(String s) {
	return s;
    }
}
