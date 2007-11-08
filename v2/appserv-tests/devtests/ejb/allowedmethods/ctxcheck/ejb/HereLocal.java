package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import javax.ejb.*;

public interface HereLocal
    extends EJBLocalObject
{
    public void doSomethingHere();

    public void accessEJBObject();

    public void accessEJBLocalObject();

    public void accessEJBHome();

    public void accessEJBLocalHome();
}
