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

package com.sun.org.apache.jdo.impl.model.java;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.InputStream;

import com.sun.org.apache.jdo.model.ModelException;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOModel;


/**
 * Abstract super class for JavaModel implementations. 
 * It implements the jdoModel property and the parent/child relationship
 * between javaModels. It also provides a map of types managed by this
 * JavaModel (see {@link #types}). The AbstractJavaModel constructor
 * automatically adds all the predefined types to this map.
 * <p>
 * A non-abstract subclass must implement methods
 * {@link #getJavaType(String name)} and 
 * {@link #getInputStreamForResource(String resourceName)}.
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 */
abstract public class AbstractJavaModel
    implements JavaModel
{
    /** Map of known JavaTypes. Key is the type name as a string. */
    protected Map types;

    /** The parent JavaModel. */
    protected JavaModel parent;

    /** The child JavaModels. */
    protected Set children = new HashSet();

    /** The corresponding JDOModel instance. */
    protected JDOModel jdoModel;
    
    /**
     * Constructor. It adds all predefined types to the cache of types
     * known by this model instance.
     * @see PredefinedType
     */
    protected AbstractJavaModel()
    {
        types = new HashMap(PredefinedType.getPredefinedTypes());
    }

    /** 
     * The method returns the JavaType instance for the specified type
     * name. A type name is unique within one JavaModel instance. The
     * method returns <code>null</code> if this model instance does not
     * know a type with the specified name.
     * @return a JavaType instance for the specified name or
     * <code>null</code> if not present in this model instance.
     */
    abstract public JavaType getJavaType(String name);

    /** 
     * The method returns the JavaType instance for the type name of the
     * specified class object. This is a convenience method for 
     * <code>getJavaType(clazz.getName())</code>. The major difference
     * between this method and getJavaType taking a type name is that this 
     * method is supposed to return a non-<code>null<code> value. The
     * specified class object describes an existing type.
     * @param clazz the Class instance representing the type
     * @return a JavaType instance for the name of the specified class
     * object.
     */
    public JavaType getJavaType(Class clazz)
    {
        return (clazz == null) ? null : getJavaType(clazz.getName());
    }

    /**
     * Finds a resource with a given name. A resource is some data that can
     * be accessed by class code in a way that is independent of the
     * location of the code. The name of a resource is a "/"-separated path
     * name that identifies the resource. The method method opens the
     * resource for reading and returns an InputStream. It returns 
     * <code>null</code> if no resource with this name is found or if the 
     * caller doesn't have adequate privileges to get the resource.
     * @param resourceName the resource name
     * @return an input stream for reading the resource, or <code>null</code> 
     * if the resource could not be found or if the caller doesn't have
     * adequate privileges to get the resource. 
     */
    abstract public InputStream getInputStreamForResource(String resourceName);

    /**
     * Returns the parent JavaModel instance of this JavaModel.
     * @return the parent JavaModel
     */
    public JavaModel getParent()
    {
        return parent;
    }

    /**
     * Set the parent JavaModel for this JavaModel. The method
     * automatically adds this JavaModel to the collection of children
     * of the specified parent JavaModel.
     * @param parent the parent JavaModel
     * @exception ModelException if impossible
     */
    public void setParent(JavaModel parent)
        throws ModelException
    {
        if (this.parent == parent) {
            // no changes => return;
            return;
        }
        if (this.parent != null) {
            // remove this from the collection of children of the old parent
            ((AbstractJavaModel)this.parent).children.remove(this);
            // add this to the collection of children of the new parent
            ((AbstractJavaModel)parent).children.add(this);
        }
        this.parent = parent;
    }

    /**
     * Returns a collection of child JavaModel instances in the form
     * of an array. All instances from the returned array have this
     * JavaModel instance as parent.
     * @return the child JavaModel instances
     */
    public JavaModel[] getChildren()
    {
        return (JavaModel[])children.toArray(new JavaModel[children.size()]);
    }

    /**
     * Returns the corresponding JDOModel instance.
     * @return the corresponding JDOModel.
     */
    public JDOModel getJDOModel()
    {
        return jdoModel;
    }

    /**
     * Sets the corresponding JDOModel instance.
     * @param jdoModel the JDOModel instance
     * @exception ModelException if impossible
     */
    public void setJDOModel(JDOModel jdoModel)
        throws ModelException
    {
        this.jdoModel = jdoModel;
    }
    
}

