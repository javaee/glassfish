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

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.hk2.xml.api.XmlRootCopy;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
public class XmlRootHandleImpl<T> implements XmlRootHandle<T> {
    private final T root;
    private final Class<T> rootClass;
    private URI rootURI;
    private final boolean advertised;
    private final boolean advertisedInHub;
    private final DynamicChangeInfo changeControl;
    
    /* package */ XmlRootHandleImpl(T root, Class<T> rootClass, URI rootURI, boolean advertised, boolean hub, DynamicChangeInfo changes) {
        this.root = root;
        this.rootClass = rootClass;
        this.rootURI = rootURI;
        this.advertised = advertised;
        this.advertisedInHub = hub;
        this.changeControl = changes;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getRoot()
     */
    @Override
    public T getRoot() {
        return root;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getRootClass()
     */
    @Override
    public Class<T> getRootClass() {
        return rootClass;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getURI()
     */
    @Override
    public URI getURI() {
        return rootURI;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#isAdvertisedInLocator()
     */
    @Override
    public boolean isAdvertisedInLocator() {
        return advertised;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#isAdvertisedInHub()
     */
    @Override
    public boolean isAdvertisedInHub() {
        return advertisedInHub;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#overlay(org.glassfish.hk2.xml.api.XmlRootHandle)
     */
    @Override
    public void overlay(XmlRootHandle<T> newRoot) {
        throw new AssertionError("overlay not yet implemented");
        
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getXmlRootCopy()
     */
    @SuppressWarnings("unchecked")
    @Override
    public XmlRootCopy<T> getXmlRootCopy() {
        changeControl.getReadLock().lock();
        try {
            BaseHK2JAXBBean bean = (BaseHK2JAXBBean) root;
            if (bean == null) {
                return new XmlRootCopyImpl<T>(this, changeControl.getChangeNumber(), null);
            }
        
            BaseHK2JAXBBean copy;
            try {
                copy = doCopy(bean);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Throwable th) {
                throw new RuntimeException(th);
            }
        
            return new XmlRootCopyImpl<T>(this, changeControl.getChangeNumber(), (T) copy);
        }
        finally {
            changeControl.getReadLock().unlock();
        }
    }
    
    private static BaseHK2JAXBBean doCopy(BaseHK2JAXBBean copyMe) throws Throwable {
        if (copyMe == null) return null;
        
        Class<?> copyMeClass = copyMe.getClass();
        Constructor<?> noArgsConstructor = copyMeClass.getConstructor();
        
        BaseHK2JAXBBean retVal = (BaseHK2JAXBBean) ReflectionHelper.makeMe(noArgsConstructor, new Object[0], false);
        retVal._shallowCopyFrom(copyMe);
        
        Set<String> childrenProps = copyMe._getChildrenXmlTags();
        for (String childProp : childrenProps) {
            Object child = copyMe._getProperty(childProp);
            if (child == null) continue;
            
            if (child instanceof List) {
                List<?> childList = (List<?>) child;
                
                ArrayList<Object> toSetChildList = new ArrayList<Object>(childList.size());
                
                for (Object subChild : childList) {
                    BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) subChild);
                    copiedChild._setParent(retVal);
                    
                    toSetChildList.add(copiedChild);
                    
                    String keyValue = copiedChild._getKeyValue();
                    if (keyValue != null) {
                        retVal._addChild(childProp, keyValue, copiedChild);
                    }
                    else {
                        // May be many of them with same name, no key...
                        retVal._addUnkeyedChild(childProp);
                    }
                }
                
                // Sets the list property into the parent
                retVal._setProperty(childProp, toSetChildList);
            }
            else {
                // A direct child
                BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) child);
                copiedChild._setParent(retVal);
                
                retVal._setProperty(childProp, copiedChild);
                
                retVal._addUnkeyedChild(childProp);
            }
        }
        
        return retVal;
    }
    
    /* package */ long getRevision() {
        return changeControl.getChangeNumber();
    }
    
    @Override
    public String toString() {
        return "XmlRootHandleImpl(" + root + "," + rootClass.getName() + "," + rootURI + "," + System.identityHashCode(this) + ")";
    }
}
