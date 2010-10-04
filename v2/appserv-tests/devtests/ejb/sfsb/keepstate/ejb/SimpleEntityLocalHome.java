package com.sun.s1asdev.ejb.sfsb.keepstate.ejb;

import javax.ejb.*;
import java.rmi.*;

public interface SimpleEntityLocalHome
    extends EJBLocalHome
{
    SimpleEntityLocal create(String key, String name)
        throws CreateException;

    SimpleEntityLocal findByPrimaryKey(String key)
        throws FinderException;
}
