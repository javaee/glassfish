/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.devtest.admin.notification.lookup.ejb;

import java.util.Collection;
import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import java.math.*;
import javax.naming.*;

/**
 */
public class LookupBean implements SessionBean {
    
    SessionContext sessionContext_;

    BigDecimal yenRate = new BigDecimal("121.6000");
    BigDecimal euroRate = new BigDecimal("0.0077");

    /**
     * Returns the yen value for a given dollar amount.
     * @param dollars dollar amount to be converted to yen.
     */
    public BigDecimal dollarToYen(BigDecimal dollars) {

        BigDecimal result = dollars.multiply(yenRate);
        return result.setScale(2,BigDecimal.ROUND_UP);
    }

    /**
     * Returns the euro value for a given yen amount.
     * @param yen yen amount to be converted to euro.
     */
    public BigDecimal yenToEuro(BigDecimal yen) {
        BigDecimal result = yen.multiply(euroRate);
        return result.setScale(2,BigDecimal.ROUND_UP);
    }

    public boolean lookupResource(String jndiName) {
        try {
            Context context = new InitialContext();
            Object obj = context.lookup(jndiName);
            if (obj != null) {
                System.out.println("Looked up " + obj.getClass().getName());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    /**
     * Required by EJB spec.
     */
    public LookupBean() {}

    /**
     * Creates a bean. Required by EJB spec.
     * @exception throws CreateException.
     */
    public void ejbCreate() {}

    /**
     * Removes the bean. Required by EJB spec.
     */
    public void ejbRemove() {}

    /**
     * Loads the state of the bean from secondary storage. Required by EJB spec.
     */
    public void ejbActivate() {}
    
    /**
     * Keeps the state of the bean to secondary storage. Required by EJB spec.
     */
    public void ejbPassivate() {}

    /**
     * Sets the session context. Required by EJB spec.
     * @param ctx A SessionContext object.
     */
   public void setSessionContext(SessionContext sc) {
       sessionContext_ = sc;
   }
}
