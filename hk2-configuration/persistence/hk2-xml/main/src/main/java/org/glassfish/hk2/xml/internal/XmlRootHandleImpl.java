/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014-2016 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.xml.api.XmlHandleTransaction;
import org.glassfish.hk2.xml.api.XmlHubCommitMessage;
import org.glassfish.hk2.xml.api.XmlRootCopy;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;
import org.glassfish.hk2.xml.spi.XmlServiceParser;

/**
 * @author jwells
 *
 */
public class XmlRootHandleImpl<T> implements XmlRootHandle<T> {
    private final XmlServiceImpl parent;
    private final Hub hub;
    private T root;
    private final ModelImpl rootNode;
    private URI rootURI;
    private final boolean advertised;
    private final boolean advertisedInHub;
    private final DynamicChangeInfo<T> changeControl;
    
    /* package */ XmlRootHandleImpl(
            XmlServiceImpl parent,
            Hub hub,
            T root,
            ModelImpl rootNode,
            URI rootURI,
            boolean advertised,
            boolean inHub,
            DynamicChangeInfo<T> changes) {
        this.parent = parent;
        this.hub = hub;
        this.root = root;
        this.rootNode = rootNode;
        this.rootURI = rootURI;
        this.advertised = advertised;
        this.advertisedInHub = inHub;
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
    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getRootClass() {
        return (Class<T>) rootNode.getOriginalInterfaceAsClass();
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
        if (!(newRoot instanceof XmlRootHandle)) {
            throw new IllegalArgumentException("newRoot must have been created by the same XmlService as this one");
        }
        XmlRootHandleImpl<T> newRootImpl = (XmlRootHandleImpl<T>) newRoot;
        if (newRootImpl.isAdvertisedInHub()) {
            throw new IllegalArgumentException("The newRoot must not be advertised in the Hub");
        }
        if (newRootImpl.isAdvertisedInLocator()) {
            throw new IllegalArgumentException("The newRoot must not be advertised as hk2 services");
        }
        if (root == null) {
            throw new IllegalArgumentException("This XmlRootHandle must have a root to be overlayed");
        }
        T newRootRoot = newRootImpl.getRoot(); 
        if (newRootRoot == null) {
            throw new IllegalArgumentException("The newRoot must have a root to overlay onto this root");
        }
        if (!(newRootRoot instanceof BaseHK2JAXBBean)) {
            throw new IllegalArgumentException("The newRoot has a root of an unknown type: " + newRootRoot.getClass().getName());
        }
        
        BaseHK2JAXBBean newRootBase = (BaseHK2JAXBBean) newRootRoot;
        
        throw new AssertionError("overlay not yet implemented");
        
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getXmlRootCopy()
     */
    @SuppressWarnings("unchecked")
    @Override
    public XmlRootCopy<T> getXmlRootCopy() {
        // In any case, the child should not be directly given the hub, as
        // it is not reflected in the hub
        DynamicChangeInfo<T> copyController =
                new DynamicChangeInfo<T>(changeControl.getJAUtilities(),
                        null,
                        false,
                        changeControl.getIdGenerator(),
                        null,
                        false,
                        changeControl.getServiceLocator());
        
        changeControl.getReadLock().lock();
        try {
            BaseHK2JAXBBean bean = (BaseHK2JAXBBean) root;
            if (bean == null) {
                return new XmlRootCopyImpl<T>(this, changeControl.getChangeNumber(), null);
            }
        
            BaseHK2JAXBBean copy;
            try {
                Map<ReferenceKey, BaseHK2JAXBBean> referenceMap = new HashMap<ReferenceKey, BaseHK2JAXBBean>();
                List<UnresolvedReference> unresolved = new LinkedList<UnresolvedReference>();
                
                copy = doCopy(bean, copyController, null, referenceMap, unresolved);
                
                Utilities.fillInUnfinishedReferences(referenceMap, unresolved);
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
    
    private BaseHK2JAXBBean doCopy(BaseHK2JAXBBean copyMe,
            DynamicChangeInfo<T> copyController,
            BaseHK2JAXBBean theCopiedParent,
            Map<ReferenceKey, BaseHK2JAXBBean> referenceMap,
            List<UnresolvedReference> unresolved) throws Throwable {
        if (copyMe == null) return null;
        
        BaseHK2JAXBBean retVal = Utilities.createBean(copyMe.getClass());
        retVal._shallowCopyFrom(copyMe);
        
        ModelImpl myModel = retVal._getModel();
        
        Set<String> childrenProps = copyMe._getChildrenXmlTags();
        for (String childProp : childrenProps) {
            Object child = copyMe._getProperty(childProp);
            if (child == null) continue;
            
            if (child instanceof List) {
                List<?> childList = (List<?>) child;
                
                ArrayList<Object> toSetChildList = new ArrayList<Object>(childList.size());
                
                for (Object subChild : childList) {
                    BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) subChild, copyController, retVal, referenceMap, unresolved);
                    
                    toSetChildList.add(copiedChild);
                }
                
                // Sets the list property into the parent
                retVal._setProperty(childProp, toSetChildList);
            }
            else if (child.getClass().isArray()) {
                int length = Array.getLength(child);
                
                
                ParentedModel pm = myModel.getChild(childProp);
                ModelImpl childModel = pm.getChildModel();
                
                Class<?> childInterface = childModel.getOriginalInterfaceAsClass();
                
                Object toSetChildArray = Array.newInstance(childInterface, length);
                
                for (int lcv = 0; lcv < length; lcv++) {
                    Object subChild = Array.get(child, lcv);
                    
                    BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) subChild, copyController, retVal, referenceMap, unresolved);
                    
                    Array.set(toSetChildArray, lcv, copiedChild);
                }
                
                // Sets the array property into the parent
                retVal._setProperty(childProp, toSetChildArray);
            }
            else {
                // A direct child
                BaseHK2JAXBBean copiedChild = doCopy((BaseHK2JAXBBean) child, copyController, retVal, referenceMap, unresolved);
                
                retVal._setProperty(childProp, copiedChild);
            }
        }
        
        if (theCopiedParent != null) {
            retVal._setParent(theCopiedParent);
        }
        
        String keyPropertyName = retVal._getKeyPropertyName();
        if (keyPropertyName != null) {
            String keyProperty = retVal._getKeyValue();
            if (keyProperty != null) {
                referenceMap.put(new ReferenceKey(myModel.getOriginalInterface(), keyProperty), retVal);
            }
            
            // Now try to resolve any references, and if we can not add them to the unfinished list
            Map<String, ChildDataModel> nonChildrenProps = myModel.getNonChildProperties();
            for (Map.Entry<String, ChildDataModel> nonChild : nonChildrenProps.entrySet()) {
                String xmlTag = nonChild.getKey();
                ChildDataModel cdm = nonChild.getValue();
                
                if (!cdm.isReference()) continue;
                
                Object fromReferenceRaw = copyMe._getProperty(xmlTag);
                if (fromReferenceRaw == null) continue;
                if (!(fromReferenceRaw instanceof BaseHK2JAXBBean)) continue;
                BaseHK2JAXBBean fromReference = (BaseHK2JAXBBean) fromReferenceRaw;
                
                String fromKeyValue = fromReference._getKeyValue();
                
                ReferenceKey rk = new ReferenceKey(cdm.getChildType(), fromKeyValue);
                
                BaseHK2JAXBBean toReference = referenceMap.get(rk);
                if (toReference != null) {
                    retVal._setProperty(xmlTag, toReference);
                }
                else {
                    // Must go in unfinished list
                    unresolved.add(new UnresolvedReference(cdm.getChildType(), fromKeyValue, xmlTag, retVal));
                }
            }
        }
        
        retVal._setDynamicChangeInfo(this, copyController, false);
        
        return retVal;
    }
    
    /* package */ long getRevision() {
        return changeControl.getChangeNumber();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#addRoot(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addRoot(T newRoot) {
        changeControl.getWriteLock().lock();
        try {
            if (root != null) {
                throw new IllegalStateException("An attempt was made to add a root to a handle that already has a root " + this);
            }
            if (!(newRoot instanceof BaseHK2JAXBBean)) {
                throw new IllegalArgumentException("The added bean must be from XmlService.createBean");
            }
            
            WriteableBeanDatabase wbd = null;
            if (advertisedInHub) {
                wbd = hub.getWriteableDatabaseCopy();
            }
            
            DynamicConfiguration config = null;
            if (advertised) {
                config = parent.getDynamicConfigurationService().createDynamicConfiguration();
            }
            
            List<ActiveDescriptor<?>> addedServices = new LinkedList<ActiveDescriptor<?>>();
            BaseHK2JAXBBean copiedRoot = Utilities._addRoot(rootNode,
                    newRoot,
                    changeControl,
                    parent.getClassReflectionHelper(),
                    wbd,
                    config,
                    addedServices,
                    this);
            
            if (config != null) {
                config.commit();
            }
            
            if (wbd != null) {
                wbd.commit(new XmlHubCommitMessage() {});
            }
            
            root = (T) copiedRoot;
            
            ServiceLocator locator = parent.getServiceLocator();
            for (ActiveDescriptor<?> added : addedServices) {
                // Ensures that the defaulters will run right away
                locator.getServiceHandle(added).getService();
            }
        }
        finally {
            changeControl.getWriteLock().unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#createAndAddRoot()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addRoot() {
        addRoot(parent.createBean((Class<T>) rootNode.getOriginalInterfaceAsClass()));
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#deleteRoot()
     */
    @Override
    public T removeRoot() {
        throw new AssertionError("removeRoot not implemented");
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getReadOnlyRoot(boolean)
     */
    @Override
    public T getReadOnlyRoot(boolean representDefaults) {
        throw new AssertionError("getReadOnlyRoot not implemented");
    }
    
    /* package */ DynamicChangeInfo<T> getChangeInfo() {
        return changeControl;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#addChangeListener(java.beans.VetoableChangeListener)
     */
    @Override
    public void addChangeListener(VetoableChangeListener... listeners) {
        changeControl.addChangeListener(listeners);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#removeChangeListener(java.beans.VetoableChangeListener)
     */
    @Override
    public void removeChangeListener(VetoableChangeListener... listeners) {
        changeControl.removeChangeListener(listeners);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#getChangeListeners()
     */
    @Override
    public List<VetoableChangeListener> getChangeListeners() {
        return changeControl.getChangeListeners();
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#lockForTransaction()
     */
    @Override
    public XmlHandleTransaction<T> lockForTransaction()
            throws IllegalStateException {
        if (changeControl == null) throw new IllegalStateException();
        
        return new XmlHandleTransactionImpl<T>(this, changeControl);
    }
    
    @Override
    public void startValidating() {
        if (changeControl == null) throw new IllegalStateException();
        
        Validator validator = changeControl.findOrCreateValidator();
        
        if (root == null) return;
        
        Set<ConstraintViolation<Object>> violations = validator.<Object>validate(root);
        if (violations == null || violations.isEmpty()) return;
        
        throw new ConstraintViolationException(violations);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#stopValidating()
     */
    @Override
    public void stopValidating() {
        if (changeControl == null) throw new IllegalStateException();
        changeControl.deleteValidator();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#isValidating()
     */
    @Override
    public boolean isValidating() {
        if (changeControl == null) throw new IllegalStateException();
        
        return (changeControl.findValidator() != null);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.xml.api.XmlRootHandle#marshall(java.io.OutputStream)
     */
    @Override
    public void marshal(OutputStream outputStream) throws IOException {
        if (changeControl == null) {
            throw new IllegalStateException("marshall May only be called on a fully initialized root handle " + this);
        }
        
        changeControl.getWriteLock().lock();
        try {
            XmlServiceParser parser = parent.getParser();
            if (parser == null) {
                XmlStreamImpl.marshall(outputStream, this);
                return;
            }
        
            parser.marshall(outputStream, this);
        }
        finally {
            changeControl.getWriteLock().unlock();
        }
    }
    
    @Override
    public String toString() {
        return "XmlRootHandleImpl(" + root + "," + rootNode + "," + rootURI + "," + System.identityHashCode(this) + ")";
    }

    
}
