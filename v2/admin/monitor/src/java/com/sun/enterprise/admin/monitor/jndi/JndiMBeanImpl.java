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
 * JndiMBeanImpl.java
 *
 * Created on March 8, 2004, 1:48 PM
 */

package com.sun.enterprise.admin.monitor.jndi;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

/**
 * The JndiMBean implementation from which a client can access jndi
 * entries given a particular context.
 
 * @author  Rob Ruyak
 */
public class JndiMBeanImpl implements JndiMBean {
    
    private JndiMBeanHelper helper;
    MBeanInfo mbeanInfo;
    private static final Logger logger = 
        Logger.getLogger(AdminConstants.kLoggerName);
    private static final StringManager sm = 
        StringManager.getManager(JndiMBeanImpl.class);
    
    /** Creates a new instance of JndiMBeanImpl */
    public JndiMBeanImpl() {
        initialize();
    }
    
    /**
     * Initializes the JndiMBeanImpl mbean object for servicing queries
     * related to the jndi entries of the application server's naming
     * service. This initialization involves the creation of the object's 
     * JndiMBeanHelper for delegating requests.
     */
    void initialize() {
        helper = new JndiMBeanHelper();
    }
    
    /**
     *
     */
    public Object getAttribute(String str) 
            throws AttributeNotFoundException, 
                MBeanException, ReflectionException {
        throw new UnsupportedOperationException(
                sm.getString("monitor.jndi.unsupported_method"));
    }
    
    /**
     *
     */
    public AttributeList getAttributes(String[] str) {
        throw new UnsupportedOperationException(
                sm.getString("monitor.jndi.unsupported_method"));
    }
    
    /**
     *
     */
    public MBeanInfo getMBeanInfo() {
        if(mbeanInfo == null) {
            mbeanInfo = new MBeanInfo(this.getClass().getName(),
                "Managed Object for " + this.getClass().getName(), 
                null, null, getOperationInfo(), null);
        } 
        return mbeanInfo;
    }
    
    /**
     *
     */
    MBeanOperationInfo[] getOperationInfo() {
        Method[] methods = this.getClass().getMethods();
        MBeanOperationInfo[] mInfo = new MBeanOperationInfo[methods.length];
        for(int i= 0; i < methods.length; i++){
           mInfo[i]= createOperationInfo(methods[i]);                     
        }
        return mInfo;
    }
    
    /**
     *
     */
    MBeanOperationInfo createOperationInfo(Method method){
        return new MBeanOperationInfo(method.getName(), 
            "Method " + method.getName(),
            getParameterInfo(method.getParameterTypes()),
            method.getReturnType().getName(), 
            MBeanOperationInfo.INFO);        
    }
    
    /**
     * 
     */ 
    MBeanParameterInfo[] getParameterInfo(Class[] paramTypes){ 
        MBeanParameterInfo[] params=null;
        if(paramTypes != null){
            params = new MBeanParameterInfo[paramTypes.length];
            for(int i = 0; i < paramTypes.length; i++){
                try {
                    params[i] = new MBeanParameterInfo("param" + i,
                                paramTypes[i].getName(),
                                paramTypes[i].getName());
                } catch(java.lang.IllegalArgumentException e){
                    logger.log(Level.INFO, e.toString());                                    
                }
            }
        }
        return params;
    }
    
    /**
     *
     */
    boolean isAttrGetterOrSetter(Method operation){
        if(operation.getName().startsWith("get") 
            || operation.getName().startsWith("set")){
            return true;
        }
        return false;
    }
    
    /**
     *
     */
    public Object invoke(String str, Object[] obj, String[] str2) 
            throws MBeanException, ReflectionException {
        Object a = null;
        Class[] c = new Class[str2.length];
        for(int i=0; i < str2.length; i++){
            c[i] = str2[i].getClass();
        }
        try {
           a = (Object)this.getClass().getMethod(str, c).invoke(this, obj);
        } catch(InvocationTargetException e){
            logger.log(Level.INFO,e.getMessage(), e);
            MBeanException me = 
                new MBeanException((Exception)e.getTargetException());
            throw me;
        } catch (Exception e) {
            throw new MBeanException(e);
        }
        return a;
    }
    
    /**
     *
     */
    public void setAttribute(javax.management.Attribute attribute) 
            throws AttributeNotFoundException, InvalidAttributeValueException, 
                MBeanException, ReflectionException {
        throw new UnsupportedOperationException(
                sm.getString("monitor.jndi.unsupported_method"));
    }
    
    /**
     *
     */
    public AttributeList setAttributes(AttributeList attributeList) {
        throw new UnsupportedOperationException(
                sm.getString("monitor.jndi.unsupported_method"));
    }
   
    /**
     * Gets all the jndi entry names given a specific context name. This 
     * method uses the JndiMBeanHelper object to execute all logic involved
     * in querying entries via jndi to the application server's naming
     * service.
     *
     * @param context The context name under which the names reside.
     * @return An array of {@link NameClassPair} objects representing the jndi entries.
     * @throws {@link MBeanException} if there is an error getting the entries.
     * @see JndiMBeanHelper#getJndiEntriesByContextPath(String)
     */
    public java.util.ArrayList getNames(String context) 
            throws NamingException {
        java.util.ArrayList names = null;
        names = helper.getJndiEntriesByContextPath(context);
        return names;
    }
}
