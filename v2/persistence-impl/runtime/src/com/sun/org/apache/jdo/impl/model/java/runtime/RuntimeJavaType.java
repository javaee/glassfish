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

import com.sun.org.apache.jdo.impl.model.java.reflection.ReflectionJavaType;
import com.sun.org.apache.jdo.model.ModelFatalException;
import com.sun.org.apache.jdo.model.java.JavaField;
import com.sun.org.apache.jdo.model.java.JavaModel;
import com.sun.org.apache.jdo.model.java.JavaType;
import com.sun.org.apache.jdo.util.I18NHelper;

/**
 * A reflection based JavaType implementation used at runtime.  
 * The implementation takes <code>java.lang.Class</code> and
 * <code>java.lang.reflect.Field</code> instances to get Java related
 * metadata about types and fields. 
 *
 * @author Michael Bouschen
 * @since JDO 1.0.1
 * @version JDO 2.0
 */
public class RuntimeJavaType
    extends ReflectionJavaType
{
    /** I18N support */
    private final static I18NHelper msg =  
        I18NHelper.getInstance(RuntimeJavaType.class);

    /** Constructor. */
    public RuntimeJavaType(Class clazz, RuntimeJavaModel javaModel)
    {
        super(clazz, javaModel);
    }

    // ===== Methods not specified in JavaType =====

    /**
     * RegisterClassListener calls this method to create a ReflectionJavaField
     * instance when processing the enhancer generated metadata.
     * @param name the name of the field
     * @param type the type of the field
     * @return the ReflectionJavaField representation
     */
    public synchronized JavaField createJavaField(String name, JavaType type)
    {
        JavaField javaField = (JavaField)declaredJavaFields.get(name);
        if (javaField != null) {
            throw new ModelFatalException(msg.msg(
                "ERR_MultipleJavaField", //NOI18N
                "RuntimeJavaType.createJavaField", name, getName())); //NOI18N
        }
        javaField = newJavaFieldInstance(name, type);
        declaredJavaFields.put(name, javaField);
        return javaField;
    }
}
