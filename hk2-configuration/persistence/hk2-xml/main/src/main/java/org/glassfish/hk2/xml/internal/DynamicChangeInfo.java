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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.configuration.hub.api.Hub;
import org.glassfish.hk2.configuration.hub.api.WriteableBeanDatabase;
import org.glassfish.hk2.utilities.general.ValidatorUtilities;
import org.glassfish.hk2.xml.api.XmlHubCommitMessage;
import org.glassfish.hk2.xml.jaxb.internal.BaseHK2JAXBBean;

/**
 * @author jwells
 *
 */
public class DynamicChangeInfo<T> {
    private final JAUtilities jaUtilities;
    private final ReentrantReadWriteLock treeLock = new ReentrantReadWriteLock();
    private final WriteLock writeTreeLock = treeLock.writeLock();
    private final ReadLock readTreeLock = treeLock.readLock();
    private long changeNumber = 0;
    private final Hub hub;
    private final XmlServiceImpl idGenerator;
    private final boolean advertiseInLocator;
    private final boolean advertiseInHub;
    private final DynamicConfigurationService dynamicService;
    private final ServiceLocator locator;
    private final LinkedHashSet<VetoableChangeListener> listeners = new LinkedHashSet<VetoableChangeListener>();
    private final LinkedHashSet<BaseHK2JAXBBean> participants = new LinkedHashSet<BaseHK2JAXBBean>();
    private XmlRootHandleImpl<T> root;
    
    private XmlDynamicChange dynamicChange = null;
    private int changeDepth = 0;
    private boolean globalSuccess = true;
    
    private Validator validator;
    
    /* package */ DynamicChangeInfo(JAUtilities jaUtilities,
            Hub hub,
            boolean advertiseInHub,
            XmlServiceImpl idGenerator,
            DynamicConfigurationService dynamicService,
            boolean advertiseInLocator,
            ServiceLocator locator) {
        this.jaUtilities = jaUtilities;
        this.hub = hub;
        this.advertiseInHub = advertiseInHub;
        this.idGenerator = idGenerator;
        this.advertiseInLocator = advertiseInLocator;
        this.dynamicService = dynamicService;
        this.locator = locator;
    }
    
    /* package */ void setRoot(XmlRootHandleImpl<T> root) {
        this.root = root;
    }
    
    public ReadLock getReadLock() {
        return readTreeLock;
    }
    
    public WriteLock getWriteLock() {
        return writeTreeLock;
    }
    
    public long getChangeNumber() {
        readTreeLock.lock();
        try {
            return changeNumber;
        }
        finally {
            readTreeLock.unlock();
        }
    }
    
    public void incrementChangeNumber() {
        writeTreeLock.lock();
        try {
            changeNumber++;
        }
        finally {
            writeTreeLock.unlock();
        }
    }
    
    public JAUtilities getJAUtilities() {
        return jaUtilities;
    }
    
    public String getGeneratedId() {
        return jaUtilities.getUniqueId();
    }
    
    public XmlServiceImpl getIdGenerator() {
        return idGenerator;
    }
    
    /**
     * Write lock MUST be held!
     * 
     * @return
     */
    public XmlDynamicChange startOrContinueChange(BaseHK2JAXBBean participant) {
        changeDepth++;
        
        if (participant != null) {
            participants.add(participant);
        }
        
        if (dynamicChange != null) return dynamicChange;
        globalSuccess = true;
        
        DynamicConfiguration change = null;
        DynamicConfiguration systemChange = null;
        if (dynamicService != null) {
            systemChange = dynamicService.createDynamicConfiguration();
            if (advertiseInLocator) {
                change = systemChange;
            }
        }
        
        WriteableBeanDatabase wbd = null;
        if (hub != null && advertiseInHub) {
            wbd = hub.getWriteableDatabaseCopy();
            
            wbd.setCommitMessage(new XmlHubCommitMessage() {});
            
            if (systemChange != null) {
                systemChange.registerTwoPhaseResources(wbd.getTwoPhaseResource());
                
                // Now the hub transaction is tied to the registry transaction
            }
        }
        
        dynamicChange = new XmlDynamicChange(wbd, change, systemChange);
        return dynamicChange;
    }
    
    
    
    /**
     * Write lock MUST be held!
     * 
     * @return
     */
    public void endOrDeferChange(boolean success) throws MultiException {
        changeDepth--;
        if (changeDepth < 0) changeDepth = 0;
        
        if (!success) globalSuccess = false;
        
        if (changeDepth > 0) return;
        
        List<BaseHK2JAXBBean> localParticipants = new ArrayList<BaseHK2JAXBBean>(participants);
        participants.clear();
        
        XmlDynamicChange localDynamicChange = dynamicChange;
        dynamicChange = null;
        
        if (localDynamicChange == null) return;
        
        ConstraintViolationException validationException = null;
        if (globalSuccess && (validator != null) && (root != null) && (root.getRoot() != null)) {
            // Validate root if validation is on
            Set<ConstraintViolation<Object>> violations = validator.<Object>validate(root.getRoot());
            if (violations != null && !violations.isEmpty()) {
                validationException = new ConstraintViolationException(violations);
            }
        }
        
        if (!globalSuccess || (validationException != null)) {
            for (BaseHK2JAXBBean participant : localParticipants) {
                participant.__rollbackChange();
            }
            
            if (validationException != null) {
                throw validationException;
            }
            return;
        }
        
        for (BaseHK2JAXBBean participant : localParticipants) {
            participant.__activateChange();
        }
        
        DynamicConfiguration systemChange = localDynamicChange.getSystemDynamicConfiguration();
        WriteableBeanDatabase wbd = localDynamicChange.getBeanDatabase();
        
        if (systemChange == null && wbd != null) {
            // Weird case, can it happen?
            wbd.commit(new XmlHubCommitMessage() {});
            return;
        }
        
        if (systemChange == null) return;
        
        // Can definitely throw, for example if a listener fails
        systemChange.commit();
    }
    
    public ServiceLocator getServiceLocator() {
        return locator;
    }
    
    public void addChangeListener(VetoableChangeListener... allAdds) {
        if (allAdds == null) return;
        
        writeTreeLock.lock();
        try {
            for (VetoableChangeListener add : allAdds) {
                listeners.add(add);
            }
        }
        finally {
            writeTreeLock.unlock();
        }
    }

    
    public void removeChangeListener(VetoableChangeListener... allRemoves) {
        if (allRemoves == null) return;
        
        writeTreeLock.lock();
        try {
            for (VetoableChangeListener remove : allRemoves) {
                listeners.remove(remove);
            }
        }
        finally {
            writeTreeLock.unlock();
        }
    }

    public List<VetoableChangeListener> getChangeListeners() {
        readTreeLock.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<VetoableChangeListener>(listeners));
        }
        finally {
            readTreeLock.unlock();
        }
    }
    
    public Validator findOrCreateValidator() {
        writeTreeLock.lock();
        try {
            if (validator != null) return validator;
            
            validator = ValidatorUtilities.getValidator();
            return validator;
        }
        finally {
            writeTreeLock.unlock();
        }
    }
    
    public void deleteValidator() {
        writeTreeLock.lock();
        try {
            validator = null;
        }
        finally {
            writeTreeLock.unlock();
        }
    }
    
    public Validator findValidator() {
        readTreeLock.lock();
        try {
            return validator;
        }
        finally {
            readTreeLock.unlock();
        }
    }
    
    @Override
    public String toString() {
        return "DynamicChangeInfo(inLocator=" + advertiseInLocator + ",inHub=" + advertiseInHub + "," + System.identityHashCode(this) + ")";
    }
}
