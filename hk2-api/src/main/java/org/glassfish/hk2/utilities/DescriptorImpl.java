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

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * The implementation of the descriptor itself, with the
 * bonus of being externalizable, and having writeable fields
 * 
 * @author jwells
 */
public class DescriptorImpl implements Descriptor, Externalizable {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 1558442492395467828L;
    
    private final static String CONTRACT_KEY = "contract=";
    private final static String NAME_KEY = "name=";
    private final static String SCOPE_KEY = "scope=";
    private final static String QUALIFIER_KEY = "qualifier=";
    private final static String TYPE_KEY = "type=";
    private final static String VISIBILITY_KEY = "visibility=";
    private final static String METADATA_KEY = "metadata=";
    private final static String RANKING_KEY = "rank=";
    private final static String PROXIABLE_KEY = "proxiable=";
    private final static String PROXY_FOR_SAME_SCOPE_KEY = "proxyForSameScope=";
    private final static String ANALYSIS_KEY = "analysis=";
    private final static String PROVIDE_METHOD_DT = "PROVIDE";
    private final static String LOCAL_DT = "LOCAL";
    private final static String START_START = "[";
    private final static String END_START = "]";
    private final static char END_START_CHAR = ']';
    private final static String SINGLETON_DIRECTIVE = "S";
    private final static String NOT_IN_CONTRACTS_DIRECTIVE = "-";
    private final static char SINGLETON_DIRECTIVE_CHAR = 'S';
    private final static char NOT_IN_CONTRACTS_DIRECTIVE_CHAR = '-';
    
    private final static Set<String> EMPTY_CONTRACTS_SET = Collections.emptySet();
    private final static Set<String> EMPTY_QUALIFIER_SET = Collections.emptySet();
    private final static Map<String, List<String>> EMPTY_METADATAS_MAP = Collections.emptyMap();
	
	private Set<String> contracts;
	private String implementation;
	private String name;
	private String scope = PerLookup.class.getName();
	private Map<String, List<String>> metadatas;
	private Set<String> qualifiers;
	private DescriptorType descriptorType = DescriptorType.CLASS;
	private DescriptorVisibility descriptorVisibility = DescriptorVisibility.NORMAL;
	private transient HK2Loader loader;
	private int rank;
	private Boolean proxiable;
	private Boolean proxyForSameScope;
	private String analysisName;
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
        descriptorVisibility = copyMe.getDescriptorVisibility();
        loader = copyMe.getLoader();
        rank = copyMe.getRanking();
        proxiable = copyMe.isProxiable();
        proxyForSameScope = copyMe.isProxyForSameScope();
        id = copyMe.getServiceId();
        locatorId = copyMe.getLocatorId();
        analysisName = copyMe.getClassAnalysisName();
        
	    if (copyMe.getAdvertisedContracts() != null && !copyMe.getAdvertisedContracts().isEmpty()) {
	        contracts = new LinkedHashSet<String>();
	        contracts.addAll(copyMe.getAdvertisedContracts());
	    }
		
	    if (copyMe.getQualifiers() != null && !copyMe.getQualifiers().isEmpty()) {
	        qualifiers = new LinkedHashSet<String>();
		    qualifiers.addAll(copyMe.getQualifiers());
	    }
		
	    if (copyMe.getMetadata() != null && !copyMe.getMetadata().isEmpty()) {
	        metadatas = new LinkedHashMap<String, List<String>>();
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
	 * @param descriptorVisibility The visibility this descriptor should have
	 * @param loader The HK2Loader to associated with this descriptor (may be null)
	 * @param rank The rank to initially associate with this descriptor
	 * @param proxiable The proxiable value to associate with this descriptor (may be null)
	 * @param proxyForSameScope The proxyForSameScope value to associate with this descriptor (may be null)
	 * @param analysisName The name of the ClassAnalysis service to use
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
			DescriptorVisibility descriptorVisibility,
			HK2Loader loader,
			int rank,
			Boolean proxiable,
			Boolean proxyForSameScope,
			String analysisName,
			Long id,
			Long locatorId) {
	    if (contracts != null && !contracts.isEmpty()) {
	        this.contracts = new LinkedHashSet<String>();
		    this.contracts.addAll(contracts);
	    }
		
		this.implementation = implementation;
		
		this.name = name;
		this.scope = scope;
		if (metadatas != null && !metadatas.isEmpty()) {
		    this.metadatas = new LinkedHashMap<String, List<String>>();
		    this.metadatas.putAll(ReflectionHelper.deepCopyMetadata(metadatas));
		}
		if (qualifiers != null && !qualifiers.isEmpty()) {
		    this.qualifiers = new LinkedHashSet<String>();
		    this.qualifiers.addAll(qualifiers);
		    
		}
		this.descriptorType = descriptorType;
		this.descriptorVisibility = descriptorVisibility;
		this.id = id;
		this.rank = rank;
		this.proxiable = proxiable;
		this.proxyForSameScope = proxyForSameScope;
		this.analysisName = analysisName;
		this.locatorId = locatorId;
		this.loader = loader;
	}
	
