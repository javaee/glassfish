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
package org.glassfish.hk2.internal;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.HK2Loader;

/**
 * The implementation of the descriptor itself, with the
 * bonus of being serializable
 * 
 * @author jwells
 *
 * TODO:  Should this implementation class be put into a public package?
 * I (JRW) think yes, it should
 */
public class DescriptorImpl implements Descriptor, Serializable {
	/**
	 * For Serialization
	 */
	private static final long serialVersionUID = 77937523212000548L;
	
	private Set<String> contracts;
	private String implementation;
	private String name;
	private String scope;
	private Map<String, List<String>> metadatas;
	private Set<String> qualifiers;
	private DescriptorType descriptorType;
	private boolean validating;
	private HK2Loader loader;
	private int rank;
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
		contracts = copyMe.getAdvertisedContracts();
		name = copyMe.getName();
		scope = copyMe.getScope();
		implementation = copyMe.getImplementation();
		qualifiers = copyMe.getQualifiers();
		descriptorType = copyMe.getDescriptorType();
		loader = copyMe.getLoader();
		
		validating = copyMe.isValidating();
		metadatas = copyMe.getMetadata();
		rank = copyMe.getRanking();
		id = copyMe.getServiceId();
		locatorId = copyMe.getLocatorId();
	}
	
	/**
	 * This is a deep copy, as the sets are taken directly into
	 * this object
	 * 
	 * @param contracts
	 * @param name 
	 * @param scope 
	 * @param implementation 
	 * @param metadatas
	 * @param qualifiers
	 * @param descriptorType 
	 * @param loader 
	 * @param validating 
	 * @param rank 
	 * @param id
	 * @param locatorId 
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
			boolean validating,
			int rank,
			Long id,
			Long locatorId) {
		this.contracts = new HashSet<String>(contracts);
		
		this.implementation = implementation;
		
		this.name = name;
		this.scope = scope;
		this.metadatas = new HashMap<String, List<String>>(metadatas);
		this.qualifiers = new HashSet<String>(qualifiers);
		this.descriptorType = descriptorType;
		this.validating = validating;
		this.id = id;
		this.rank = rank;
		this.locatorId = locatorId;
		this.loader = loader;
	}

	@Override
	public Set<String> getAdvertisedContracts() {
		return Collections.unmodifiableSet(contracts);
	}

	@Override
	public String getImplementation() {
		return implementation;
	}

	@Override
	public String getScope() {
		return scope;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<String> getQualifiers() {
		return new HashSet<String>(qualifiers);
	}

    @Override
    public DescriptorType getDescriptorType() {
        return descriptorType;
    }
	
    @Override
    public boolean isValidating() {
        // TODO Auto-generated method stub
        return validating;
    }

	@Override
	public Map<String, List<String>> getMetadata() {
		return new HashMap<String, List<String>>(metadatas);
	}
	
	/* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#getLoader()
     */
    @Override
    public HK2Loader getLoader() {
        return loader;
    }

    @Override
    public int getRanking() {
        return rank;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Descriptor#setRanking(int)
     */
    @Override
    public int setRanking(int ranking) {
        int retVal = rank;
        rank = ranking;
        return retVal;
    }
	
	@Override
	public Long getServiceId() {
		return id;
	}
	
	@Override
	public Long getLocatorId() {
	    return locatorId;
	}
	
	private static String writeSet(Set<?> set) {
		StringBuffer sb = new StringBuffer("{");
		
		boolean first = true;
		for (Object writeMe : set) {
			if (first) {
				first = false;
				sb.append(writeMe.toString());
			}
			else {
				sb.append("," + writeMe.toString());
			}
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	private static String writeList(List<String> list) {
		StringBuffer sb = new StringBuffer("[");
		
		boolean first = true;
		for (String writeMe : list) {
			if (first) {
				first = false;
				sb.append(writeMe.toString());
			}
			else {
				sb.append("," + writeMe.toString());
			}
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	private static String writeMetadata(Map<String, List<String>> metadata) {
		StringBuffer sb = new StringBuffer("{");
		
		boolean first = true;
		for (Map.Entry<String, List<String>> entry : metadata.entrySet()) {
			if (first) {
				first = false;
			    sb.append(entry.getKey() + "=");
			}
			else {
				sb.append("," + entry.getKey() + "=");
			}
			
			sb.append(writeList(entry.getValue()));
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("DescriptorImpl(");
		
		sb.append("\n\timplementation=" + implementation);
		
		if (name != null) {
		    sb.append("\n\tname=" + name);
		}
		
		sb.append("\n\tcontracts=");
		sb.append(writeSet(contracts));
		
		sb.append("\n\tscope=" + scope);
		
		sb.append("\n\tqualifiers=");
		sb.append(writeSet(qualifiers));
		
		sb.append("\n\tdescriptorType=" + descriptorType);
		
		sb.append("\n\tmetadata=");
		sb.append(writeMetadata(metadatas));
		
		sb.append("\n\tloader=" + loader);
		
		sb.append("\n\tid=" + id);
		
		sb.append("\n\tlocatorId=" + locatorId);
		
		sb.append(")");
		
		return sb.toString();
	}

    

    

    
    
}
