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

/* GeneratedMonitoringMBeanImpl.java
 * $Id: GeneratedMonitoringMBeanImpl.java,v 1.6 2007/04/17 22:26:13 sirajg Exp $
 * $Revision: 1.6 $
 * $Date: 2007/04/17 22:26:13 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim - 
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio - 
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi;
//import com.sun.enterprise.admin.monitor.registry.StatsHolder;
import javax.management.*;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.*;
import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.lang.*;

/**
 * A generic dynamic mbean implementation for Monitoring which instruments a
 * JSR77 compliant Stats object to be managed by this JMX managed object.
 * @author  <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 */
public class GeneratedMonitoringMBeanImpl implements DynamicMBean {
    volatile MBeanInfo mbeanInfo;
    final Stats resourceInstance;
    public static final String LOGGER_NAME="this.is.console";
    final Logger logger;
    final HashSet<String> attributeSet;
    //StatsHolder statsHolder = null;
    /**
     * constructs a monitoring mbean that manages an underlying Stats resource
     */
    public GeneratedMonitoringMBeanImpl(Stats stats) {
        logger = Logger.getLogger(LOGGER_NAME);
        this.resourceInstance=stats;
        attributeSet=new HashSet<String>();
    }
    
    /**
     * introspects the underlying Stats resource and generates an MBeanInfo object
     * and populates attributes  internally for later use culled from the
     * Statistic objects returned by the Stats object.
     * @param javax.management.j2ee.statistics.Stats
     * @param javax.management.MBeanInfo
     */
    final MBeanInfo introspect(){
        // !!! 'mbeanInfo' must be 'volatile' !!!
        // this is thread-safe, not the double-null-check idiom so long as
        // 'mbeanInfo' is 'volatile'
        if ( mbeanInfo != null ) {
            return mbeanInfo;
        }
        
        synchronized( this ) {
            if ( mbeanInfo == null ) {
                ManagedResourceIntrospector mri = new ManagedResourceIntrospector(this);
                mbeanInfo = mri.introspect(this.resourceInstance);
                setUpAttributeSet();
            }
        }
        
        return mbeanInfo;
    }
    /**
     * sets up the internal data structure to hold attributes derived from
     * Statistic objects returned by the Stats object.
     */
    private void setUpAttributeSet(){
        MBeanAttributeInfo[] attrInfo = mbeanInfo.getAttributes();
        String attr =null;
        for(int i=0;i<attrInfo.length;i++){
            attr = attrInfo[i].getName();
            attributeSet.add(attr);
        }
    }
    
    /**
     * Implementation of DynamicMBean interface's method. Parses the passed in
     * attribute into two portions.
     * One portion invokes the corresponding method in the Stats object and
     * the second portion is used to invoke the underlying method in the
     * Statistic object returned by the Stats object in the first invocation.
     * Example of an attribute pattern: For JVMStats, HeapSize_UpperBound would
     * translate into JVMStats.getHeapSize() which returns a BoundedRangeStatistic
     * object. The BoundedRangeStatistic.getUpperBound() method is invoked
     * to return the value of the attribute to the client.
     * @param java.lang.String
     * @return java.lang.Object
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.MBeanException
     * @throws javax.management.ReflectionException
     */
    public Object getAttribute(String str)
    throws javax.management.AttributeNotFoundException,
    javax.management.MBeanException, javax.management.ReflectionException {
        if(str == null){
            throw new NullPointerException("An attribute needs to be specified to get value");
        }
        
        introspect();

        if(!isValidAttribute(str)){
            throw new AttributeNotFoundException("The requested attribute is not recognized");
        }
        Statistic a = null;
        Object retval=null;
        String[] attrParts = AttributeStringHelper.splitAttribute(str);
        logger.log(Level.INFO,"accessing the Stats object with attr="+attrParts[0]);
        a = (Statistic)resourceInstance.getStatistic(attrParts[0]);
        if(a == null){
            try{
                a = (Statistic)resourceInstance.getClass().
                getMethod("get"+str).
                invoke(resourceInstance);
            }
            catch(Exception e){
                logger.log(Level.INFO,e.getLocalizedMessage());
            }
        }
        //note: do not change this to an "else{ }" block
        if(a != null){
            try{
                retval = a.getClass().getMethod("get"+attrParts[1]).invoke(a);
            }
            catch(Exception e){
                logger.log(Level.INFO,e.getLocalizedMessage());
                logger.log(Level.FINE,e.getStackTrace().toString());
            }
        }
        return retval;
    }
    
    /**
     * checks if the passed in string is a recognized attribute
     * @param java.lang.String
     * @return boolean
     */
    private boolean isValidAttribute(String str){
        if(attributeSet.contains(str))
            return true;
        return false;
    }
    
    /**
     * Implementation of DynamicMBean interface's method. Loops through the
     * passed in String[] and calls getAttribute(str) for each element.
     * @param java.lang.String[]
     * @return javax.management.AttributeList
     */
    public javax.management.AttributeList getAttributes(String[] str) {
        introspect();
        
        AttributeList list = new AttributeList();
        try{
            for(int i=0; i<str.length;i++){
                list.add(i, new Attribute(str[i],getAttribute(str[i])));
            }
        }
        catch(Exception e){
            logger.log(Level.INFO,e.getMessage()+"\n"+e.getCause().toString());
        }
        return list;
    }
    
    /**
     * Implementation of DynamicMBean interface's method. Returns the MBeanInfo
     * object that was generated during introspection of the underlying Stats
     * resource.
     * @return javax.management.MBeanInfo
     */
    public javax.management.MBeanInfo getMBeanInfo() {
        introspect();
        return mbeanInfo;
    }
    
    public Object invoke(String str, Object[] obj, String[] str2) throws
    javax.management.MBeanException, javax.management.ReflectionException{
        introspect();
        Object a =null;
        Class[] c = new Class[]{};
        for(int i=0; i<str2.length;i++){
            c[i] = str2[i].getClass();
        }
        try{
            a = (Object) resourceInstance.getClass().getMethod(str, c).invoke(resourceInstance, obj);
        }
        catch(Exception e){
            logger.log(Level.INFO,e.getLocalizedMessage());
            logger.log(Level.FINE,e.getStackTrace().toString());
        }
        return a;
    }
    
    /**
     * Implementation of DynamicMBean interface's method - NO-OP. 
     * @param javax.management.Attribute
     * @throws javax.management.AttributeNotFoundException
     * @throws javax.management.InvalidAttributeValueException
     * @throws javax.management.MBeanException
     * @throws javax.management.ReflectionException
     */
    public void setAttribute(javax.management.Attribute attribute) throws
    javax.management.AttributeNotFoundException,
    javax.management.InvalidAttributeValueException,
    javax.management.MBeanException,
    javax.management.ReflectionException {   }
    
    /**
     * Implementation of DynamicMBean interface's method. Sets a list of attributes.
     * Iterates through the list and calls setAttribute() for each element.
     * @param javax.management.AttributeList
     * @return javax.management.AttributeList
     */
    public javax.management.AttributeList setAttributes(
        javax.management.AttributeList attributeList) {
        return new AttributeList();
    }
    
    /**
     * Returns String[] of attribute names.
     * @return String[]
     */
    public String[] listAttributes(){
        String[] array = new String[attributeSet.size()];
        attributeSet.toArray(array);
        return array;
    }
}
