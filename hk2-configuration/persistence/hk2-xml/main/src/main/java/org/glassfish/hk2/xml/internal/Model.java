/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.utilities.general.GeneralUtilities;

/**
 * This model is a description of the children and non-children nodes
 * of a Bean.  It contains only Strings or other easy to constructs
 * structures so that it can be added to the proxy at build time
 * and hence save some reflection at runtime
 * 
 * @author jwells
 *
 */
public class Model implements Serializable {
    private static final long serialVersionUID = 752816761552710497L;
    
    /** For thread safety on the computed fields */
    private final static Object lock = new Object();

    /** The interface from which the JAXB proxy was created, fully qualified */
    private String originalInterface;
    
    /** Calculated at runtime lazily this is the original interface as a class */
    private volatile Class<?> originalInterfaceAsClass;
    
    /** The JAXB proxy of the originalInterface, fully qualified */
    private String translatedClass;
    
    /** Calculated at runtime lazily this is the proxy as a class */
    private volatile Class<?> translatedClassAsClass;
    
    /** If this node can be a root, the xml tag of the root of the document */
    private String rootName;
    
    /** A map from the xml tag to the parented child node */
    private final Map<String, ParentedModel> childrenByName = new HashMap<String, ParentedModel>();
    
    /** A map from xml tag to information about the non-child property */
    private final Map<String, ChildDataModel> nonChildProperty = new HashMap<String, ChildDataModel>();
    
    /** If this node has a key, this is the property name of the key */
    private String keyProperty;
    
    /**
     * These are calculated values and only filled in when asked for
     */
    private Set<String> unKeyedChildren = null;
    private Set<String> keyedChildren = null;
    private JAUtilities jaUtilities = null;
    private ClassLoader myLoader;
    
    public Model() {
    }
    
    public Model(String originalInterface,
        String translatedClass) {
        this.originalInterface = originalInterface;
        this.translatedClass = translatedClass;
    }
    
    public void setRootName(String rootName) {
        this.rootName = rootName;
    }
    
    public void setKeyProperty(String keyProperty) {
        this.keyProperty = keyProperty;
    }
    
    public void addChild(
            String childInterface,
            String xmlTag,
            ChildType childType,
            String givenDefault,
            Map<String, String> childDefault) {
        ParentedModel pm = new ParentedModel(childInterface, xmlTag, childType, givenDefault, childDefault);
        childrenByName.put(xmlTag, pm);
    }
    
    public void addNonChild(String xmlTag, String defaultValue, String childType) {
        nonChildProperty.put(xmlTag, new ChildDataModel(childType, defaultValue));
    }

    /**
     * @return the originalInterface
     */
    public String getOriginalInterface() {
        return originalInterface;
    }

    /**
     * @return the translatedClass
     */
    public String getTranslatedClass() {
        return translatedClass;
    }

    /**
     * @return the rootName
     */
    public String getRootName() {
        return rootName;
    }

    /**
     * @return the keyProperty
     */
    public String getKeyProperty() {
        return keyProperty;
    }
    
    public Map<String, ParentedModel> getChildrenByName() {
        return childrenByName;
    }
    
    public Map<String, ChildDataModel> getNonChildProperties() {
        return nonChildProperty;
    }
    
    public Set<String> getUnKeyedChildren() {
        synchronized (lock) {
            if (unKeyedChildren != null) return unKeyedChildren;
            
            unKeyedChildren = new HashSet<String>();
            
            for (Map.Entry<String, ParentedModel> entry : childrenByName.entrySet()) {
                if (entry.getValue().getChildModel().getKeyProperty() != null) continue;
                unKeyedChildren.add(entry.getKey());
            }
            
            return unKeyedChildren;
        }
    }
    
    public Set<String> getKeyedChildren() {
        synchronized (lock) {
            if (keyedChildren != null) return keyedChildren;
            
            keyedChildren = new HashSet<String>();
            
            for (Map.Entry<String, ParentedModel> entry : childrenByName.entrySet()) {
                if (entry.getValue().getChildModel().getKeyProperty() == null) continue;
                keyedChildren.add(entry.getKey());
            }
            
            return keyedChildren;
        }
    }
    
    public void setJAUtilities(JAUtilities jaUtilities, ClassLoader myLoader) {
        synchronized (lock) {
            if (this.jaUtilities != null) return;
            this.jaUtilities = jaUtilities;
            this.myLoader = myLoader;
            
            for (ParentedModel pm : childrenByName.values()) {
                pm.setRuntimeInformation(jaUtilities, myLoader);
            }
            
            for (ChildDataModel cdm : nonChildProperty.values()) {
                cdm.setLoader(myLoader);
            }
        }
    }
    
    public String getDefaultChildValue(String propName) {
        synchronized (lock) {
            ChildDataModel cd = nonChildProperty.get(propName);
            if (cd == null) return null;
            return cd.getDefaultAsString();
        }
    }
    
    public boolean isChildProperty(String propName) {
        synchronized (lock) {
            if (nonChildProperty.containsKey(propName)) return false;
            if (childrenByName.containsKey(propName)) return true;
            
            throw new AssertionError("Unknwn property " + propName + " for " + this);
        }
    }
    
    public Class<?> getNonChildType(String propName) {
        synchronized (lock) {
            ChildDataModel cd = nonChildProperty.get(propName);
            if (cd == null) return null;
            
            return cd.getChildTypeAsClass();
        }
    }
    
    public ParentedModel getChild(String propName) {
        synchronized (lock) {
            return childrenByName.get(propName);
        }
    }
    
    public Class<?> getOriginalInterfaceAsClass() {
        if (originalInterfaceAsClass != null) return originalInterfaceAsClass;
        
        synchronized (lock) {
            if (originalInterfaceAsClass != null) return originalInterfaceAsClass;
            
            originalInterfaceAsClass = GeneralUtilities.loadClass(myLoader, originalInterface);
            return originalInterfaceAsClass;
            
        }
    }
    
    public Class<?> getProxyAsClass() {
        if (translatedClassAsClass != null) return translatedClassAsClass;
        
        synchronized (lock) {
            if (translatedClassAsClass != null) return translatedClassAsClass;
            
            translatedClassAsClass = GeneralUtilities.loadClass(myLoader, translatedClass);
            return translatedClassAsClass;
            
        }
    }
    
    public Collection<ParentedModel> getAllChildren() {
        synchronized (lock) {
            return Collections.unmodifiableCollection(childrenByName.values());
        }
    }
    
    public Map<String, ParentedModel> getChildrenProperties() {
        synchronized (lock) {
            return Collections.unmodifiableMap(childrenByName);
        }
    }
    
    @Override
    public int hashCode() {
        return translatedClass.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Model)) return false;
        
        return translatedClass.equals(((Model) o).getTranslatedClass());
    }
    
    @Override
    public String toString() {
        return "Model(" + originalInterface + "," + translatedClass + "," + rootName + "," + keyProperty + ")";
    }
    
}
