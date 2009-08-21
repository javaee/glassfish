/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.deployment.ejb30.ear.xmloverride;

import javax.annotation.Resource;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.Status;

@RunAs(value="nobody")
@Stateful
@PermitAll
@Resource(name="myDS", type=DataSource.class, mappedName="jdbc/noSuchRes")
public class SfulEJB implements Sful, SessionSynchronization
{
    private boolean inTx = false;
    @EJB private Sless sless;

    @Resource(name="myDS5", lookup="jdbc/__default")
    private DataSource myDS5;

    public String hello() {
        System.out.println("In SfulEJB:hello()");
        return sless.hello();
    }

    //@TransactionAttribute(TransactionAttributeType.REQUIRED)
    @DenyAll
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String goodNight(String message) {
        System.out.println("In SfulEJB:goodNight(): " + inTx);
        if (!inTx) {
            throw new IllegalStateException("It should be in transaction.");
        }

        try {
            InitialContext ic = new InitialContext();
            DataSource myDS = (DataSource)ic.lookup("java:comp/env/myDS");
            int loginTimeout = myDS.getLoginTimeout();
            System.out.println("myDS login timeout = " + loginTimeout);
            DataSource myDS2 = (DataSource)ic.lookup("java:comp/env/jdbc/__default");
            int loginTimeout2 = myDS2.getLoginTimeout();
            System.out.println("myDS2 login timeout = " + loginTimeout2);

            DataSource myDS3 = (DataSource)ic.lookup("java:comp/env/myDS3");
            int loginTimeout3 = myDS3.getLoginTimeout();
            System.out.println("myDS3 login timeout = " + loginTimeout3);

            DataSource myDS4 = (DataSource)ic.lookup("java:comp/env/myDS4");
            int loginTimeout4 = myDS4.getLoginTimeout();
            System.out.println("myDS4 login timeout = " + loginTimeout4);

            int loginTimeout5 = myDS5.getLoginTimeout();
            System.out.println("myDS5 login timeout = " + loginTimeout5);

        } catch(Exception ex) {
            throw new IllegalStateException("Cannot get login timeout: " + ex);
        } 
        return "goodNight";
    }

    @RolesAllowed(value={"nobody"})
    public String goodNight(String message1, String message2) {
        System.out.println("In SfulEJB:goodNight(" + message1 + ", " + message2 + ")");
        return "goodNight " + message1 + ", " + message2;
    }

    @PermitAll
    public String bye() {
        System.out.println("In SfulEJB:bye()");
        return "bye";
    }

    // -- implements SessionSynchronization --
    public void afterBegin() {
        inTx = true;
    }

    public void beforeCompletion() {
        inTx = true;
    }

    public void afterCompletion(boolean committed) {
        inTx = false;
    }
}
