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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;

import org.glassfish.hk2.utilities.reflection.ClassReflectionHelper;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * Used by implementations of XmlService, though it may not actually
 * be JAXB that is calling the methods
 * 
 * @author jwells
 *
 */
public class Hk2JAXBUnmarshallerListener extends Unmarshaller.Listener {
    private final JAUtilities jaUtilities;
    private final ClassReflectionHelper classReflectionHelper;

    /**
     * @param xmlServiceImpl
     */
    Hk2JAXBUnmarshallerListener(JAUtilities jaUtilities, ClassReflectionHelper classReflectionHelper) {
        this.jaUtilities = jaUtilities;
        this.classReflectionHelper = classReflectionHelper;
    }

    private final LinkedList<BaseHK2JAXBBean> allBeans = new LinkedList<BaseHK2JAXBBean>();
    
    private void setUserKey(BaseHK2JAXBBean bean, boolean listOrArray) {
        ModelImpl model = bean._getModel();
        
        String keyProperty = model.getKeyProperty();
        if (keyProperty == null && listOrArray) {
            bean._setKeyValue(jaUtilities.getUniqueId());
            
            return;
        }
        
        if (keyProperty == null) return;
        
        String key = (String) bean._getProperty(keyProperty);
        if (key == null) return;
        
        bean._setKeyValue(key);
    }
    
    @SuppressWarnings("unchecked")
    private void setSelfXmlTagInAllChildren(BaseHK2JAXBBean targetBean) {
        ModelImpl model = targetBean._getModel();
        
        for (Map.Entry<String, ChildDescriptor> childDescriptorEntry : model.getAllChildrenDescriptors().entrySet()) {
            ParentedModel parentedNode = childDescriptorEntry.getValue().getParentedModel();
            
            if (parentedNode != null) {
                String childXmlTag = parentedNode.getChildXmlTag();
                
                Object children;
                switch (parentedNode.getAliasType()) {
                case NORMAL:
                    children = targetBean._getProperty(childXmlTag);
                    break;
                case IS_ALIAS:
                    children = targetBean._getProperty(childXmlTag);
                    targetBean.__fixAlias(childXmlTag, parentedNode.getChildXmlAlias());
                    
                    break;
                case HAS_ALIASES:
                default:
                    children = null;
                }
                
                if (children == null) continue;
                
                String proxyName = Utilities.getProxyNameFromInterfaceName(parentedNode.getChildInterface());
                
                if (children instanceof List) {
                    for (Object child : (List<Object>) children) {
                        if (!child.getClass().getName().equals(proxyName)) {
                            continue;
                        }
                        
                        BaseHK2JAXBBean childBean = (BaseHK2JAXBBean) child;
                        
                        childBean._setSelfXmlTag(parentedNode.getChildXmlTag());
                        
                        setUserKey(childBean, true);
                    }
                    
                }
                else if (children.getClass().isArray()) {
                    for (Object child : (Object[]) children) {
                        BaseHK2JAXBBean childBean = (BaseHK2JAXBBean) child;
                        
                        childBean._setSelfXmlTag(parentedNode.getChildXmlTag());
                        
                        setUserKey(childBean, true);
                    }
                }
                else {
                    BaseHK2JAXBBean childBean = (BaseHK2JAXBBean) children;
                    
                    childBean._setSelfXmlTag(parentedNode.getChildXmlTag());
                    
                    setUserKey(childBean, false);
                }
                
            }
            else {
                String nonChildProp = childDescriptorEntry.getKey();
                ChildDataModel cdm = childDescriptorEntry.getValue().getChildDataModel();
                
                if (AliasType.IS_ALIAS.equals(cdm.getAliasType())) {
                    targetBean.__fixAlias(nonChildProp, cdm.getXmlAlias());
                }
            }
        }
    }
    
    @Override
    public void afterUnmarshal(Object target, Object parent) {
        if (!(target instanceof BaseHK2JAXBBean)) return;
        
        BaseHK2JAXBBean targetBean = (BaseHK2JAXBBean) target;
        BaseHK2JAXBBean parentBean = (BaseHK2JAXBBean) parent;
        ModelImpl targetNode = targetBean._getModel();
        
        allBeans.add(targetBean);
        
        if (parentBean == null) {
            targetBean._setSelfXmlTag(targetNode.getRootName());
        }
        setSelfXmlTagInAllChildren(targetBean);
    }
    
    @Override
    public void beforeUnmarshal(Object target, Object parent) {
        if (!(target instanceof BaseHK2JAXBBean)) return;
        
        BaseHK2JAXBBean targetBean = (BaseHK2JAXBBean) target;
        
        targetBean._setClassReflectionHelper(classReflectionHelper);
        targetBean._setParent(parent);
    }
    
    LinkedList<BaseHK2JAXBBean> getAllBeans() {
        return allBeans;
    }
    
}