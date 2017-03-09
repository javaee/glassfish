/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.hk2.xml.internal.alt;

import org.glassfish.hk2.xml.internal.MethodType;

public interface MethodInformationI {

    /**
     * @return the originalMethod
     */
    public AltMethod getOriginalMethod();

    /**
     * @return the methodType
     */
    public MethodType getMethodType();

    /**
     * @return the getterSetterType
     */
    public AltClass getGetterSetterType();

    /**
     * @return the representedProperty
     */
    public String getRepresentedProperty();

    /**
     * @return the defaultValue
     */
    public String getDefaultValue();
    
    /**
     * @return The wrapper tag or null if there is none
     */
    public String getWrapperTag();

    /**
     * @return the baseChildType
     */
    public AltClass getBaseChildType();

    /**
     * @return true if this is a method that returns a key
     */
    public boolean isKey();

    /**
     * @return true if the method is for a List
     */
    public boolean isList();

    /**
     * @return true if the method is for an array
     */
    public boolean isArray();

    /**
     * @return true if this is a reference method
     */
    public boolean isReference();

    /**
     * @return The decapitilized version of the property name
     */
    public String getDecapitalizedMethodProperty();

    /**
     * @return true if this is for an element (as opposed to an attribute)
     */
    public boolean isElement();
    
    /**
     * Returns the parameterized type of the
     * list, or null if this is not a list or
     * the type of the list is unknown
     * 
     * @return The fully qualified class name
     * of the lists parameterized type
     */
    public AltClass getListParameterizedType();

}