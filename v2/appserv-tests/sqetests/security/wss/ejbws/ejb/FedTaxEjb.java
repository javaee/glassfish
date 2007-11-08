/*
 * @(#)FedTaxEjb.java        1.1 2004/05/03
 *
 * Copyright (c) 2004-2005 Sun Microsystems, Inc.
 * 4150,Network Circle, Santa Clara, California, 95054, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 */

package com.sun.appserv.sqe.security.wss.ejbws.taxcal;


import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;

/**
 * FedTax EJB WebService Bean implementation. Using in Web Services Security tests.
 *
 * @version 1.1  03 May 2004
 * @author Jagadesh Munta
 */

public class FedTaxEjb implements SessionBean {

    /**
     * Implements the service business method getFedTax as defined in the
     * Service Interface.
     */
    private SessionContext sc;
    private static final double FED_TAX_RATE = 0.2;

    public FedTaxEjb() {
    }

    public void ejbCreate() throws RemoteException {
	System.out.println("In FedTaxEjb::ejbCreate !!");
    }

    // Business service method implementation.
    public double getFedTax(double income, double deductions) {
        return ((income -  deductions) * FED_TAX_RATE);
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }
    
}
