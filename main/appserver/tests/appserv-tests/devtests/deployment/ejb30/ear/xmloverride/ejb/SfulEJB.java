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

    private DataSource myDS6;

    @Resource(name="myDS7", lookup="jdbc/noexist")
    private DataSource myDS7;

    @Resource(name="myDS8", mappedName="jdbc/noexist2")
    private DataSource myDS8;

    @Resource(name="envEntry1", lookup="java:app/env/value1")
    private Integer envEntry1;

    @Resource(name="envEntry2", lookup="nonexist")
    private Integer envEntry2;

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

            int loginTimeout6 = myDS6.getLoginTimeout();
            System.out.println("myDS6 login timeout = " + loginTimeout6);

            int loginTimeout7 = myDS7.getLoginTimeout();
            System.out.println("myDS7 login timeout = " + loginTimeout7);

            int loginTimeout8 = myDS8.getLoginTimeout();
            System.out.println("myDS8 login timeout = " + loginTimeout8);

            System.out.println("enEntry1 = " + envEntry1);
            System.out.println("envEntry2 = " + envEntry2);

            if( (envEntry1 == null) || envEntry1.intValue() != 8 ) {
                throw new RuntimeException("invalid enventry1 value");
            }

            if( (envEntry2 == null) || envEntry2.intValue() != 88 ) {
                throw new RuntimeException("invalid enventry2 value");
            }

        } catch(Exception ex) {
            throw new IllegalStateException("Cannot get expected value: " + ex);
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
