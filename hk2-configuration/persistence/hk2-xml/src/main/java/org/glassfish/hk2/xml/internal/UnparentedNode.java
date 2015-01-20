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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
public class UnparentedNode implements Serializable {
    private static final long serialVersionUID = -1875168445525432246L;

    /** A lock for concurrency */
    private final Object lock = new Object();
    
    /** The interface from which the JAXB proxy was created */
    private Class<?> originalInterface;
    
    /** The JAXB proxy of the originalInterface */
    private Class<?> translatedClass;
    
    /** If this node can be a root, the xml tag of the root of the document */
    private String rootName;
    
    /** A map from an interface to a parented child node */
    private final Map<Class<?>, ParentedNode> children = new HashMap<Class<?>, ParentedNode>();
    
    /** A map from the property name (not the xml tag) to the parented child node */
    private final Map<String, ParentedNode> childrenByName = new HashMap<String, ParentedNode>();
    
    /** A set of all non-child properties of this node */
    private final Set<String> nonChildProperty = new HashSet<String>();
    
    private Set<String> unKeyedChildren = null;
    
    /** If this node has a key, this is the property name of the key */
    private String keyProperty;
    
    public UnparentedNode() {
    }
    
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
    
    public void addChild(String xmlTag, boolean multiChildList, boolean multiChildArray, UnparentedNode child) {
        synchronized (lock) {
            ParentedNode pn = new ParentedNode(xmlTag, multiChildList, multiChildArray, child);
            children.put(child.getOriginalInterface(), pn);
            childrenByName.put(xmlTag, pn);
        }
    }
    
    public void addNonChildProperty(String xmlTag) {
        synchronized (lock) {
            nonChildProperty.add(xmlTag);
        }
    }
    
    public ParentedNode getChild(Class<?> childType) {
        synchronized (lock) {
            return children.get(childType);
        }
    }
    
    public ParentedNode getChild(String propName) {
        synchronized (lock) {
            return childrenByName.get(propName);
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
    
    public Set<String> getUnKeyedChildren() {
        synchronized (lock) {
            if (unKeyedChildren != null) return unKeyedChildren;
            
            unKeyedChildren = new HashSet<String>();
            
            for (Map.Entry<String, ParentedNode> entry : childrenByName.entrySet()) {
                if (entry.getValue().getChild().getKeyProperty() != null) continue;
                unKeyedChildren.add(entry.getKey());
            }
            
            return unKeyedChildren;
        }
    }
    
    public Set<String> getNonChildProperties() {
        synchronized (lock) {
            return Collections.unmodifiableSet(nonChildProperty);
        }
    }
    
    public Map<String, ParentedNode> getChildrenProperties() {
        synchronized (lock) {
            return Collections.unmodifiableMap(childrenByName);
        }
    }

    @Override
    public String toString() {
        return "UnparentedNode(" + originalInterface + "," + System.identityHashCode(this) + ")";
    }
}
