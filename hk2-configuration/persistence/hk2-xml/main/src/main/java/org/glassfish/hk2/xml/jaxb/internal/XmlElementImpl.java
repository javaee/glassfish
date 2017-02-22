/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.xml.jaxb.internal;

import javax.xml.bind.annotation.XmlElement;

import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.xml.internal.Utilities;

/**
 * @author jwells
 *
 */
public class XmlElementImpl extends AnnotationLiteral<XmlElement> implements XmlElement {
    private static final long serialVersionUID = -5015658933035011114L;
    
    private final String name;
    private final boolean nillable;
    private final boolean required;
    private final String namespace;
    private final String defaultValue;
    private final Class<?> type;
    private final String typeByName;

    /**
     * 
     */
    public XmlElementImpl(String name, boolean nillable, boolean required, String namespace, String defaultValue, Class<?> type) {
        this.name = name;
        this.nillable = nillable;
        this.required = required;
        this.namespace = namespace;
        this.defaultValue = defaultValue;
        this.type = type;
        typeByName = (type == null) ? "null" : type.getName();
    }
    
    public XmlElementImpl(String name, boolean nillable, boolean required, String namespace, String defaultValue, String typeByName) {
        this.name = name;
        this.nillable = nillable;
        this.required = required;
        this.namespace = namespace;
        this.defaultValue = defaultValue;
        this.type = null;
        this.typeByName = typeByName;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.XmlElement#name()
     */
    @Override
    public String name() {
        return name;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.XmlElement#nillable()
     */
    @Override
    public boolean nillable() {
        return nillable;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.XmlElement#required()
     */
    @Override
    public boolean required() {
        return required;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.XmlElement#namespace()
     */
    @Override
    public String namespace() {
        return namespace;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.XmlElement#defaultValue()
     */
    @Override
    public String defaultValue() {
        return defaultValue;
    }

    /* (non-Javadoc)
     * @see javax.xml.bind.annotation.XmlElement#type()
     */
    @Override
    public Class type() {
        if (type == null) return Object.class;
        return type;
    }
    
    public String getTypeByName() {
        return typeByName;
    }
    
    @Override
    public String toString() {
        return "@XmlElementImpl(name=" + name +
                ",nillable=" + nillable +
                ",required=" + required +
                ",namespace=" + namespace +
                ",defaultValue=" + Utilities.safeString(defaultValue) +
                ",type=" + type +
                ",typeByName=" + typeByName +
                ",sid=" + System.identityHashCode(this) + ")";
    }

}
