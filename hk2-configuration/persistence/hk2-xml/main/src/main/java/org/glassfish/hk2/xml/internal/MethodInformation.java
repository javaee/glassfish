/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.internal;

import org.glassfish.hk2.xml.internal.alt.AltClass;
import org.glassfish.hk2.xml.internal.alt.AltMethod;
import org.glassfish.hk2.xml.internal.alt.MethodInformationI;

/**
 * Information needed for proxy from a method
 * 
 * @author jwells
 *
 */
public class MethodInformation implements MethodInformationI {
    /** The actual method */
    private final AltMethod originalMethod;
    
    /** The type of method, GETTER, SETTER et al */
    private final MethodType methodType;
    
    /**
     * If this is a getter or setter, the type of thing being set,
     * which might be a List or array, it is the true thing the
     * getter or setter of the method is setting
     */
    private final AltClass getterSetterType;
    
    /**
     * The original variable name if this is a getter or setter
     * before being translated to the representedProperty by the
     * XmlElement or XmlAttribute annotation
     */
    private final String decapitalizedMethodProperty;
    
    /** The xml tag for this method */
    private final String representedProperty;
    
    /** The default value specified for this method */
    private final String defaultValue;
    
    /**
     * This is the type of thing being set.  For example
     * if this method is returning a List or Array
     * it'll be the first parameterized Type of the List
     * and if it an Array it'll be the Component type
     * of the Array
     */
    private final AltClass baseChildType;
    
    /**
     * True if this is a key property
     */
    private final boolean key;
    
    /**
     * True if this is a List child method
     */
    private final boolean isList;
    
    /**
     * True if this is an array child method
     */
    private final boolean isArray;
    
    /**
     * True if this is a setter or getter for a reference
     */
    private final boolean isReference;
    
    /**
     * True if this is considered an element, false if it is an attribute
     */
    private final boolean isElement;
    
    /**
     * The parameterized type of the list if known
     */
    private final AltClass listParameterizedType;
    
    public MethodInformation(AltMethod originalMethod,
            MethodType methodType,
            String decapitalizedMethodProperty,
            String representedProperty,
            String defaultValue,
            AltClass baseChildType,
            AltClass gsType,
            boolean key,
            boolean isList,
            boolean isArray,
            boolean isReference,
            boolean isElement,
            AltClass listParameterizedType) {
        this.originalMethod = originalMethod;
        this.methodType = methodType;
        this.decapitalizedMethodProperty = decapitalizedMethodProperty;
        this.representedProperty = representedProperty;
        this.defaultValue = defaultValue;
        this.baseChildType = baseChildType;
        this.getterSetterType = gsType;
        this.key = key;
        this.isList = isList;
        this.isArray = isArray;
        this.isReference = isReference;
        this.isElement = isElement;
        this.listParameterizedType = listParameterizedType;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#getOriginalMethod()
     */
    @Override
    public AltMethod getOriginalMethod() {
        return originalMethod;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#getMethodType()
     */
    @Override
    public MethodType getMethodType() {
        return methodType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#getGetterSetterType()
     */
    @Override
    public AltClass getGetterSetterType() {
        return getterSetterType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#getRepresentedProperty()
     */
    @Override
    public String getRepresentedProperty() {
        return representedProperty;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#getDefaultValue()
     */
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#getBaseChildType()
     */
    @Override
    public AltClass getBaseChildType() {
        return baseChildType;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#isKey()
     */
    @Override
    public boolean isKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#isList()
     */
    @Override
    public boolean isList() {
        return isList;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#isArray()
     */
    @Override
    public boolean isArray() {
        return isArray;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#isReference()
     */
    @Override
    public boolean isReference() {
        return isReference;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#getDecapitalizedMethodProperty()
     */
    @Override
    public String getDecapitalizedMethodProperty() {
        return decapitalizedMethodProperty;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.internal.MethodInformationI#isElement()
     */
    @Override
    public boolean isElement() {
        return isElement;
    }
    
    @Override
    public AltClass getListParameterizedType() {
        return listParameterizedType;
    }
    
    @Override
    public String toString() {
        return "MethodInformation(name=" + originalMethod.getName() + "," +
          "type=" + methodType + "," +
          "getterType=" + getterSetterType + "," +
          "decapitalizedMethodProperty=" + decapitalizedMethodProperty + "," +
          "representedProperty=" + representedProperty + "," +
          "defaultValue=" + ((Generator.JAXB_DEFAULT_DEFAULT.equals(defaultValue)) ? "" : defaultValue) + "," +
          "baseChildType=" + baseChildType + "," +
          "key=" + key + "," +
          "isList=" + isList + "," +
          "isArray=" + isArray + "," +
          "isReference=" + isReference + "," +
          "isElement=" + isElement + "," +
          "listParameterizedType=" + listParameterizedType + "," +
          System.identityHashCode(this) + ")";
          
    }

    
}