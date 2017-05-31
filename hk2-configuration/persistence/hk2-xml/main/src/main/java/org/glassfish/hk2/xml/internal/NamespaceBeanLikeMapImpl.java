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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap;

/**
 * @author jwells
 *
 */
public class NamespaceBeanLikeMapImpl implements NamespaceBeanLikeMap, Serializable {
    private static final long serialVersionUID = 7351909351649012181L;

    private Map<String, Map<String, Object>> namespaceMap =
            new LinkedHashMap<String, Map<String, Object>>();
    
    private Map<String, Map<String, Object>> backupMap;
    
    public NamespaceBeanLikeMapImpl() {
        namespaceMap.put(XmlService.DEFAULT_NAMESPACE, new LinkedHashMap<String, Object>());
    }
    
    
    
    private static Map<String, Map<String, Object>> deepCopyNamespaceBeanLikeMaps(Map<String, Map<String, Object>> copyMe) {
        Map<String, Map<String, Object>> retVal = new LinkedHashMap<String, Map<String, Object>>();
        if (copyMe == null) return retVal;
        
        for (Map.Entry<String, Map<String,Object>> entry : copyMe.entrySet()) {
            String namespace = entry.getKey();
            Map<String, Object> blm = entry.getValue();
            
            retVal.put(namespace, new LinkedHashMap<String, Object>(blm));
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#getValue(java.lang.String, java.lang.String)
     */
    @Override
    public Object getValue(String namespace, String key) {
        namespace = QNameUtilities.fixNamespace(namespace);
        
        Map<String, Object> nMap = namespaceMap.get(namespace);
        if (nMap == null) return null;
        
        return nMap.get(key);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#setValue(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void setValue(String namespace, String key, Object value) {
        namespace = QNameUtilities.fixNamespace(namespace);
        
        Map<String, Object> narrowedMap = namespaceMap.get(namespace);
        if (narrowedMap == null) {
            narrowedMap = new LinkedHashMap<String, Object>();
            namespaceMap.put(namespace, narrowedMap);
        }
        
        narrowedMap.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#isSet(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isSet(String namespace, String key) {
        namespace = QNameUtilities.fixNamespace(namespace);
        
        Map<String, Object> narrowedMap = namespaceMap.get(namespace);
        if (narrowedMap == null) return false;
        
        return narrowedMap.containsKey(key);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#backup()
     */
    @Override
    public void backup() {
        if (backupMap != null) return;
        
        backupMap = deepCopyNamespaceBeanLikeMaps(namespaceMap);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#restoreBackup(boolean)
     */
    @Override
    public void restoreBackup(boolean drop) {
        try {
            if (backupMap == null) {
                return;
            }
            
            if (drop) {
                return;
            }
            
            namespaceMap = backupMap;
        }
        finally {
            backupMap = null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#getBeanLikeMap(java.util.Map)
     */
    @Override
    public Map<String, Object> getBeanLikeMap(
            Map<String, String> namespaceToPrefixMap) {
        LinkedHashMap<String, Object> retVal = new LinkedHashMap<String, Object>();
        
        for (Map.Entry<String, Map<String, Object>> outerEntry : namespaceMap.entrySet()) {
            String namespace = outerEntry.getKey();
            Map<String, Object> blm = outerEntry.getValue();
            
            boolean addNamespace = !XmlService.DEFAULT_NAMESPACE.equals(namespace);
            String prefix = addNamespace ? namespaceToPrefixMap.get(namespace) : null ;
            
            if (addNamespace && prefix == null) {
                // could not find the namespace prefix to use
                continue;
            }
            
            for (Map.Entry<String, Object> innerEntry : blm.entrySet()) {
                String key;
                Object value = innerEntry.getValue();
            
                if (addNamespace) {
                    key = prefix + ":" + innerEntry.getKey();
                }
                else {
                    key = innerEntry.getKey();
                }
                
                retVal.put(key, value);
            }
        }
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#shallowCopy(org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap, org.glassfish.hk2.xml.internal.ModelImpl)
     */
    @Override
    public void shallowCopy(NamespaceBeanLikeMap copyFrom, ModelImpl copyModel, boolean copyReferences) {
        for (Map.Entry<String, Map<String, Object>> outerEntry : copyFrom.getNamespaceBeanLikeMap().entrySet()) {
            String copyNamespace = outerEntry.getKey();
            Map<String, Object> copyBeanLikeMap = outerEntry.getValue();
            
            for (Map.Entry<String, Object> entrySet : copyBeanLikeMap.entrySet()) {
                String xmlTag = entrySet.getKey();
                
                QName childQName = QNameUtilities.createQName(copyNamespace, xmlTag);
                
                if (copyModel.getKeyedChildren().contains(childQName) || copyModel.getUnKeyedChildren().contains(childQName)) {
                    continue;
                }
                
                ChildDataModel cdm = copyModel.getNonChildProperties().get(childQName);
                if (!copyReferences && cdm != null && cdm.isReference()) {
                    continue;
                }
                
                setValue(copyNamespace, xmlTag, entrySet.getValue());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#getNamespaceBeanLikeMap()
     */
    @Override
    public Map<String, Map<String, Object>> getNamespaceBeanLikeMap() {
        return namespaceMap;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.jaxb.internal.NamespaceBeanLikeMap#getQNameMap()
     */
    @Override
    public Map<QName, Object> getQNameMap() {
        Map<QName, Object> retVal = new LinkedHashMap<QName, Object>();
        
        for (Map.Entry<String, Map<String, Object>> outerEntry : namespaceMap.entrySet()) {
            String namespace = outerEntry.getKey();
            Map<String, Object> innerMap = outerEntry.getValue();
            
            for (Map.Entry<String, Object> innerEntry : innerMap.entrySet()) {
                QName key = QNameUtilities.createQName(namespace, innerEntry.getKey());
                Object value = innerEntry.getValue();
                
                retVal.put(key, value);
            }
            
        }
        
        return retVal;
    }
    
    @Override
    public String toString() {
        return "NamespaceBeanLikeMapImpl(" + System.identityHashCode(this) + ")";
    }



    

    
}
