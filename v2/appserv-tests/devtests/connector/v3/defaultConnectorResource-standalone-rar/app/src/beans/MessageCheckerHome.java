/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package beans;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface MessageCheckerHome extends EJBLocalHome {
    MessageChecker create() throws CreateException;

    boolean done() throws CreateException;

    int expectedResults() throws CreateException;

    void notifyAndWait() throws CreateException;
}
