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

/* ManagedResourceIntrospector.java
 * $Id: ManagedResourceIntrospector.java,v 1.3 2005/12/25 03:43:34 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:34 $
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
import javax.management.*;
import javax.management.j2ee.statistics.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
/**
 * A helper class that introspects a Managed Resource that is being
 * instrumented by a JMX MBean for manageability. 
 * @author  sg112326
 */
public class ManagedResourceIntrospector {
    final boolean READABLE = true;
    final boolean WRITABLE = true;
    final boolean ISGETTER = true;
  
    DynamicMBean mbean;
    Vector attributes;
    public static final String LOGGER_NAME="this.is.console";
    final Logger logger; 
    
    ManagedResourceIntrospector(DynamicMBean mbean){
        this.mbean=mbean;
        attributes=new Vector();
        logger = Logger.getLogger(LOGGER_NAME);
    }
    
    MBeanInfo introspect(Stats stats){
        return new MBeanInfo(
                mbean.getClass().getName(),//className
                "Managed Object for "+stats.getClass().getName()+ " managed resource",//description
                getAttributeInfo(stats), //AttributeInfo
                null, //constructorInfo
                getOperationInfo(stats), //operationInfo
                null //notifications
                );

    }
    
    /**
     * Creates array of MBeanAttributeInfo objects for attributes 
     * standing for Statistic objects derived from the managed resource's
     * getStatisticNames() method
     */
    MBeanAttributeInfo[] getAttributeInfo(Stats stats){
        MBeanAttributeInfo[] attrInfo=null;
        if(stats != null){
            Object[] attrs = deriveUnderlyingAttributes(stats);
            attrInfo = new MBeanAttributeInfo[attrs.length];
            for(int i= 0; i < attrs.length; i++){
                attrInfo[i] = new MBeanAttributeInfo((String)attrs[i],Statistic.class.getName(),
                                "Attribute"+attrs[i], READABLE, !WRITABLE, !ISGETTER);

            }
        }
        return attrInfo;
    }

    /**
     * From the passed in Stats object, this method determines the underlying 
     * Statistic type and derives from it, attributes that return primitive values.
     * @param Stats
     */
    Object[] deriveUnderlyingAttributes(Stats stats){
        String[] attrs = stats.getStatisticNames();
    
        for(int i=0; i< attrs.length; i++){            
            introspectEachStatistic((stats.getStatistic(attrs[i])).getClass(), attrs[i]);
        }
        String[] a = new String[attributes.size()];
        return attributes.toArray(a);
    }
    
    void introspectEachStatistic(Class statistic, String statName){
        Set a = new HashSet(Arrays.asList(statistic.getMethods()));
        Iterator it = a.iterator();
        while(it.hasNext()){
            String s = (String)((Method) it.next()).getName();
            if(s.startsWith("get")&& !s.equals("getClass")){
                s = s.replaceFirst("get","");
                attributes.add(AttributeStringHelper.joinAttributes(statName,s));
            }
        }  
    }
    
    /**
     * creates array of MBeanOperationInfo objects to determine operations 
     * to be exposed. Excludes the underlying managed resource's methods
     * pertaining to 
     */
    MBeanOperationInfo[] getOperationInfo(Stats stats){
        Method[] opers = stats.getClass().getMethods();
        MBeanOperationInfo[] operInfo = new MBeanOperationInfo[opers.length];
        for(int i= 0; i < opers.length; i++){
            if(!isAttrGetterOrSetter(opers[i])){ 
                operInfo[i]= createOperationInfo(opers[i]);
            }                       
        }
        operInfo = addMoreMBeanOperations(operInfo);        
        return operInfo;
    }

    /**
     * Add any operations defined in the MBean other than ones pertaining
     * directly to Stats or Statistic operations. example: listAtrributes()
     */ 
    private MBeanOperationInfo[] addMoreMBeanOperations(MBeanOperationInfo[] operInfo){
        MBeanOperationInfo oper = new MBeanOperationInfo("listAttributes",//Name
            "Method listAttributes",//Description
            null,//MBeanParameterInfo
            String.class.getName(), //Return Type in String
            MBeanOperationInfo.INFO // Action representing read-only operation
            );        
        MBeanOperationInfo[] opers = new MBeanOperationInfo[operInfo.length+1];
        opers = operInfo;
        opers[opers.length-1] = oper;
        return operInfo;
    }
    
    /**
     * returns true for an operation if it meets the JMX equivalent spec 
     * of distinguishing an get(set)Attribute() or get(set)Attributes() from a
     * non getter/setter operation.
     */
    boolean isAttrGetterOrSetter(Method operation){
        if(operation.getName().startsWith("get") 
            || operation.getName().startsWith("set")){
            return true;
        }
        return false;
    }
    
    /**
     * returns an OperationInfo Object given a Method object
     *
     */
    MBeanOperationInfo createOperationInfo(Method oper){
        return new MBeanOperationInfo(oper.getName(),//Name
            "Method "+oper.getName(),//Description
            getParameterInfo(oper.getParameterTypes()),//MBeanParameterInfo
            oper.getReturnType().getName(), //Return Type in String
            MBeanOperationInfo.INFO // Action representing read-only operation
            );        
    }
    
    /**
     * creates an array of MBeanParameterInfo objects that represent 
     * parameters and their signatures for a given operation
     */ 
    MBeanParameterInfo[] getParameterInfo(Class[] paramTypes){ 
        MBeanParameterInfo[] params=null;
        if(paramTypes != null){
            params = new MBeanParameterInfo[paramTypes.length];
            
            for(int i=0; i<paramTypes.length;i++){
                try{
                    params[i] = new MBeanParameterInfo("param"+i,
                                paramTypes[i].getName(),
                                paramTypes[i].getName());
                }
                catch(java.lang.IllegalArgumentException e){
                    logger.log(Level.INFO, e.toString());                                    
                }
            }
        }
        return params;
    }
}
