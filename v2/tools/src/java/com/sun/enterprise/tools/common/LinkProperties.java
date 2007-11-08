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
 * LinkFields.java
 *
 * Created on March 14, 2001, 11:33 AM
 */

package com.sun.enterprise.tools.common;

import com.sun.enterprise.tools.common.util.diagnostics.Reporter;
import com.sun.enterprise.tools.common.util.diagnostics.StackTrace;

import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;

/**
 *
 * @author  vkraemer
 * @version 
 */
public class LinkProperties extends Object implements java.beans.PropertyChangeListener {

    protected Object target;
    
    protected String sourceFieldName;
    
    protected Method writer = null;
//    protected Method reader = null;
    
    protected Object args[] = { target };
    
    /** Creates new LinkFields */ 
    public LinkProperties(Object target, String commonName) throws java.beans.IntrospectionException {
        this.target = target;
        sourceFieldName = commonName;
        writer = PropertyUtils.getWriter(target,commonName);
//        reader = PropertyUtils.getWriter(target,commonName);
    }

    /** Creates new LinkFields */
    public LinkProperties(Object target, String srcName, String destName) 
        throws java.beans.IntrospectionException {
        this.target = target;
        sourceFieldName = srcName;
        writer = PropertyUtils.getWriter(target,destName);
//        reader = PropertyUtils.getWriter(target,destName);
    }
        
    public void propertyChange(java.beans.PropertyChangeEvent pce) {
        Reporter.info(pce); //NOI18N
        String changedProperty = pce.getPropertyName();
        Reporter.info(changedProperty); //NOI18N
        Reporter.info(pce.getOldValue()); //NOI18N
        Reporter.info(pce.getNewValue()); //NOI18N
        Reporter.info(sourceFieldName); //NOI18N
        try {
//            Object targetValue = reader.invoke(target, null);
            
            if (changedProperty.equals(sourceFieldName)) {
                Reporter.info("case one");//NOI18N
                Reporter.verbose(pce); //NOI18N
                args[0] =  pce.getNewValue();
                writer.invoke(target, args);
            }
            // this is a hack for forte which has really strange property names...
            // for example:
            // the  /WebApp/ResourceRef.51/Description is the description
            // of a WebStandardData.ResourceRefData.
            /*else if (changedProperty.endsWith(sourceFieldName.substring(1))) {
                Reporter.info("case two");//NOI18N
                args[0] =  pce.getNewValue();
                writer.invoke(target, args);
            }*/
        }
        catch (Throwable t) {
            try {
                args[0] = pce.getNewValue().toString();
                writer.invoke(target, args);
            }
            catch (Throwable tt) {
                Reporter.critical(new StackTrace(t)); //NOI18N
            }
        }
    }
    
    public static void main(String args[]) {
        Reporter.setSeverityLevel(0); //NOI18N
        TestObject a = new TestObject("foo");//NOI18N
        TestObject b = new TestObject("bar");//NOI18N
        
        java.beans.PropertyChangeSupport propWrap = new java.beans.PropertyChangeSupport(a);
        
        System.out.println(a); //NOI18N
        System.out.println(b); //NOI18N
        try {
        propWrap.addPropertyChangeListener(new LinkProperties(b,"fOne"));//NOI18N
        propWrap.addPropertyChangeListener(new LinkProperties(b,"fOne", "fTwo"));//NOI18N
        
        a.setFOne("baz");//NOI18N
        a.setFTwo("Blah");//NOI18N
        propWrap.firePropertyChange("fOne","foo","baz");//NOI18N
        propWrap.firePropertyChange("fTwo", "foo", "Blah");//NOI18N
        
        System.out.println(a); //NOI18N
        System.out.println(b);    //NOI18N
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    static class TestObject {
        private String fOne;
        private String fTwo;
        
        public TestObject(String arg) {
            fOne = arg;
            fTwo = arg;
        }
        
        public void setFOne(String newVal) {
            fOne = newVal;
        }
        
        public String getFOne() {
            return fOne;
        }

        public void setFTwo(String newVal) {
            fTwo = newVal;
        }
        
        public String getFTwo() {
            return fTwo;
        }
        
        public String toString() {
            return "My values are " + fOne + " and " + fTwo;//NOI18N
        }
    }
}
