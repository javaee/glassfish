/*
 * @(#)StateTaxIF.java        1.1 2004/05/03
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

import java.rmi.RemoteException;
import java.rmi.Remote;

/**
 * StateTax EJB WebService Bean Remote interface. Using in Web Services Security
 * tests.
 *
 * @version 1.1  03 May 2004
 * @author Jagadesh Munta
 */
public interface StateTaxIF extends Remote{
        /*
         * Interface to declare one business method - getFedTax method.
         */
	public double getStateTax(double income, double deductions)
            throws RemoteException;

}
