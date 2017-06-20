/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import javax.ejb.EJBLocalObject;
import java.rmi.RemoteException;

public interface VersionChecker extends EJBLocalObject {
    int getVersion() ;
}
