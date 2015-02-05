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
package org.glassfish.hk2.extras.hk2bridge.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationListener;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * @author jwells
 *
 */
@Singleton
@Visibility(DescriptorVisibility.LOCAL)
public class Hk2BridgeImpl implements DynamicConfigurationListener {
    private final ServiceLocator local;
    private ServiceLocator remote;
    private Filter filter;
    
    private List<ActiveDescriptor<?>> mirroredDescriptors = new ArrayList<ActiveDescriptor<?>>();
    
    @Inject
    private Hk2BridgeImpl(ServiceLocator local) {
        this.local = local;
        
    }
    
    public synchronized void setRemote(ServiceLocator remote) {
        this.remote = remote;
        this.filter = new NoLocalNoRemoteFilter(remote.getLocatorId());
        
        List<ActiveDescriptor<?>> newDescriptors = local.getDescriptors(filter);
        
        handleChange(newDescriptors);
    }
    
    @SuppressWarnings("unchecked")
    private synchronized void handleChange(List<ActiveDescriptor<?>> newDescriptors) {
        if (remote == null) return;
        
        HashSet<ActiveDescriptor<?>> toRemove = new HashSet<ActiveDescriptor<?>>(mirroredDescriptors);
        toRemove.removeAll(newDescriptors);
        
        HashSet<ActiveDescriptor<?>> toAdd = new HashSet<ActiveDescriptor<?>>(newDescriptors);
        toAdd.removeAll(mirroredDescriptors);
        
        DynamicConfigurationService remoteDCS = remote.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = remoteDCS.createDynamicConfiguration();
        
        boolean dirty = false;
        for (ActiveDescriptor<?> removeMe : toRemove) {
            Filter removeFilter = new RemoteFilter(removeMe.getLocatorId(), removeMe.getServiceId());
            config.addUnbindFilter(removeFilter);
            dirty = true;
        }
        
        for (ActiveDescriptor<?> addMe : toAdd) {
            CrossOverDescriptor<Object> cod = new CrossOverDescriptor<Object>(local, (ActiveDescriptor<Object>) addMe);
            config.addActiveDescriptor(cod);
            dirty = true;
        }
        
        if (dirty) {
            config.commit();
        }
        
        mirroredDescriptors = newDescriptors;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.DynamicConfigurationListener#configurationChanged()
     */
    @Override
    public void configurationChanged() {
        List<ActiveDescriptor<?>> newDescriptors = local.getDescriptors(filter);
        
        handleChange(newDescriptors);
    }
    
    private static class NoLocalNoRemoteFilter implements Filter {
        private final long remoteLocatorId;
        
        private NoLocalNoRemoteFilter(long remoteId) {
            remoteLocatorId = remoteId;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.Filter#matches(org.glassfish.hk2.api.Descriptor)
         */
        @Override
        public boolean matches(Descriptor d) {
            if (DescriptorVisibility.LOCAL.equals(d.getDescriptorVisibility())) {
                return false;
            }
            
            Set<Long> previousVisits = getMetadataLongs(d, ExtrasUtilities.HK2BRIDGE_LOCATOR_ID);
            
            if (previousVisits.contains(new Long(remoteLocatorId))) {
                // cycle!
                return false;
            }
            
            return true;
        }
    }
    
    private static class RemoteFilter implements Filter {
        private final long localLocatorId;
        private final long localServiceId;
        
        private RemoteFilter(long localLocatorId, long localServiceId) {
            this.localLocatorId = localLocatorId;
            this.localServiceId = localServiceId;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.api.Filter#matches(org.glassfish.hk2.api.Descriptor)
         */
        @Override
        public boolean matches(Descriptor d) {
            String remoteLocatorVal = ServiceLocatorUtilities.getOneMetadataField(d, ExtrasUtilities.HK2BRIDGE_LOCATOR_ID);
            if (remoteLocatorVal == null) return false;
            
            long remoteLocatorId = getOneMetadataFieldAsLong(d, ExtrasUtilities.HK2BRIDGE_LOCATOR_ID);
            if (remoteLocatorId == -1) return false;
            
            if (localLocatorId != remoteLocatorId) return false;
            
            long remoteServiceId = getOneMetadataFieldAsLong(d, ExtrasUtilities.HK2BRIDGE_SERVICE_ID);
            if (remoteServiceId == -1) return false;
            
            return (remoteServiceId == localServiceId);
        }
    }
    
    /**
     * Must be used for positive values, a failure to parse returns -1
     * 
     * @param d The descriptor to get the value from
     * @param field The metadata field to get the value from
     * @return The zero or positive number, -1 if field not there or not parseable
     */
    private static long getOneMetadataFieldAsLong(Descriptor d, String field) {
        String val = ServiceLocatorUtilities.getOneMetadataField(d, field);
        if (val == null) return -1;
        
        try {
            return Long.parseLong(val);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }
    
    /**
     * Gets all of the longs encoded into this descriptors metadata
     * field
     * 
     * @param d
     * @param field
     * @return
     */
    private static Set<Long> getMetadataLongs(Descriptor d, String field) {
        Set<Long> retVal = new HashSet<Long>();
        
        List<String> metadataValues = d.getMetadata().get(field);
        if (metadataValues == null) return retVal;
        
        for (String metadataValue : metadataValues) {
            try {
                Long val = new Long(metadataValue);
                retVal.add(val);
            }
            catch (NumberFormatException nfe) {
                // Do nothing, just skip it
            }
        }
        
        return retVal;
    }

    
}