	@Override
	public synchronized Set<String> getAdvertisedContracts() {
	    if (contracts == null) return EMPTY_CONTRACTS_SET;
		return Collections.unmodifiableSet(contracts);
	}
	
	/**
	 * Adds an advertised contract to the set of contracts advertised by this descriptor
	 * @param addMe The contract to add.  May not be null
	 */
	public synchronized void addAdvertisedContract(String addMe) {
	    if (addMe == null) return;
	    if (contracts == null) contracts = new LinkedHashSet<String>();
	    contracts.add(addMe);
	}
	
	/**
	 * Removes an advertised contract from the set of contracts advertised by this descriptor
	 * @param removeMe The contract to remove.  May not be null
	 * @return true if removeMe was removed from the set
	 */
	public synchronized boolean removeAdvertisedContract(String removeMe) {
	    if (removeMe == null || contracts == null) return false;
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
	    if (qualifiers == null) return EMPTY_QUALIFIER_SET;
		return Collections.unmodifiableSet(qualifiers);
	}
	
	/**
	 * Adds the given string to the list of qualifiers
	 * 
	 * @param addMe The fully qualified class name of the qualifier to add.  May not be null
	 */
	public synchronized void addQualifier(String addMe) {
	    if (addMe == null) return;
	    if (qualifiers == null) qualifiers = new LinkedHashSet<String>();
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
	    if (qualifiers == null) return false;
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
    public synchronized DescriptorVisibility getDescriptorVisibility() {
        return descriptorVisibility;
    }
    
    /**
     * Sets the descriptor visilibity
     * @param descriptorVisibility The visibility this descriptor should have
     */
    public synchronized void setDescriptorVisibility(DescriptorVisibility descriptorVisibility) {
        if (descriptorVisibility == null) throw new IllegalArgumentException();
        this.descriptorVisibility = descriptorVisibility;
    }

	@Override
	public synchronized Map<String, List<String>> getMetadata() {
	    if (metadatas == null) return EMPTY_METADATAS_MAP;
		return Collections.unmodifiableMap(metadatas);
	}
	
	/**
	 * Sets the metadata of this DescriptorImpl to exactly the set
	 * of metadata in the incoming map.  Any previous metadata values
	 * will be removed.  A deep copy of the incoming map will be made,
	 * so it is safe to use the input map after use of this API
	 * 
	 * @param metadata The non-null metadata that this descriptor
	 * should have
	 */
	public synchronized void setMetadata(Map<String, List<String>> metadata) {
	    if (metadatas == null) {
	        metadatas = new LinkedHashMap<String, List<String>>();
	    }
	    else {
	        metadatas.clear();
	    }
	    
	    metadatas.putAll(ReflectionHelper.deepCopyMetadata(metadata));
	}
	
	/**
	 * Adds all of the entries from this map to the existing descriptor's
	 * metadata.  None of the keys in the map may have the '=' character
	 * 
	 * @param metadata The non-null but possibly empty list of fields
	 * to add to the metadata map
	 */
	public synchronized void addMetadata(Map<String, List<String>> metadata) {
	    if (metadatas == null) metadatas = new LinkedHashMap<String, List<String>>();
	    
        metadatas.putAll(ReflectionHelper.deepCopyMetadata(metadata));
    }
	
	/**
	 * Adds a value to the list of values associated with this key
	 * 
	 * @param key The key to which to add the value.  May not be null.  May
	 * not contain the character '='
	 * @param value The value to add.  May not be null
	 */
	public synchronized void addMetadata(String key, String value) {
	    if (metadatas == null) metadatas = new LinkedHashMap<String, List<String>>();
	    ReflectionHelper.addMetadata(metadatas, key, value);
	}
	
	/**
	 * Removes the given value from the given key
	 * 
	 * @param key The key of the value to remove.  May not be null, and
	 * may not contain the character '='
	 * @param value The value to remove.  May not be null
	 * @return true if the value was removed
	 */
	public synchronized boolean removeMetadata(String key, String value) {
	    if (metadatas == null) return false;
	    return ReflectionHelper.removeMetadata(metadatas, key, value);
	}
	
	/**
	 * Removes all the metadata values associated with key
	 * 
	 * @param key The key of the metadata values to remove
	 * @return true if any value was removed
	 */
	public synchronized boolean removeAllMetadata(String key) {
	    if (metadatas == null) return false;
	    return ReflectionHelper.removeAllMetadata(metadatas, key);
	}
	
	/**
     * Removes all metadata values
     */
    public synchronized void clearMetadata() {
        metadatas = null;
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
	public Boolean isProxiable() {
	    return proxiable;
	}
	
	/**
	 * Sets whether or not this descriptor should be proxied
	 * @param proxiable if true then this descriptor will be proxied.
	 * If false then this descriptor will not be proxied.  If null
	 * this descriptor will follow the rules of the scope it is in
	 */
	public void setProxiable(Boolean proxiable) {
	    this.proxiable = proxiable;
	}
	
	@Override
    public Boolean isProxyForSameScope() {
        return proxyForSameScope;
    }
	
	/**
	 * Sets whether or not to proxy this descriptor for other
	 * services in the same scope
	 * 
	 * @param proxyForSameScope if true then this descriptor will be proxied
	 * for services in the same scope.  If false then this descriptor will not
	 * be proxied for services in the same scope.  If null
     * this descriptor will follow the rules of the scope it is in
	 */
	public void setProxyForSameScope(Boolean proxyForSameScope) {
        this.proxyForSameScope = proxyForSameScope;
    }
	
	@Override
    public String getClassAnalysisName() {
        return analysisName;
    }
	
	/**
	 * Sets the name of the service that will be used
	 * to analyze this class
	 * 
	 * @param name The name of the {@link ClassAnalyzer}
	 * service that should be used to analyze this
	 * descriptor
	 */
	public void setClassAnalysisName(String name) {
	    analysisName = name;
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
	
	public int hashCode() {
	    int retVal = 0;
	    
	    if (implementation != null) {
	        retVal ^= implementation.hashCode();
	    }
	    if (contracts != null) {
	        for (String contract : contracts) {
	            retVal ^= contract.hashCode();
	        }
	    }
	    if (name != null) {
	        retVal ^= name.hashCode();
	    }
	    if (scope != null) {
	        retVal ^= scope.hashCode();
	    }
	    if (qualifiers != null) {
	        for (String qualifier : qualifiers) {
	            retVal ^= qualifier.hashCode();
	        }
	    }
	    if (descriptorType != null) {
	        retVal ^= descriptorType.hashCode();
	    }
	    if (descriptorVisibility != null) {
            retVal ^= descriptorVisibility.hashCode();
        }
	    if (metadatas != null) {
	        for (Map.Entry<String, List<String>> entries : metadatas.entrySet()) {
	            retVal ^= entries.getKey().hashCode();
	            
	            for (String value : entries.getValue()) {
	                retVal ^= value.hashCode();
	            }
	        }
	    }
	    if (proxiable != null) {
	        if (proxiable.booleanValue()) {
	            retVal ^= 1;
	        }
	        else {
	            retVal ^= -1;
	        }
	    }
	    if (proxyForSameScope != null) {
            if (proxyForSameScope.booleanValue()) {
                retVal ^= 2;
            }
            else {
                retVal ^= -2;
            }
        }
	    if (analysisName != null) {
	        retVal ^= analysisName.hashCode();
	    }
	    
	    return retVal;
	}
	
	private static <T> boolean equalOrderedCollection(Collection<T> a, Collection<T> b) {
	    if (a == b) return true;
	    if (a == null) return false;
	    if (b == null) return false;
	    
	    if (a.size() != b.size()) return false;
	    
	    Object aAsArray[] = a.toArray();
	    Object bAsArray[] = b.toArray();
	    
	    for (int lcv = 0; lcv < a.size(); lcv++) {
	        if (!GeneralUtilities.safeEquals(aAsArray[lcv], bAsArray[lcv])) return false;
	    }
	    
	    return true;
	}
	
	private static <T> boolean equalMetadata(Map<String, List<String>> a, Map<String, List<String>> b) {
        if (a == b) return true;
        if (a == null) return false;
        if (b == null) return false;
        
        if (a.size() != b.size()) return false;
        
        for (Map.Entry<String, List<String>> entry : a.entrySet()) {
            String aKey = entry.getKey();
            List<String> aValue = entry.getValue();
            
            List<String> bValue = b.get(aKey);
            if (bValue == null) return false;
            
            if (!equalOrderedCollection(aValue, bValue)) return false;
        }
        
        return true;
    }
	
	public boolean equals(Object a) {
	    if (a == null) return false;
	    if (!(a instanceof Descriptor)) return false;
	    Descriptor d = (Descriptor) a;
	    
	    if (!GeneralUtilities.safeEquals(implementation, d.getImplementation())) return false;
	    if (!equalOrderedCollection((contracts == null) ? EMPTY_CONTRACTS_SET : contracts, d.getAdvertisedContracts())) return false;
	    if (!GeneralUtilities.safeEquals(name, d.getName())) return false;
	    if (!GeneralUtilities.safeEquals(scope, d.getScope())) return false;
	    if (!equalOrderedCollection((qualifiers == null) ? EMPTY_QUALIFIER_SET : qualifiers, d.getQualifiers())) return false;
	    if (!GeneralUtilities.safeEquals(descriptorType, d.getDescriptorType())) return false;
	    if (!GeneralUtilities.safeEquals(descriptorVisibility, d.getDescriptorVisibility())) return false;
	    if (!equalMetadata((metadatas == null) ? EMPTY_METADATAS_MAP : metadatas, d.getMetadata())) return false;
	    if (!GeneralUtilities.safeEquals(proxiable, d.isProxiable())) return false;
	    if (!GeneralUtilities.safeEquals(proxyForSameScope, d.isProxyForSameScope())) return false;
	    if (!GeneralUtilities.safeEquals(analysisName, d.getClassAnalysisName())) return false;
	    
	    return true;
	}
	
	/**
	 * Will pretty print a descriptor
	 * 
	 * @param sb The string buffer put the pretty print into
	 * @param d The descriptor to write
	 */
	public static void pretty(StringBuffer sb, Descriptor d) {
	    if (sb == null || d == null) return;
	    
        sb.append("\n\timplementation=" + d.getImplementation());
        
        if (d.getName() != null) {
            sb.append("\n\tname=" + d.getName());
        }
        
        sb.append("\n\tcontracts=");
        sb.append(ReflectionHelper.writeSet(d.getAdvertisedContracts()));
        
        sb.append("\n\tscope=" + d.getScope());
        
        sb.append("\n\tqualifiers=");
        sb.append(ReflectionHelper.writeSet(d.getQualifiers()));
        
        sb.append("\n\tdescriptorType=" + d.getDescriptorType());
        
        sb.append("\n\tdescriptorVisibility=" + d.getDescriptorVisibility());
        
        sb.append("\n\tmetadata=");
        sb.append(ReflectionHelper.writeMetadata(d.getMetadata()));
        
        sb.append("\n\trank=" + d.getRanking());
        
        sb.append("\n\tloader=" + d.getLoader());
        
        sb.append("\n\tproxiable=" + d.isProxiable());
        
        sb.append("\n\tproxyForSameScope=" + d.isProxyForSameScope());
        
        sb.append("\n\tanalysisName=" + d.getClassAnalysisName());
        
        sb.append("\n\tid=" + d.getServiceId());
        
        sb.append("\n\tlocatorId=" + d.getLocatorId());
        
        sb.append("\n\tidentityHashCode=" + System.identityHashCode(d));
	    
	}
	
	public synchronized String toString() {
        StringBuffer sb = new StringBuffer("Descriptor(");
        
        pretty(sb, this);
        
        sb.append(")");
        
        return sb.toString();
	}
	
	/**
	 * This writes this object to the data output stream in a human-readable
	 * format, excellent for writing out data files
	 * 
	 * @param out The output stream to write this object out to
	 * @throws IOException on failure
	 */
	public void writeObject(PrintWriter out) throws IOException {
	
        out.print(START_START);
        
        // Implementation
        if (implementation != null) {
            out.print(implementation);
        }
        
        out.print(END_START);
        
        if (scope != null && scope.equals(Singleton.class.getName())) {
            out.print(SINGLETON_DIRECTIVE);
        }
        
        boolean implementationInContracts = true;
        if (contracts != null && implementation != null && !contracts.contains(implementation)) {
            out.print(NOT_IN_CONTRACTS_DIRECTIVE);
            implementationInContracts = false;
        }
        
        out.println();
        
        // Contracts
        if (contracts != null && !contracts.isEmpty() &&
                (!implementationInContracts || (contracts.size() > 1))) {
            String excluded = (implementationInContracts) ? implementation : null ;
            
            out.println(CONTRACT_KEY + ReflectionHelper.writeSet(contracts, excluded));
        }
        
        if (name != null) {
            out.println(NAME_KEY + name);
        }
        
        if ((scope != null) && !(
                scope.equals(PerLookup.class.getName()) ||
                scope.equals(Singleton.class.getName()))) {
            out.println(SCOPE_KEY + scope);
        }
        
        if (qualifiers != null && !qualifiers.isEmpty()) {
            out.println(QUALIFIER_KEY + ReflectionHelper.writeSet(qualifiers));
        }
        
        if (descriptorType != null && descriptorType.equals(DescriptorType.PROVIDE_METHOD)) {
            out.println(TYPE_KEY + PROVIDE_METHOD_DT);
        }
        
        if (descriptorVisibility != null && descriptorVisibility.equals(DescriptorVisibility.LOCAL)) {
            out.println(VISIBILITY_KEY + LOCAL_DT);
        }
        
        if (rank != 0) {
            out.println(RANKING_KEY + rank);
        }
        
        if (proxiable != null) {
            out.println(PROXIABLE_KEY + proxiable.booleanValue());
        }
        
        if (proxyForSameScope != null) {
            out.println(PROXY_FOR_SAME_SCOPE_KEY + proxyForSameScope.booleanValue());
        }
        
        if (analysisName != null &&
                !ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME.equals(analysisName)) {
            out.println(ANALYSIS_KEY + analysisName);
        }
        
        if (metadatas != null && !metadatas.isEmpty()) {
            out.println(METADATA_KEY + ReflectionHelper.writeMetadata(metadatas));
        }
        
        out.println();  // This demarks the end of the section
    }
	
	private void reinitialize() {
	    contracts = null;
	    implementation = null;
	    name = null;
	    scope = PerLookup.class.getName();
	    metadatas = null;
	    qualifiers = null;
	    descriptorType = DescriptorType.CLASS;
	    descriptorVisibility = DescriptorVisibility.NORMAL;
	    loader = null;
	    rank = 0;
	    proxiable = null;
	    proxyForSameScope = null;
	    analysisName = null;
	    id = null;
	    locatorId = null;
	}

	/**
	 * This can be used to read in instances of this object that were previously written out with
	 * writeObject.  Useful for reading from external data files
	 * 
	 * @param in The reader to read from
	 * @return true if a descriptor was read, false otherwise.  This is useful if reading a file that might have comments at the end
	 * @throws IOException on failure
	 */
	public boolean readObject(BufferedReader in) throws IOException {
	    // Reinitialize all fields
	    reinitialize();
	    
        String line = in.readLine();
        
        boolean sectionStarted = false;
        while (line != null) {
            String trimmed = line.trim();
            
            if (!sectionStarted) {
                if (trimmed.startsWith(START_START)) {
                    sectionStarted = true;
                    
                    int endStartIndex = trimmed.indexOf(END_START_CHAR, 1);
                    if (endStartIndex < 0) {
                        throw new IOException("Start of implementation ends without ] character: " +
                            trimmed);
                    }
                    
                    if (endStartIndex > 1) {
                        implementation = trimmed.substring(1, endStartIndex);
                    }
                    
                    String directives = trimmed.substring(endStartIndex + 1);
                    
                    boolean doesNotContainImplementation = false;
                    if (directives != null) {
                        for (int lcv = 0; lcv < directives.length(); lcv++) {
                            char charAt = directives.charAt(lcv);
                            
                            if (charAt == SINGLETON_DIRECTIVE_CHAR) {
                                scope = Singleton.class.getName();
                            }
                            else if (charAt == NOT_IN_CONTRACTS_DIRECTIVE_CHAR) {
                                doesNotContainImplementation = true;
                            }
                        }
                    }
                    
                    if (!doesNotContainImplementation && (implementation != null)) {
                        if (contracts == null) contracts = new LinkedHashSet<String>();
                        contracts.add(implementation);
                    }
                }
            }
            else {
                if (trimmed.length() <= 0) {
                    // A blank line indicates end of object
                    return true;
                }
                
                int equalsIndex = trimmed.indexOf('=');
                
                if (equalsIndex >= 1) {
                    
                    String leftHandSide = trimmed.substring(0, equalsIndex + 1);  // include the =
                    String rightHandSide = trimmed.substring(equalsIndex + 1);
                    
                    if (leftHandSide.equalsIgnoreCase(CONTRACT_KEY)) {
                        if (contracts == null) contracts = new LinkedHashSet<String>();
                        ReflectionHelper.readSet(rightHandSide, contracts);
                    }
                    else if (leftHandSide.equals(QUALIFIER_KEY)) {
                        LinkedHashSet<String> localQualifiers = new LinkedHashSet<String>();
                        ReflectionHelper.readSet(rightHandSide, localQualifiers);
                        if (!localQualifiers.isEmpty()) qualifiers = localQualifiers;
                    }
                    else if (leftHandSide.equals(NAME_KEY)) {
                        name = rightHandSide;
                    }
                    else if (leftHandSide.equals(SCOPE_KEY)) {
                        scope = rightHandSide;
                    }
                    else if (leftHandSide.equals(TYPE_KEY)) {
                        if (rightHandSide.equals(PROVIDE_METHOD_DT)) {
                            descriptorType = DescriptorType.PROVIDE_METHOD;
                        }
                    }
                    else if (leftHandSide.equals(VISIBILITY_KEY)) {
                        if (rightHandSide.equals(LOCAL_DT)) {
                            descriptorVisibility = DescriptorVisibility.LOCAL;
                        }
                    }
                    else if (leftHandSide.equals(METADATA_KEY)) {
                        LinkedHashMap<String, List<String>> localMetadatas = new LinkedHashMap<String, List<String>>();
                        ReflectionHelper.readMetadataMap(rightHandSide, localMetadatas);
                        if (!localMetadatas.isEmpty()) metadatas = localMetadatas;
                    }
                    else if (leftHandSide.equals(RANKING_KEY)) {
                        rank = Integer.parseInt(rightHandSide);
                    }
                    else if (leftHandSide.equals(PROXIABLE_KEY)) {
                        proxiable = Boolean.parseBoolean(rightHandSide);
                    }
                    else if (leftHandSide.equals(PROXY_FOR_SAME_SCOPE_KEY)) {
                        proxyForSameScope = Boolean.parseBoolean(rightHandSide);
                    }
                    else if (leftHandSide.equals(ANALYSIS_KEY)) {
                        analysisName = rightHandSide;
                    }
                    
                    // Otherwise it is an unknown type, just forget it
                }
            }
            
            line = in.readLine();
        }
        
        return sectionStarted;
    }


    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        StringWriter sw = new StringWriter();
        writeObject(new PrintWriter(sw));
        out.writeObject(sw.toString());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String descriptorString = (String) in.readObject();

        readObject(new BufferedReader( new StringReader(descriptorString)));
    }
}
