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

package com.sun.org.apache.jdo.impl.model.java.runtime.jdk5;

import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaMethod;
import com.sun.org.apache.jdo.model.java.JavaProperty;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.model.jdo.JDOField;

import java.lang.reflect.Field;
import com.sun.org.apache.jdo.impl.model.java.runtime.*;

/**
/**
 * A reflection based JavaType implementation used at runtime in a
 * J2SE5.0 environment.  
 * The implementation takes <code>java.lang.Class</code> and
 * <code>java.lang.reflect.Field</code> instances to get Java related
 * metadata about types and fields. 
 *
 * @author Michael Bouschen
 */
public class JDK5RuntimeJavaType
    extends RuntimeJavaType
{
    /** Constructor. */
    public JDK5RuntimeJavaType(Class clazz, JDK5RuntimeJavaModel javaModel)
    {
        super(clazz, javaModel);
    }

   /**
     * Creates a new instance of the JavaField implementation class.
     * <p>
     * This implementation returns a <code>JDK5RuntimeJavaField</code>
     * instance.
     * @return a new JavaField instance.
     */
    protected JavaField newJavaFieldInstance(String name, JavaType type) 
    {
        return new JDK5RuntimeJavaField(name, type, this);
    }
    
    /**
     * Creates a new instance of the JavaField implementation class.
     * <p>
     * This implementation returns a <code>ReflectionJavaField</code>
     * instance.
     * @return a new JavaField instance.
     */

    protected JavaField newJavaFieldInstance(Field field) 
    {
        return new JDK5RuntimeJavaField(field, this);
    }
    
    /**
     * Creates a new instance of the JavaProperty implementation class.
     * <p>
     * This implementation returns a <code>JavaPropertyImpl</code>
     * instance.
     * @return a new JavaProperty instance.
     */
    protected JavaProperty newJavaPropertyInstance(String name, 
            JavaMethod getter, JavaMethod setter, JavaType type) 
    {
        return new JDK5RuntimeJavaProperty(name, getter, setter, type, this);
    }
    
}
