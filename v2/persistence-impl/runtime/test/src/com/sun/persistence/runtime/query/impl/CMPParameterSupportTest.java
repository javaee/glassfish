/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.persistence.runtime.query.impl;

import junit.framework.*;

/**
 *
 * @author db13166
 */
public class CMPParameterSupportTest extends TestCase {
    private static final String ejbName1 = "anEjbName";
    private static final String ejbName2 = "anotherEjbName";
    
    private CMPParameterSupport ps;
    
    public CMPParameterSupportTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        ps = new CMPParameterSupport(
                getClass().getDeclaredMethod(
                "aMethod",
                java.lang.String.class, java.lang.Double.class));
        ps.setParameterKind("?1", ejbName1);
        ps.setParameterKind("?2", ejbName2);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CMPParameterSupportTest.class);
        
        return suite;
    }
    
    /**
     * Test of getParameterType method, of class com.sun.persistence.runtime.query.impl.CMPParameterSupport.
     */
    public void testGetParameterType() {
        assertSame(ps.getParameterType("?1"), java.lang.String.class);
        assertSame(ps.getParameterType("?2"), java.lang.Double.class);
        
        try {
            assertSame(ps.getParameterType("?0"), java.lang.String.class);
            assertSame(ps.getParameterType("?5"), java.lang.String.class);
            assertSame(ps.getParameterType("?fred"), java.lang.String.class);
            assertSame(ps.getParameterType("?"), java.lang.String.class);
            assertSame(ps.getParameterType(""), java.lang.String.class);
            fail("Didn't throw expected EJBQLException");
        } catch (EJBQLException ex) {
            // expected
        } catch (Exception ex) {
            fail("Caught unexpected exception " + ex);
        }
    }
    
    /**
     * Test of getParameterCount method, of class com.sun.persistence.runtime.query.impl.CMPParameterSupport.
     */
    public void testGetParameterEjbName() {
        assertEquals(ejbName1, ps.getParameterKind("?1"));
        assertEquals(ejbName2, ps.getParameterKind("?2"));
        try {
            assertEquals(ejbName1, ps.getParameterKind(""));
            assertEquals(ejbName1, ps.getParameterKind("?0"));
            assertEquals(ejbName1, ps.getParameterKind("?-1"));
            assertEquals(ejbName1, ps.getParameterKind("?3"));
            fail("Didn't throw expected EJBQLException");            
        } catch (EJBQLException ex) {
            // expected
        } catch (Exception ex) {
            fail("Caught unexpected exception " + ex);
        }
    }
    
    /**
     * Test of getParameterCount method, of class com.sun.persistence.runtime.query.impl.CMPParameterSupport.
     */
    public void testsetParameterEjbName() {
        // Deliberately empty; see setUp.
    }
    
        /** This exists solely for the purpose of getting a method for use by
     * <code>setUp</code>.
     */
    private int aMethod(String s, Double d) {
        // This method does nothing
        return 0;
    }
}
