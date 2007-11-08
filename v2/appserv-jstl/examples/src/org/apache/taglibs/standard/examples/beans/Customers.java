/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.apache.taglibs.standard.examples.beans;

import java.util.*;
import java.text.*;

/**
 * Customers Datastore.
 *
 * @author Pierre Delisle
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:20:37 $
 */

public class Customers {
    
    //*********************************************************************
    // Instance variables
    
    private static Vector customers = new Vector();
    private static int nextSeqNo = 0;
    
    //*********************************************************************
    // Datastore operations
    
    public static void create(
    String lastName,
    String firstName,
    String birthDate,
    String line1,
    String line2,
    String city,
    String state,
    String zip,
    String country) {
        create(lastName, firstName, birthDate, line1, line2, city, state, zip,
        country, null, null);
    }
    
    /**
     *  Create new customer
     */
    public static void create(
    String lastName,
    String firstName,
    String birthDate,
    String line1,
    String line2,
    String city,
    String state,
    String zip,
    String country,
    String phoneHome,
    String phoneCell) {
        Customer customer =
        new Customer(++nextSeqNo, lastName, firstName,
        genDate(birthDate), genAddress(line1, line2, city, state, zip, country),
        phoneHome, phoneCell);
        customers.add(customer);
    }
    
    /**
     * Find all customers
     */
    public static Collection findAll() {
        return customers;
    }
    
    //*********************************************************************
    // Utility methods
    
    private static Date genDate(String dateString) {
        DateFormat df = new SimpleDateFormat("M/d/y");
        Date date;
        try {
            date = df.parse(dateString);
        } catch (Exception ex) {
            date = null;
        }
        return date;
    }
    
    private static Address genAddress(String line1, String line2, String city,
    String state, String zip, String country) {
        return new Address(line1, line2, city, state, zip, country);
    }
}
