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

import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.xml.api.XmlService;

/**
 * @author jwells
 *
 */
public class QNameUtilities {
    
    /**
     * Returns the namespace after accounting for null or empty strings
     * 
     * @param namespace The possibly null namespace
     * @return The non-null namespace
     */
    public static final String fixNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty() || namespace.trim().isEmpty()) {
            return XmlService.DEFAULT_NAMESPACE;
        }
        
        return namespace;
    }
    
    public static QName createQName(String namespace, String localPart) {
        return createQName(namespace, localPart, null);
    }
    
    /**
     * Creates a QName taking into account the DEFAULT_NAMESPACE field
     * from JAXB
     * 
     * @param namespace The possibly null namespace
     * @param localPart The not-null localPart
     * @param defaultNamespace The default namespace if known, or null if not known
     * @return
     */
    public static QName createQName(String namespace, String localPart, String defaultNamespace) {
        if (localPart == null) return null;
        
        if ((namespace == null) || namespace.isEmpty() || namespace.trim().isEmpty() ||
                XmlService.DEFAULT_NAMESPACE.equals(namespace) ||
                ((defaultNamespace != null) && GeneralUtilities.safeEquals(namespace, defaultNamespace))) {
            return new QName(localPart);
        }
        
        return new QName(namespace, localPart);
    }
    
    /**
     * Returns the namespace, but if the namespace is null or
     * empty will return
     * {@link XmlService#DEFAULT_NAMESPACE} instead
     * 
     * @param qName qName to find the namespace of or null
     * @return null if qName is null or the String for the namespace if not null
     */
    public static String getNamespace(QName qName) {
        return getNamespace(qName, null);
    }
    
    /**
     * Returns the namespace, but if the namespace is null or
     * empty will return
     * {@link XmlService#DEFAULT_NAMESPACE} instead
     * 
     * @param qName qName to find the namespace of or null
     * @param defaultNamespace The default namespace if known, or null if not known
     * @return null if qName is null or the String for the namespace if not null
     */
    public static String getNamespace(QName qName, String defaultNamespace) {
        if (qName == null) return null;
        
        String namespace = qName.getNamespaceURI();
        if ((namespace == null) || namespace.isEmpty() || namespace.trim().isEmpty() ||
                ((defaultNamespace != null) && GeneralUtilities.safeEquals(defaultNamespace, namespace))) {
            return XmlService.DEFAULT_NAMESPACE;
        }
        
        return namespace;
        
    }

}
