/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * GeneratedMonitoringMBeanTest.java
 *
 * Created on August 7, 2003, 10:42 AM
 */

package com.sun.enterprise.admin.monitor.util;

import junit.framework.*;
import javax.management.j2ee.statistics.Stats;
import javax.management.*;
import java.util.*;

/**
 * Unit Test for testing GeneratedMonitoringMBean that gets created through the process of 
 * transforming a JSR77 Stats object into a Dynamic MBean. The transformation is
 * helped by the ManagedResourceIntrospector which has its own tests.
 * @author  sg112326
 */
public class GeneratedMonitoringMBeanTest extends TestCase{

    public void testGetAttributeWithNull(){
        try{
            Enumeration e = attributes.keys();
            while(e.hasMoreElements()){
                Object n = mbean.getAttribute(null);
                try{
                    assertEquals(Long.class, n.getClass());
                }
                catch(Error ex){                    
                        ex.getLocalizedMessage();
                        assertEquals(String.class, n.getClass());
                }
            }
        }
        catch(Exception ex){
            assertEquals(NullPointerException.class, ex.getClass());
        }        
    }
    
    public void testGetAttributeWithIncorrectAttrName(){
        try{
            Enumeration e = attributes.keys();
            while(e.hasMoreElements()){
                String elem = (String)e.nextElement();
                Object n = mbean.getAttribute(elem+"o");
                try{
                    assertEquals(Long.class, n.getClass());
                }
                catch(Error ex){                    
                        ex.getLocalizedMessage();
                        assertEquals(String.class, n.getClass());
                }
            }
        }
        catch(Exception ex){
            assertEquals(AttributeNotFoundException.class, ex.getClass());
        }    
    }
    
    public void testGetAttributeWithCorrectAttrName(){
        try{
            Enumeration e = attributes.keys();
            while(e.hasMoreElements()){
                String elem = (String)e.nextElement();
                Object n = mbean.getAttribute(elem);
                try{
                    assertEquals(Long.class, n.getClass());
                }
                catch(Error ex){                    
                        ex.getLocalizedMessage();
                        assertEquals(String.class, n.getClass());
                }
            }
        }
        catch(Exception ex){
            assertEquals(NullPointerException.class, ex.getClass());
        }
    }
    public void testAtrributes(){
        Enumeration en = attributes.elements();
        while(en.hasMoreElements()){
            String attr = (String)en.nextElement();
            System.out.println("testing attribute:"+attr);
            try{ assertEquals("HeapSize", attr.substring(0,attr.indexOf("_")));
            }catch(Error ex){
                try{assertEquals("MaxMemory",attr.substring(0,attr.indexOf("_")));
                }catch(Error e){
                    try{assertEquals("UpTime",attr.substring(0,attr.indexOf("_")));
                    }catch(Error e1){
                        assertEquals("AvailableProcessors",attr.substring(0,attr.indexOf("_")));
                    }
                }
            }
        }
    }
    
    public void testCreation(){
        assertNotNull(mbean);
        assertNotNull(m);
        assertNotNull(jvm);
        assertNotNull(attrs);        
    }
    /** Creates a new instance of GeneratedMonitoringMBeanTest */
    public GeneratedMonitoringMBeanTest(java.lang.String testName) {
        super(testName);
    }
    GeneratedMonitoringMBeanImpl mbean; 
    S1ASJVMStatsImplMock jvm;
    MBeanInfo m;
    MBeanAttributeInfo[] attrs;
    Hashtable attributes = new Hashtable();
    
    protected void setUp() {
        mbean = new GeneratedMonitoringMBeanImpl();
        jvm = new S1ASJVMStatsImplMock();
        m = mbean.introspect(jvm);
        attrs = m.getAttributes();
        for(int i=0; i< attrs.length;i++){
            attributes.put(attrs[i].getName(),attrs[i].getName());
        }
    }
    
    protected void tearDown() {
        
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GeneratedMonitoringMBeanTest.class);
        return suite;
    }
    
    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
}
