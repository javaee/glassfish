/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package test;

import javax.ejb.*;
import javax.naming.*;

/**
 * This SessionBean is used to test setting CMP field 'name' to a value 
 * that is too large for the column size that it is mapped to.
 * The test is expected to be executed with flush after business method 
 * set to true for setNameWithFlush() and to false for setName().
 * The test is executed for CMP1.1 bean (A1) and CMP2.x bean (A2).
 */ 
public class TestBean implements SessionBean {

    private  A1LocalHome a1Home = null;
    private  A2LocalHome a2Home = null;
    private  A1Local a1bean = null;
    private  A2Local a2bean = null;

    // SessionBean methods
 
    public void ejbCreate() throws CreateException {
        System.out.println("TestBean ejbCreate");
        try {
            a1Home = lookupA1();
            a2Home = lookupA2();
 
        } catch (NamingException ex) {
            throw new EJBException(ex.getMessage());
        }
    }    
 
    public void ejbActivate() {
        System.out.println("TestBean ejbActivate");
    }    

    public void ejbPassivate() {
            a1Home = null;
            a2Home = null;
    }

    public void ejbRemove() {

    }
    
    public void setSessionContext(SessionContext sc) {

    }

    private A1LocalHome lookupA1() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/A1Flush");
        return (A1LocalHome) objref;
    }    

    private A2LocalHome lookupA2() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/A2Flush");
        return (A2LocalHome) objref;
    }    

    /** 
     * Run test for CMP1.1 bean without flush after business
     * method. This method will fail at commit and the client
     * will get a RuntimeException.
     */
    public void testA1() throws CreateException {
        a1bean = a1Home.create("A1");
        a1bean.setName("A12345678901234567890"); 
    }

    /** 
     * Run test for CMP2.x bean without flush after business
     * method. This method will fail at commit and the client
     * will get a RuntimeException.
     */
    public void testA2() throws CreateException {
        a2bean = a2Home.create("A2");
        a2bean.setName("A12345678901234567890"); 
    }

    /** 
     * Run test for CMP1.1 bean with flush after business
     * method. This method will fail at flush and the client
     * will get our FlushException.
     */
    public void testA1WithFlush() throws CreateException, FlushException {
        a1bean = a1Home.create("B1");
        try {
            a1bean.setNameWithFlush("A12345678901234567890"); 
        } catch (EJBException e) {
            if (isExpectedException(e))
                throw new FlushException(e.toString());
            else
                throw e;
        }
    }

    /** 
     * Run test for CMP2.x bean with flush after business
     * method. This method will fail at flush and the client
     * will get our FlushException.
     */
    public void testA2WithFlush() throws CreateException, FlushException {
        a2bean = a2Home.create("B2");
        try {
            a2bean.setNameWithFlush("A12345678901234567890"); 
        } catch (EJBException e) {
            if (isExpectedException(e))
                throw new FlushException(e.toString());
            else
                throw e;
        }

    }

    private boolean isExpectedException (Exception e) {
        boolean expected = false;

        Throwable c = e.getCause();
        if (c != null && 
                (c instanceof com.sun.jdo.api.persistence.support.JDODataStoreException)) {

            String msg = c.getMessage();
            expected = (msg != null) && (msg.indexOf("JDO76400") > -1);
        }

        return expected;
    }
} 
