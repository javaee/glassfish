/*
 * @(#)Tax.java        1.1 2005/08/03
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

package com.sun.appserv.sqe.security.wss.annotations;


import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * FedTax WebService endpoint using the Java EE 5 annotations. 
 * Used for testing Web Services Security tests on Java EE 5 platform.
 *
 * @version 1.1  08 Aug 2005
 * @author Jagadesh Munta
 */

@WebService(
        name="Tax",
        serviceName="TaxService",
        targetNamespace="http://sun.com/appserv/sqe/security/taxws"
)
public class Tax {

    private static final double FED_TAX_RATE = 0.2;

    public Tax() {
    }

    @WebMethod(operationName="getFedTax", action="urn:GetFedTax")
    public double getFedTax(double income, double deductions) {
        return ((income -  deductions) * FED_TAX_RATE);
    }

    
}
