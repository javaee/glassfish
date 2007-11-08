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

/*
 * JavaModelTest.java
 *
 * Created on 14. April 2005, 14:46
 */

package com.sun.persistence.runtime.model;

import java.util.Collection;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaProperty;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.java.JavaModelFactory;
import com.sun.org.apache.jdo.impl.model.java.runtime.jdk5.JDK5RuntimeJavaModelFactory;

import com.sun.org.apache.jdo.tck.pc.company.Department;

public class JavaModelTest extends TestCase {

    /** The JavaModelfactory instance. */
    private static final JavaModelFactory javaModelFactory =
        JDK5RuntimeJavaModelFactory.getInstance();

    /** Constructor. */
    public JavaModelTest(String testName) {
        super(testName);
    }

    /** Test JavaModel */
    public void testJavaModel() {
        // class Department
        JavaType departmentType = javaModelFactory.getJavaType(Department.class);
        
        // check fields
        checkField(departmentType, "employees", //NOI18N
                   "java.util.Set", null); //NOI18N
        checkField(departmentType, "deptid",  //NOI18N
                   "long", null); //NOI18N
        checkField(departmentType, "fundedEmps", 
                   "java.util.Set", null);  //NOI18N
        checkField(departmentType, "employeeOfTheMonth",  //NOI18N
                   "com.sun.org.apache.jdo.tck.pc.company.Employee", null); //NOI18N
        checkField(departmentType, "name", //NOI18N
                   "java.lang.String", null); //NOI18N
        checkField(departmentType, "company",//NOI18N
                   "com.sun.org.apache.jdo.tck.pc.company.Company", null); //NOI18N
        
        // check properties
        checkProperty(departmentType, "employees", //NOI18N
                      "java.util.Set", null); //NOI18N
        checkProperty(departmentType, "deptid", //NOI18N
                      "long", null); //NOI18N
        checkProperty(departmentType, "fundedEmps", 
                      "java.util.Set", null); //NOI18N
        checkProperty(departmentType, "employeeOfTheMonth", //NOI18N
                      "com.sun.org.apache.jdo.tck.pc.company.Employee", null); //NOI18N
        checkProperty(departmentType, "name", //NOI18N
                      "java.lang.String", null); //NOI18N
        checkProperty(departmentType, "company",//NOI18N
                      "com.sun.org.apache.jdo.tck.pc.company.Company", null); //NOI18N

        // class Foo
        JavaType fooType = javaModelFactory.getJavaType(Foo.class);
  
        // check fields
        checkField(fooType, "a", //NOI18N
                   "int", null); //NOI18N
        checkField(fooType, "b", //NOI18N
                   "java.util.Collection", "java.lang.Integer"); //NOI18N
        checkField(fooType, "c", //NOI18N
                   "[I", "int"); //NOI18N

        // check properties
        checkProperty(fooType, "m", //NOI18N
                      "long", null); //NOI18N
        checkProperty(fooType, "n", //NOI18N
                      "boolean", null); //NOI18N
        checkProperty(fooType, "o", //NOI18N
                      "java.lang.Object", null); //NOI18N
        checkProperty(fooType, "p", //NOI18N
                      "java.util.Collection", "java.lang.Long"); //NOI18N
        checkProperty(fooType, "q", //NOI18N
                      "java.util.Collection", "java.lang.Byte"); //NOI18N
        checkProperty(fooType, "r", //NOI18N
                      "boolean", null); //NOI18N
        checkProperty(fooType, "s", //NOI18N
                      "java.lang.Double", null); //NOI18N
        checkProperty(fooType, "t", //NOI18N
                      "java.util.Date", null); //NOI18N
    }

    /**
     * Checks the field of the specified name of the class represented by the
     * specified JavaType.
     */
    private void checkField(JavaType beanClass, String fieldName, 
                            String expectedType, 
                            String expectedComponentType) {
        String msg = "field " + fieldName + " of class " + beanClass.getName(); //NOI18N
        JavaField javaField = beanClass.getJavaField(fieldName);
        assertNotNull("Missing " + msg, javaField); //NOI18N
        JavaType type = javaField.getType();
        assertNotNull("Missing type of " + msg, type); //NOI18N
        assertEquals("Wrong type of " + msg, expectedType, type.getName()); //NOI18N
        JavaType componentType = javaField.getComponentType();
        if (expectedComponentType == null) {
            assertNull("Unexpected component type for " + msg, //NOI18N
                       componentType);
        }
        else {
            assertEquals("Wrong component type of " + msg, //NOI18N
                         expectedComponentType, componentType.getName());
        }
    }
    
    /**
     * Checks the property of the specified name of the class represented by the
     * specified JavaType.
     */
    private void checkProperty(JavaType beanClass, String propName, 
                               String expectedType,
                               String expectedComponentType) {
        String msg = "property " + propName + " of class " + beanClass.getName(); //NOI18N
        JavaProperty javaProp = beanClass.getJavaProperty(propName);
        assertNotNull("Missing " + msg, javaProp); //NOI18N
        JavaType type = javaProp.getType();
        assertNotNull("Missing type of " + msg, type); //NOI18N
        assertEquals("Wrong type of " + msg, expectedType, type.getName()); //NOI18N
        JavaType componentType = javaProp.getComponentType();
        if (expectedComponentType == null) {
            assertNull("Unexpected component type for " + msg, //NOI18N
                       componentType);
        }
        else {
            assertEquals("Wrong component type of " + msg, //NOI18N
                         expectedComponentType, componentType.getName());
            
        }
    }
    
    // ============== Helper class ===================
    
    public static class Foo {
        // fields
        private int a;
        private Collection<Integer> b;
        private int[] c;
        
        // properties
        public long getM() { return 0; }
        public boolean isN() { return true; }
        public void setN(boolean b) {}
        public void setO(Object o) {}
        public Collection<Long> getP() { return null; }
        public void setQ(Collection<Byte> q) {}
        protected boolean getR() { return true; }
        protected void setR(boolean b) {}
        protected void setS(Double d) {}
        protected Date getT() { return null; }
    }
    
    // ============== JUnit methods ===================
    
    /** */
    protected void setUp() throws Exception { 
    }
    
    /** */
    protected void tearDown() throws Exception { }
    
    /** */
    public static Test suite() {
        return new TestSuite(JavaModelTest.class);
    }

}
