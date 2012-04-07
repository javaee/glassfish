/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.utilities;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.internal.ReflectionHelper;

/**
 * The implementation of the descriptor itself, with the
 * bonus of being serializable, and having writeable fields
 * 
 * @author jwells
 */
public class DescriptorImpl implements Descriptor, Serializable {
	/**
	 * For Serialization
	 */
	private static final long serialVersionUID = 77937523212000548L;
	
	private Set<String> contracts = new HashSet<String>();
	private String implementation;
	private String name;
	private String scope = PerLookup.class.getName();
	private Map<String, List<String>> metadatas = new LinkedHashMap<String, List<String>>();
	private Set<String> qualifiers = new HashSet<String>();
	private DescriptorType descriptorType = DescriptorType.CLASS;
	private HK2Loader loader;
	private int rank;
	private Descriptor baseDescriptor;
	private Long id;
	private Long locatorId;
	
	/**
	 * For serialization
	 */
	public DescriptorImpl() {	
	}
	
	/**
	 * Does a deep copy of the incoming descriptor
	 * 
	 * @param copyMe The descriptor to copy
	 */
	public DescriptorImpl(Descriptor copyMe) {
	    name = copyMe.getName();
        scope = copyMe.getScope();
        implementation = copyMe.getImplementation();
        descriptorType = copyMe.getDescriptorType();
        loader = copyMe.getLoader();
        rank = copyMe.getRanking();
        id = copyMe.getServiceId();
        locatorId = copyMe.getLocatorId();
        baseDescriptor = copyMe.getBaseDescriptor();
        
	    if (copyMe.getAdvertisedContracts() != null) {
	        contracts.addAll(copyMe.getAdvertisedContracts());
	    }
		
	    if (copyMe.getQualifiers() != null) {
		    qualifiers.addAll(copyMe.getQualifiers());
	    }
		
	    if (copyMe.getMetadata() != null) {
		    metadatas.putAll(ReflectionHelper.deepCopyMetadata(copyMe.getMetadata()));
	    }
	}
	
	/**
	 * This creates this descriptor impl, taking all of the fields
	 * as given
	 * 
	 * @param contracts The set of contracts this descriptor impl should advertise (should not be null)
	 * @param name The name of this descriptor (may be null)
	 * @param scope The scope of this descriptor.  If null PerLookup is assumed
	 * @param implementation The name of the implementation class (should not be null)
	 * @param metadatas The metadata associated with this descriptor (should not be null)
	 * @param qualifiers The set of qualifiers associated with this descriptor (should not be null)
	 * @param descriptorType The type of this descriptor (should not be null)
	 * @param loader The HK2Loader to associated with this descriptor (may be null)
	 * @param rank The rank to initially associate with this descriptor
	 * @param baseDescriptor The base descriptor to associated with this descriptor
	 * @param id The ID this descriptor should take (may be null)
	 * @param locatorId The locator ID this descriptor should take (may be null)
	 */
	public DescriptorImpl(
	        Set<String> contracts,
			String name,
			String scope,
			String implementation,
			Map<String, List<String>> metadatas,
			Set<String> qualifiers,
			DescriptorType descriptorType,
			HK2Loader loader,
			int rank,
			Descriptor baseDescriptor,
			Long id,
			Long locatorId) {
		this.contracts.addAll(contracts);
		
		this.implementation = implementation;
		
		this.name = name;
		this.scope = scope;
		this.metadatas.putAll(ReflectionHelper.deepCopyMetadata(metadatas));
		this.qualifiers.addAll(qualifiers);
		this.descriptorType = descriptorType;
		this.id = id;
		this.rank = rank;
		this.locatorId = locatorId;
		this.loader = loader;
		this.baseDescriptor = baseDescriptor;
	}
	
	@Override
	public synchronized Set<String> getAdvertisedContracts() {
		return Collections.unmodifiableSet(contracts);
	}
	
	/**
	 * Adds an advertised contract to the set of contracts advertised by this descriptor
	 * @param addMe The contract to add.  May not be null
	 */
	public synchronized void addAdvertisedContract(String addMe) {
	    if (addMe == null) return;
	    contracts.add(addMe);
	}
	
	/**
	 * Removes an advertised contract from the set of contracts advertised by this descriptor
	 * @param removeMe The contract to remove.  May not be null
	 * @return true if removeMe was removed from the set
	 */
	public synchronized boolean removeAdvertisedContract(String removeMe) {
	    if (removeMe == null) return false;
	    return contracts.remove(removeMe);
	}

	@Override
	public synchronized String getImplementation() {
		return implementation;
	}
	
