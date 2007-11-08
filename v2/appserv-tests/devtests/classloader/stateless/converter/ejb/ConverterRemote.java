/**
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.ejb.stateless.converter.ejb;

import javax.ejb.EJBObject;
import java.rmi.RemoteException;
import java.math.*;

public interface ConverterRemote extends EJBObject {
    public String getParserFactoryClassName() throws RemoteException;
}
