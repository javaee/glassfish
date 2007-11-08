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
 * Copyright 2005 The Apache Software Foundation.
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

package com.sun.org.apache.jdo.impl.model.java.runtime;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.security.AccessController;
import java.security.PrivilegedAction;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOClass;
import com.sun.org.apache.jdo.model.jdo.JDOField;
import com.sun.org.apache.jdo.model.jdo.JDOModel;
import com.sun.org.apache.jdo.model.jdo.PersistenceModifier;
import com.sun.persistence.support.spi.JDOImplHelper;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.persistence.support.spi.RegisterClassEvent;

/**
 * The Model listener gets notified whenever a persistence-capable class gets 
 * registered with the JDOImplHelper at runtime.
 *
 * @author Michael Bouschen
 */
public class RegisterClassListener
    implements com.sun.persistence.support.spi.RegisterClassListener
{
    /** The corresponding JDOImplHelper instance. */
    JDOImplHelper helper;

    /** The JavaModel factory. */
    RuntimeJavaModelFactory javaModelFactory;

    /** Logger. */
    private static Log logger =
        LogFactory.getFactory().getInstance("com.sun.org.apache.jdo.impl.model.jdo"); // NOI18N

    /** 
     * Constructor. 
     * @param helper the JDOImplHelper instance.
     * @param javaModelFactory the JavaModel factory.
     */
    public RegisterClassListener(JDOImplHelper helper, 
                                 RuntimeJavaModelFactory javaModelFactory)
    {
        this.helper = helper;
        this.javaModelFactory = javaModelFactory;
    }

    /**
     * This method gets called when a persistence-capable class is registered.
     * @param event a RegisterClassEvent instance describing the registered 
     * class plus metatdata.
     */
    public void registerClass(RegisterClassEvent event)
    {
        if (logger.isDebugEnabled())
            logger.debug("RegisterClassListener.registerClass " + //NOI18N
                         event.getRegisteredClass());
        try {
            updateJDOClass(createJDOClass(event.getRegisteredClass()),
                           event.getFieldNames(), 
                           event.getFieldTypes(),
                           event.getFieldFlags(),
                           event.getPersistenceCapableSuperclass());
        }
        catch (ModelException ex) {
            // ignore error message
            logger.error("Problems updating JDOModel", ex); //NOI18N
            System.out.println("caught " + ex); //NOI18N
        }
    }
    
    /** 
     * Internal method to update the corresponding JDOClass instance with the 
     * runtime meta data.
     * @param pcClass the class object of the persistence-capable class
     */
    private JDOClass createJDOClass(Class pcClass)
        throws ModelException
    {
        String pcClassName = pcClass.getName();
        ClassLoader classLoader = 
            javaModelFactory.getClassLoaderPrivileged(pcClass);
        JavaModel javaModel = javaModelFactory.getJavaModel(classLoader);
        JDOModel jdoModel = javaModel.getJDOModel();
        // do not load XML here, this will be done on first request
        JDOClass jdoClass = jdoModel.createJDOClass(pcClassName, false);
        JavaType javaType = javaModel.getJavaType(pcClass);
        jdoClass.setJavaType(javaType);
        return jdoClass;
    }

    /** 
     * Internal method to update the specified JDOClass instance with the 
     * runtime meta data.
     * @param jdoClass the jdoClass instance to be updated
     * @param fieldNames the names of the managed fields
     * @param fieldTypes the types of the managed fields
     * @param fieldFlags the jdo field flags of the managed fields
     * @param pcSuperclass the class object of the persistence-capable 
     * superclass
     */
    private void updateJDOClass(JDOClass jdoClass,
                                String[] fieldNames, 
                                Class[] fieldTypes,
                                byte[] fieldFlags,
                                Class pcSuperclass)
        throws ModelException
    {
        // handle superclass
        if (pcSuperclass != null) {
            ClassLoader classLoader = 
                javaModelFactory.getClassLoaderPrivileged(pcSuperclass);
            JavaModel superJavaModel = 
                javaModelFactory.getJavaModel(classLoader);
            JDOModel superJDOModel = superJavaModel.getJDOModel();
            // do not load XML => last arg should be false
            JDOClass superJDOClass = 
                superJDOModel.getJDOClass(pcSuperclass.getName(), false);
            jdoClass.setPersistenceCapableSuperclass(superJDOClass);
        }
        
        // Iterate the field names and set the corresponding field type
        RuntimeJavaType declaringClass = (RuntimeJavaType)jdoClass.getJavaType();
        for (int i = 0; i < fieldNames.length; i++) {
            JDOField jdoField = jdoClass.createJDOField(fieldNames[i]);
            updateJDOField(jdoField, fieldTypes[i], fieldFlags[i],
                           declaringClass);
        }
    }

    /** 
     * Internal method to update the specified JDOField instance with the 
     * runtime meta data.
     * @param jdoField the jdoField instance to be updated
     * @param fieldType the type of the field
     * @param fieldFlags the jdo field flags
     */
    private void updateJDOField(JDOField jdoField,
                                Class fieldType,
                                byte fieldFlags,
                                RuntimeJavaType declaringClass)
        throws ModelException
    {
        // handle JavaField
        JavaField javaField = declaringClass.createJavaField(jdoField.getName(), 
            javaModelFactory.getJavaType(fieldType));
        jdoField.setJavaField(javaField);
        
        // handle field flags
        jdoField.setSerializable(
            (fieldFlags & PersistenceCapable.SERIALIZABLE) > 0);
    }
}