	/**
	 * Sets the implementation
	 * @param implementation The implementation this descriptor should have
	 */
    public synchronized void setImplementation(String implementation) {
        this.implementation = implementation;
    }

	@Override
	public synchronized String getScope() {
		return scope;
	}
	
	/**
	 * Sets the scope this descriptor should have
	 * @param scope The scope of this descriptor
	 */
	public synchronized void setScope(String scope) {
	    this.scope = scope;
	}

	@Override
	public synchronized String getName() {
		return name;
	}
	
	/**
	 * Sets the name this descriptor should have
	 * @param name The name for this descriptor
	 */
	public synchronized void setName(String name) {
	    this.name = name;
	}

	@Override
	public synchronized Set<String> getQualifiers() {
		return Collections.unmodifiableSet(qualifiers);
	}
	
	/**
	 * Adds the given string to the list of qualifiers
	 * 
	 * @param addMe The fully qualified class name of the qualifier to add.  May not be null
	 */
	public synchronized void addQualifier(String addMe) {
	    if (addMe == null) return;
	    qualifiers.add(addMe);
	}
	
	/**
	 * Removes the given qualifier from the list of qualifiers
	 * 
	 * @param removeMe The fully qualifier class name of the qualifier to remove.  May not be null
	 * @return true if the given qualifier was removed
	 */
	public synchronized boolean removeQualifier(String removeMe) {
	    if (removeMe == null) return false;
	    return qualifiers.remove(removeMe);
	}

    @Override
    public synchronized DescriptorType getDescriptorType() {
        return descriptorType;
    }
    
    /**
     * Sets the descriptor type
     * @param descriptorType The descriptor type.  May not be null
     */
    public synchronized void setDescriptorType(DescriptorType descriptorType) {
        if (descriptorType == null) throw new IllegalArgumentException();
        this.descriptorType = descriptorType;
    }

	@Override
	public synchronized Map<String, List<String>> getMetadata() {
		return Collections.unmodifiableMap(metadatas);
	}
	
	/**
	 * Adds a value to the list of values associated with this key
	 * 
	 * @param key The key to which to add the value.  May not be null
	 * @param value The value to add.  May not be null
	 */
	public synchronized void addMetadata(String key, String value) {
	    ReflectionHelper.addMetadata(metadatas, key, value);
	}
	
	/**
	 * Removes the given value from the given key
	 * 
	 * @param key The key of the value to remove.  May not be null
	 * @param value The value to remove.  May not be null
	 * @return true if the value was removed
	 */
	public synchronized boolean removeMetadata(String key, String value) {
	    return ReflectionHelper.removeMetadata(metadatas, key, value);
	}
	
	/**
	 * Removes all the metadata values associated with key
	 * 
	 * @param key The key of the metadata values to remove
	 * @return true if any value was removed
	 */
	public synchronized boolean removeAllMetadata(String key) {
	    return ReflectionHelper.removeAllMetadata(metadatas, key);
	}
	
	/* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLoader()
     */
    @Override
    public synchronized HK2Loader getLoader() {
        return loader;
    }
    
    /**
     * Sets the loader to use with this descriptor
     * @param loader The loader to use with this descriptor
     */
    public synchronized void setLoader(HK2Loader loader) {
        this.loader = loader;
    }

    @Override
    public synchronized int getRanking() {
        return rank;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#setRanking(int)
     */
    @Override
    public synchronized int setRanking(int ranking) {
        int retVal = rank;
        rank = ranking;
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getBaseDescriptor()
     */
    @Override
    public synchronized Descriptor getBaseDescriptor() {
        return baseDescriptor;
    }
    
    /**
     * Sets the base descriptor to be associated with this descriptor
     * 
     * @param baseDescriptor The base descriptor to be associated with this descriptor
     */
    public synchronized void setBaseDescriptor(Descriptor baseDescriptor) {
        this.baseDescriptor = baseDescriptor;
    }
	
	@Override
	public synchronized Long getServiceId() {
		return id;
	}
	
	/**
	 * Sets the service id for this descriptor
	 * @param id the service id for this descriptor
	 */
	public synchronized void setServiceId(Long id) {
	    this.id = id;
	}
	
	@Override
	public synchronized Long getLocatorId() {
	    return locatorId;
	}
	
	/**
	 * Sets the locator id for this descriptor
	 * @param locatorId the locator id for this descriptor
	 */
	public synchronized void setLocatorId(Long locatorId) {
	    this.locatorId = locatorId;
	}
	
	public synchronized String toString() {
	    return ReflectionHelper.prettyPrintDescriptor(this);
	}
}
