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

package com.sun.enterprise.tools.common.beans;

import java.beans.*;

public class IasConnectorOneZeroBeanInfo extends SimpleBeanInfo {
    
    private static final java.util.ResourceBundle bundle =
        java.util.ResourceBundle.getBundle("com.sun.enterprise.tools.common.beans.Bundle"); // NOI18N
    
    // Bean descriptor information will be obtained from introspection.//GEN-FIRST:BeanDescriptor
    private static BeanDescriptor beanDescriptor = null;
    private static BeanDescriptor getBdescriptor(){
        //GEN-HEADEREND:BeanDescriptor
        
        // Here you can add code for customizing the BeanDescriptor.
        
        return beanDescriptor;     } //GEN-LAST:BeanDescriptor
    
    
    // Properties information will be obtained from introspection.//GEN-FIRST:Properties
    private static PropertyDescriptor[] properties = null;
    private static PropertyDescriptor[] getPdescriptor(){//GEN-HEADEREND:Properties
        try {
        properties = new PropertyDescriptor[8];
        // Here you can add code for customizing the properties array.
        properties[0] = getPropDesc("description", IasConnectorOneZero.class); // NOI18N
        properties[1] = getPropDesc("idleTimeoutInSeconds", IasConnectorOneZero.class); // NOI18N
        properties[2] = getPropDesc("jndiName", IasConnectorOneZero.class); // NOI18N
        properties[3] = getPropDesc("maxPoolSize", IasConnectorOneZero.class); // NOI18N
        properties[4] = getPropDesc("maxWaitTimeInMillis", IasConnectorOneZero.class); // NOI18N
        properties[5] = getPropDesc("propertyElements", IasConnectorOneZero.class); // NOI18N
        properties[6] = getPropDesc("roleMap", IasConnectorOneZero.class); // NOI18N
        properties[7] = getPropDesc("steadyPoolSize", IasConnectorOneZero.class); // NOI18N
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        return properties;     } //GEN-LAST:Properties
    
    private static PropertyDescriptor getPropDesc(String propName, Class bc) 
    throws java.beans.IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(propName,bc);
        pd.setDisplayName(bundle.getString("DISPNAME_"+propName));
        pd.setName(bundle.getString("NAME_"+propName));
        pd.setShortDescription(bundle.getString("SHORTDESC_"+propName));
        return pd;
    }
    // Event set information will be obtained from introspection.//GEN-FIRST:Events
    private static EventSetDescriptor[] eventSets = null;
    private static EventSetDescriptor[] getEdescriptor(){//GEN-HEADEREND:Events
        
        // Here you can add code for customizing the event sets array.
        
        return eventSets;     } //GEN-LAST:Events
    
    // Method information will be obtained from introspection.//GEN-FIRST:Methods
    private static MethodDescriptor[] methods = null;
    private static MethodDescriptor[] getMdescriptor(){//GEN-HEADEREND:Methods
        
        // Here you can add code for customizing the methods array.
        
        return methods;     } //GEN-LAST:Methods
    
    
    private static int defaultPropertyIndex = -1; //GEN-BEGIN:Idx
    private static int defaultEventIndex = -1; //GEN-END:Idx
    
    
    //GEN-FIRST:Superclass
    
    // Here you can add code for customizing the Superclass BeanInfo.
    
    //GEN-LAST:Superclass
    
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     *
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
    }*/
    
    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getPdescriptor();
    }
    
    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     *
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     *
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }
    
    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     *
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }
    
    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean.
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     *
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }*/
}

