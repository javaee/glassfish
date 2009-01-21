/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.apache.catalina.mbeans;


import java.util.ArrayList;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceParams;
import org.apache.tomcat.util.modeler.BaseModelMBean;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.deploy.ContextResource</code> component.</p>
 *
 * @author Amy Roh
 * @version $Revision: 1.3 $ $Date: 2006/03/12 01:27:03 $
 */

public class ContextResourceMBean extends BaseModelMBean {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public ContextResourceMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

    }


    // ----------------------------------------------------- Instance Variables


    // ------------------------------------------------------------- Attributes


    /**
     * Obtain and return the value of a specific attribute of this MBean.
     *
     * @param name Name of the requested attribute
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
    public Object getAttribute(String name)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException {
 
        // Validate the input parameters
        if (name == null)
            throw new RuntimeOperationsException
                (new IllegalArgumentException("Attribute name is null"),
                 "Attribute name is null");

        ContextResource cr = null;
        try {
            cr = (ContextResource) getManagedResource();
        } catch (InstanceNotFoundException e) {
            throw new MBeanException(e);
        } catch (InvalidTargetObjectTypeException e) {
             throw new MBeanException(e);
        }
        
        String value = null;
        if ("auth".equals(name)) {
            return (cr.getAuth());
        } else if ("description".equals(name)) {
            return (cr.getDescription());
        } else if ("name".equals(name)) {
            return (cr.getName());              
        } else if ("scope".equals(name)) {
            return (cr.getScope());  
        } else if ("type".equals(name)) {
            return (cr.getType());
        } else {
            NamingResources nr = cr.getNamingResources(); 
            if (nr == null) {
                throw new AttributeNotFoundException
                    ("Cannot find naming resource "+cr.getName());
            }
            ResourceParams rp = nr.findResourceParams(cr.getName());
            if (rp == null) {
                throw new AttributeNotFoundException
                    ("Cannot find resource param "+cr.getName());
            }
            value = (String) rp.getParameters().get(name);
            if (value == null) {
                throw new AttributeNotFoundException
                    ("Cannot find attribute "+name+rp);
            }
        }
        
        return value;
        
    }

    
    /**
     * Set the value of a specific attribute of this MBean.
     *
     * @param attribute The identification of the attribute to be set
     *  and the new value
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
     public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, MBeanException,
        ReflectionException {

        // Validate the input parameters
        if (attribute == null)
            throw new RuntimeOperationsException
                (new IllegalArgumentException("Attribute is null"),
                 "Attribute is null");
        String name = attribute.getName();
        Object value = attribute.getValue();
        if (name == null)
            throw new RuntimeOperationsException
                (new IllegalArgumentException("Attribute name is null"),
                 "Attribute name is null"); 
        
        ContextResource cr = null;
        try {
            cr = (ContextResource) getManagedResource();
        } catch (InstanceNotFoundException e) {
            throw new MBeanException(e);
        } catch (InvalidTargetObjectTypeException e) {
             throw new MBeanException(e);
        }
        
        if ("auth".equals(name)) {
            cr.setAuth((String)value);
        } else if ("description".equals(name)) {
            cr.setDescription((String)value);
        } else if ("name".equals(name)) {
            cr.setName((String)value);              
        } else if ("scope".equals(name)) {
            cr.setScope((String)value);  
        } else if ("type".equals(name)) {
            cr.setType((String)value);
        } else {
            ResourceParams rp = 
                cr.getNamingResources().findResourceParams(cr.getName());
            if (rp != null) {
                String valueStr = ""+value;
                rp.addParameter(name, valueStr);
                cr.getNamingResources().removeResourceParams(cr.getName());
            } else {
                rp = new ResourceParams();
                rp.setName(cr.getName());
                String valueStr = ""+value;
                rp.addParameter(name, valueStr);
            }
            cr.getNamingResources().addResourceParams(rp);
        }
        
        // cannot use side-efects.  It's removed and added back each time 
        // there is a modification in a resource.
        NamingResources nr = cr.getNamingResources();
        nr.removeResource(cr.getName());
        nr.addResource(cr);
    }
    
}
