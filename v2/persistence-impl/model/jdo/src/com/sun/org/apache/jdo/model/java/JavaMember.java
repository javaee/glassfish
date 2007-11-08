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

package com.sun.org.apache.jdo.model.java;

/**
 * This is the super interface for named JavaModel elements having a declaring
 * class such as JavaField, JavaMethod, etc.
 * 
 * @author Michael Bouschen
 * @since JDO 2.0
 */
public interface JavaMember extends JavaElement 
{
    /**
     * Returns the name of the member. 
     * @return member name
     */
    public String getName();

    /**
     * Returns the JavaType instance representing the class or interface
     * that declares the member represented by this JavaMember instance.
     * @return the JavaType instance of the declaring class.
     */
    public JavaType getDeclaringClass();

    /**
     * Returns the Java language modifiers for the field represented by
     * this JavaMember, as an integer. The java.lang.reflect.Modifier class
     * should be used to decode the modifiers. 
     * @return the Java language modifiers for this JavaMember
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers();

    /**
     * Returns the JavaType representation of the type of the member.
     * @return type of the member
     */
    public JavaType getType();

    /**
     * Returns the JavaType representation of the component type of the type
     * of the member, if the field type is an array or collection. The
     * method returns <code>null</code>, if the member type is not an array
     * or collection.
     * @return the component type of the member type in case of an array or
     * collection.
     */
    public JavaType getComponentType();
}
