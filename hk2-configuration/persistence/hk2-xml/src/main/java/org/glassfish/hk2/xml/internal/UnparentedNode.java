/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * This tells things about a node in an XML tree, without any
 * reference to the parent of the node.  That is because
 * the node can be parented by anyone, and hence information
 * about the parent would make it specific to a certain tree
 * or location in a tree
 * 
 * @author jwells
 *
 */
public class UnparentedNode {
    private final Class<?> originalInterface;
    private Class<?> translatedClass;
    private String rootName;
    private final Map<Class<?>, ParentedNode> children = new HashMap<Class<?>, ParentedNode>();
    private String keyProperty;
    
    public UnparentedNode(Class<?> originalInterface) {
        this.originalInterface = originalInterface;
    }
    
    public Class<?> getOriginalInterface() {
        return originalInterface;
    }
    
    /**
     * @return the translatedClass
     */
    public Class<?> getTranslatedClass() {
        return translatedClass;
    }

    /**
     * @param translatedClass the translatedClass to set
     */
    public void setTranslatedClass(Class<?> translatedClass) {
        this.translatedClass = translatedClass;
    }

    /**
     * @return the rootName
     */
    public String getRootName() {
        return rootName;
    }

    /**
     * @param rootName the rootName to set
     */
    public void setRootName(String rootName) {
        this.rootName = rootName;
    }
    
    public void addChild(String xmlTag, UnparentedNode child) {
        synchronized (children) {
            children.put(child.getOriginalInterface(), new ParentedNode(xmlTag, child));
        }
    }
    
    public ParentedNode getChild(Class<?> childType) {
        synchronized (children) {
            return children.get(childType);
        }
    }

    /**
     * @return the keyProperty
     */
    public String getKeyProperty() {
        return keyProperty;
    }

    /**
     * @param keyProperty the keyProperty to set
     */
    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }

    @Override
    public String toString() {
        return "UnparentedNode(" + originalInterface + "," + System.identityHashCode(this) + ")";
    }
}
