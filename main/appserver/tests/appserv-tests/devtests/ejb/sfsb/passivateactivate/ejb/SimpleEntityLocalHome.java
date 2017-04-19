package com.sun.s1asdev.ejb.sfsb.passivateactivate.ejb;

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
