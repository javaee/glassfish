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
package org.glassfish.hk2.xml.internal;

import javax.xml.namespace.QName;

import org.glassfish.hk2.xml.api.XmlService;

/**
 * @author jwells
 *
 */
public class QNameUtilities {
    /**
     * Creates a QName taking into account the DEFAULT_NAMESPACE field
     * from JAXB
     * 
     * @param namespace
     * @param xmlTag
     * @return
     */
    public static QName createQName(String namespace, String xmlTag) {
        if (namespace == null || namespace.isEmpty() || namespace.trim().isEmpty() || XmlService.DEFAULT_NAMESPACE.equals(namespace)) {
            return new QName(xmlTag);
        }
        
        return new QName(namespace, xmlTag);
    }
    
    /**
     * Returns the namespace, but if the namespace is null or
     * empty will return
     * {@link XmlService#DEFAULT_NAMESPACE} instead
     * 
     * @param qName A non-null qName to find the namespace of
     * @return A non-null namespace
     */
    public static String getNamespace(QName qName) {
        String namespace = qName.getNamespaceURI();
        if ((namespace == null) || namespace.isEmpty() || namespace.trim().isEmpty()) {
            return XmlService.DEFAULT_NAMESPACE;
        }
        
        return namespace;
        
    }

}
