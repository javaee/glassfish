package com.sun.s1asdev.ejb.sfsb.keepstate.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleEntityLocal
    extends EJBLocalObject
{
    public String getName();

    public void setName(String val);
}
