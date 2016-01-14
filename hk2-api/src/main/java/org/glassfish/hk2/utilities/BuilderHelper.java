/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2016 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.Metadata;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ProxyForSameScope;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.UseProxy;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.internal.ActiveDescriptorBuilderImpl;
import org.glassfish.hk2.internal.ConstantActiveDescriptor;
import org.glassfish.hk2.internal.DescriptorBuilderImpl;
import org.glassfish.hk2.internal.IndexedFilterImpl;
import org.glassfish.hk2.internal.SpecificFilterImpl;
import org.glassfish.hk2.internal.StarFilter;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * This class is used to generate DescriptorBuilders to be used
 * as a simple mechanism to create a Filter or Descriptor.
 */
public class BuilderHelper {
    /**
     * Returns an indexed filter that will return all descriptors that
     * have contract as an advertised contract
     * 
     * @param contract The advertised contract to look for
     * @return The indexed filter that can be used to calls to ServiceLocator methods
     */
    public static IndexedFilter createContractFilter(String contract) {
        return new IndexedFilterImpl(contract, null);
    }
    
    /**
     * Returns an indexed filter that will return all descriptors that
     * have the given name
     * 
     * @param name The name to look for
     * @return The indexed filter that can be used to calls to ServiceLocator methods
     */
    public static IndexedFilter createNameFilter(String name) {
        return new IndexedFilterImpl(null, name);
    }
    
    /**
     * Returns an indexed filter that will return all descriptors that
     * have the given name and given contract
     * 
     * @param contract The advertised contract to look for
     * @param name The name to look for
     * @return The indexed filter that can be used to calls to ServiceLocator methods
     */
    public static IndexedFilter createNameAndContractFilter(String contract, String name) {
        return new IndexedFilterImpl(contract, name);
    }
    
    /** The key for the name field of the tokenized string */
    public final static String NAME_KEY = "name";
    
    /** The key for the qualifier field of the tokenized string */
    public final static String QUALIFIER_KEY = "qualifier";
    
    /** The token separator */
    public final static String TOKEN_SEPARATOR = ";";
    
    /**
     * Creates a filter from a token string as per the following rules.<OL>
     * <LI>The token delimiter is ;</LI>
     * <LI>The first token is the contract.  If the tokenString starts with ; there is no contract</LI>
     * <LI>All other tokens are in key = value form (with = being the separator)</LI>
     * <LI>A valid key that can appear only once is &quot;name&quot;</LI>
     * <LI>A valid key that can appear any number of times is &quot;qualifier&quot;</LI>
     * </OL>
     * <P>
     * The following are examples of valid token strings:<UL>
     * <LI>com.acme.product.RocketShoes</LI>
     * <LI>com.acme.product.Sneakers;name=Nike</LI>
     * <LI>com.acme.product.Sneakers;name=Nike;qualifier=com.acme.color.Red;qualifier=com.acme.style.HighTop</LI>
     * <LI>;name=Narcissus</LI>
     * </UL>
     * 
     * @param tokenString A non-null string following the rules stated above
     * @return A filter that will match the given string
     * @throws IllegalArgumentException If the token string is invalid in some way
     */
    public static IndexedFilter createTokenizedFilter(String tokenString) throws IllegalArgumentException {
        if (tokenString == null) throw new IllegalArgumentException("null passed to createTokenizedFilter");
        
        StringTokenizer st = new StringTokenizer(tokenString, TOKEN_SEPARATOR);
        
        String contract = null;
        String name = null;
        final Set<String> qualifiers = new LinkedHashSet<String>();
        
        boolean firstToken = true;
        if (tokenString.startsWith(TOKEN_SEPARATOR)) {
            firstToken = false;
        }
        
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (firstToken) {
                firstToken = false;
                
                if (token.length() <= 0) continue;
                contract = token;
            }
            else {
                int index = token.indexOf('=');
                if (index < 0) {
                    throw new IllegalArgumentException("No = character found in token " + token);
                }
                
                String leftHandSide = token.substring(0, index);
                String rightHandSide = token.substring(index + 1);
                if (rightHandSide.length() <= 0) {
                    throw new IllegalArgumentException("No value found in token " + token);
                }
                
                if (NAME_KEY.equals(leftHandSide)) {
                    name = rightHandSide;
                }
                else if (QUALIFIER_KEY.equals(leftHandSide)) {
                    qualifiers.add(rightHandSide);
                }
                else {
                    throw new IllegalArgumentException("Unknown key: " + leftHandSide);
                }
            }
        }
        
        final String fContract = contract;
        final String fName = name;
        
        return new IndexedFilter() {

            @Override
            public boolean matches(Descriptor d) {
                if (qualifiers.isEmpty()) return true;
                
                return d.getQualifiers().containsAll(qualifiers);
            }

            @Override
            public String getAdvertisedContract() {
                return fContract;
            }

            @Override
            public String getName() {
                return fName;
            }
            
            @Override
            public String toString() {
                String cField = (fContract == null) ? "" : fContract;
                String nField = (fName == null) ? "" : ";name=" + fName;
                
                StringBuffer sb = new StringBuffer();
                for (String q : qualifiers) {
                    sb.append(";qualifier=" + q);
                }
                
                return "TokenizedFilter(" + cField + nField + sb.toString() + ")";
            }
            
        };
        
    }
    
    /**
     * This method creates a filter that will match one and only one descriptor.  The passed
     * in descriptor must have both its serviceID and locatorId filled in, or else this
     * method will throw an IllegalArgumentException
     * 
     * @param descriptor The descriptor from which to create a filter
     * @return A filter to use that will match this descriptor exactly
     */
    public static IndexedFilter createSpecificDescriptorFilter(Descriptor descriptor) {
        String contract = ServiceLocatorUtilities.getBestContract(descriptor);
        
        String name = descriptor.getName();
        
        if (descriptor.getServiceId() == null) {
            throw new IllegalArgumentException("The descriptor must have a specific service ID");
        }
        
        if (descriptor.getLocatorId() == null) {
            throw new IllegalArgumentException("The descriptor must have a specific locator ID");
        }
        
        return new SpecificFilterImpl(contract, name,
                descriptor.getServiceId(),
                descriptor.getLocatorId());
        
    }
    
    /**
     * Returns a filter that will return true an IndexedFilter that will match 
     * the {@link DescriptorImpl#equals(Object)} return
     * 
     * @param descriptorImpl A non-null Descriptor to compare against other Descriptors
     * @param deepCopy If true then a copy will be made of the descriptorImpl to protect the filter
     * from the case where the incoming descriptorImpl may change.  If false then the user
     * must ensure that the fields of the descriptorImpl cannot change after the filter is returned.
     * @return An IndexedFilter that can be used to determine equality with the given filter
     * based on the fields of the Descriptor
     */
    public static IndexedFilter createDescriptorFilter(Descriptor descriptorImpl, boolean deepCopy) {
        final Descriptor filterDescriptor = (deepCopy) ? new DescriptorImpl(descriptorImpl) : descriptorImpl ;
        return new IndexedFilter() {

            @Override
            public boolean matches(Descriptor d) {
                return DescriptorImpl.descriptorEquals(filterDescriptor, d);
            }

            @Override
            public String getAdvertisedContract() {
                Set<String> contracts = filterDescriptor.getAdvertisedContracts();
                if (contracts == null || contracts.isEmpty()) return null;
                return contracts.iterator().next();
            }

            @Override
            public String getName() {
                return filterDescriptor.getName();
            }
            
        };
        
    }
    
    /**
     * Returns a filter that will return true an IndexedFilter that will match 
     * the {@link DescriptorImpl#equals(Object)} return
     * 
     * @param descriptorImpl A non-null Descriptor to compare against other Descriptors
     * @return An IndexedFilter that can be used to determine equality with the given filter
     * based on the fields of the Descriptor
     */
    public static IndexedFilter createDescriptorFilter(Descriptor descriptorImpl) {
        return createDescriptorFilter(descriptorImpl, true);
    }
    
    /**
     * Returns a filter of type Descriptor that matches
     * all descriptors
     * 
     * @return A filter that matches all descriptors
     */
    public static Filter allFilter() {
      return StarFilter.getDescriptorFilter();
    }
	
	/**
     * This method links an implementation class with a {@link DescriptorBuilder}, to
     * be used to further build the {@link Descriptor}.
     * 
     * @param implementationClass The fully qualified name of the implementation
     * class to be associated with the DescriptorBuilder.
     * @param addToContracts if true, this implementation class will be added to the
     * list of contracts
     * 
     * @return A {@link DescriptorBuilder} that can be used to further build up the
     * {@link Descriptor}
     * @throws IllegalArgumentException if implementationClass is null
     */
    public static DescriptorBuilder link(String implementationClass, boolean addToContracts) throws IllegalArgumentException {
        if (implementationClass == null) throw new IllegalArgumentException();
        
        return new DescriptorBuilderImpl(implementationClass, addToContracts);
    }
	
	/**
	 * This method links an implementation class with a {@link DescriptorBuilder}, to
	 * be used to further build the {@link Descriptor}.  This method will automatically
	 * put the implementationClass into the list of advertised contracts.
	 * 
	 * @param implementationClass The fully qualified name of the implementation
	 * class to be associated with the PredicateBuilder.
	 * 
	 * @return A {@link DescriptorBuilder} that can be used to further build up the
	 * {@link Descriptor}
	 * @throws IllegalArgumentException if implementationClass is null
	 */
	public static DescriptorBuilder link(String implementationClass) throws IllegalArgumentException {
	    return link(implementationClass, true);
	}
	
	/**
     * This method links an implementation class with a {@link DescriptorBuilder}, to
     * be used to further build the {@link Descriptor}
     * 
     * @param implementationClass The implementation class to be associated
     * with the {@link DescriptorBuilder}.
     * @param addToContracts true if this impl class should be automatically added to
     * the list of contracts
     * @return A {@link DescriptorBuilder} that can be used to further build up the
     * {@link Descriptor}
     * @throws IllegalArgumentException if implementationClass is null
     */
    public static DescriptorBuilder link(Class<?> implementationClass, boolean addToContracts) throws IllegalArgumentException {
        if (implementationClass == null) throw new IllegalArgumentException();
        
        DescriptorBuilder builder = link(implementationClass.getName(), addToContracts);
        
        return builder;
    }
	
	/**
	 * This method links an implementation class with a {@link DescriptorBuilder}, to
	 * be used to further build the {@link Descriptor}.
	 * 
	 * @param implementationClass The implementation class to be associated
	 * with the {@link DescriptorBuilder}.
	 * @return A {@link DescriptorBuilder} that can be used to further build up the
	 * {@link Descriptor}
	 * @throws IllegalArgumentException if implementationClass is null
	 */
	public static DescriptorBuilder link(Class<?> implementationClass) throws IllegalArgumentException {
	    if (implementationClass == null) throw new IllegalArgumentException();
	    
	    boolean isFactory = (Factory.class.isAssignableFrom(implementationClass));
	    
	    DescriptorBuilder db = link(implementationClass, !isFactory);
	    
	    return db;
	}
	
	/**
     * This method creates an {@link ActiveDescriptorBuilder}, whose job it
     * is to create an unreified {@link ActiveDescriptor}.  The implementation
     * class given will NOT automatically be added to the set of contracts
     * of the {@link ActiveDescriptor}.
     * 
     * @param implementationClass The implementation class to be associated
     * with the {@link ActiveDescriptorBuilder}.
     * @return A {@link ActiveDescriptorBuilder} that can be used to further build up the
     * {@link Descriptor}
     * @throws IllegalArgumentException if implementationClass is null
     */
    public static ActiveDescriptorBuilder activeLink(Class<?> implementationClass) throws IllegalArgumentException {
        if (implementationClass == null) throw new IllegalArgumentException();
        
        return new ActiveDescriptorBuilderImpl(implementationClass);
    }
    
    /**
     * This creates a descriptor that will always return the given object.  The
     * set of types in the advertised contracts will contain the class of the
     * constant along with:<UL>
     * <LI>Any superclass of the constant marked with {@link Contract}</LI>
     * <LI>Any interface of the constant marked with {@link Contract}</LI>
     * </UL>
     * 
     * @param constant The non-null constant that should always be returned from
     * the create method of this ActiveDescriptor.  
     * @return The descriptor returned can be used in calls to
     * DynamicConfiguration.addActiveDescriptor
     * @throws IllegalArgumentException if constant is null
     */
    public static <T> AbstractActiveDescriptor<T> createConstantDescriptor(T constant) {
        if (constant == null) throw new IllegalArgumentException();
        
        Set<Type> contracts;
        
        Class<?> cClass = constant.getClass();
        ContractsProvided provided = cClass.getAnnotation(ContractsProvided.class);
        
        if (provided != null) {
            contracts = new HashSet<Type>();
            for (Class<?> specified : provided.value()) {
                contracts.add(specified);
            }
        }
        else {
            contracts = ReflectionHelper.getAdvertisedTypesFromObject(constant, Contract.class);
        }
        
        return createConstantDescriptor(constant,
                ReflectionHelper.getName(constant.getClass()),
                contracts.toArray(new Type[contracts.size()]));
    }
    
    /**
     * Gets the rank from the given class
     * 
     * @param fromClass The class to get the rank from.  Will also check all
     * superclasses
     * @return The rank this class should initially have, or 0 if there is
     * no Rank annotation on this class or all its superclasses
     */
    public static int getRank(Class<?> fromClass) {
        while (fromClass != null && !Object.class.equals(fromClass)) {
            Rank rank = fromClass.getAnnotation(Rank.class);
            if (rank != null) {
                return rank.value();
            }
            
            fromClass = fromClass.getSuperclass();
        }
        
        return 0;
    }
    
    /**
     * This creates a descriptor that will always return the given object.
     * The advertised contracts is given in the incoming parameter and the
     * name on the descriptor also comes from the incoming parameter.
     * 
     * @param constant The non-null constant that should always be returned from
     * the create method of this ActiveDescriptor.
     * @param name The possibly null name that should be associated with this constant descriptor
     * @param contracts The possibly empty set of contracts that should be associated with this
     * descriptor 
     * @return The descriptor returned can be used in calls to
     * DynamicConfiguration.addActiveDescriptor
     * @throws IllegalArgumentException if constant is null
     */
    public static <T> AbstractActiveDescriptor<T> createConstantDescriptor(T constant, String name, Type... contracts) {
        if (constant == null) throw new IllegalArgumentException();
        
        Annotation scope =
                ReflectionHelper.getScopeAnnotationFromObject(constant);
        Class<? extends Annotation> scopeClass = (scope == null) ? PerLookup.class :
            scope.annotationType();
        
        Set<Annotation> qualifiers =
                ReflectionHelper.getQualifiersFromObject(constant);
        
        Map<String, List<String>> metadata = new HashMap<String, List<String>>();
        if (scope != null) {
            getMetadataValues(scope, metadata);
        }
        
        for (Annotation qualifier : qualifiers) {
            getMetadataValues(qualifier, metadata);
        }
        
        Set<Type> contractsAsSet;
        if (contracts.length <= 0) {
            contractsAsSet = ReflectionHelper.getAdvertisedTypesFromObject(constant, Contract.class);
        }
        else {
            contractsAsSet = new LinkedHashSet<Type>();
            
            for (Type cType : contracts) {
                contractsAsSet.add(cType);
            }
        }
        
        Boolean proxy = null;
        UseProxy up = constant.getClass().getAnnotation(UseProxy.class);
        if (up != null) {
            proxy = up.value();
        }
        
        Boolean proxyForSameScope = null;
        ProxyForSameScope pfss = constant.getClass().getAnnotation(ProxyForSameScope.class);
        if (pfss != null) {
            proxyForSameScope = pfss.value();
        }
        
        DescriptorVisibility visibility = DescriptorVisibility.NORMAL;
        Visibility vi = constant.getClass().getAnnotation(Visibility.class);
        if (vi != null) {
            visibility = vi.value();
        }
        
        String classAnalysisName = null;
        Service service = constant.getClass().getAnnotation(Service.class);
        if (service != null) {
            classAnalysisName = service.analyzer();
        }
        
        int rank = getRank(constant.getClass());
        
        return new ConstantActiveDescriptor<T>(
                constant,
                contractsAsSet,
                scopeClass,
                name,
                qualifiers,
                visibility,
                proxy,
                proxyForSameScope,
                classAnalysisName,
                metadata,
                rank);
    }
    
    /**
     * This returns a DescriptorImpl based on the given class.  The returned
     * descriptor will include the class itself as an advertised contract and
     * all implemented interfaces that are marked &#64;Contract
     * 
     * @param clazz The class to analyze
     * @return The DescriptorImpl corresponding to this class
     */
    public static DescriptorImpl createDescriptorFromClass(Class<?> clazz) {
        if (clazz == null) return new DescriptorImpl();
        
        Set<String> contracts = ReflectionHelper.getContractsFromClass(clazz, Contract.class);
        String name = ReflectionHelper.getName(clazz);
        String scope = ReflectionHelper.getScopeFromClass(clazz, ServiceLocatorUtilities.getPerLookupAnnotation()).annotationType().getName();
        Set<String> qualifiers = ReflectionHelper.getQualifiersFromClass(clazz);
        DescriptorType type = DescriptorType.CLASS;
        if (Factory.class.isAssignableFrom(clazz)) {
            type = DescriptorType.PROVIDE_METHOD;
        }
        
        Boolean proxy = null;
        UseProxy up = clazz.getAnnotation(UseProxy.class);
        if (up != null) {
            proxy = Boolean.valueOf(up.value());
        }
        
        Boolean proxyForSameScope = null;
        ProxyForSameScope pfss = clazz.getAnnotation(ProxyForSameScope.class);
        if (pfss != null) {
            proxyForSameScope = Boolean.valueOf(pfss.value());
        }
        
        DescriptorVisibility visibility = DescriptorVisibility.NORMAL;
        Visibility vi = clazz.getAnnotation(Visibility.class);
        if (vi != null) {
            visibility = vi.value();
        }
        
        int rank = getRank(clazz);
        
        // TODO:  Can we get metadata from @Service?
        return new DescriptorImpl(
                contracts,
                name,
                scope,
                clazz.getName(),
                new HashMap<String, List<String>>(),
                qualifiers,
                type,
                visibility,
                null,
                rank,
                proxy,
                proxyForSameScope,
                null,
                null,
                null);
    }
	
    /**
     * Makes a deep copy of the incoming descriptor
     * 
     * @param copyMe The descriptor to copy
     * @return A new descriptor with all fields copied
     */
	public static DescriptorImpl deepCopyDescriptor(Descriptor copyMe) {
	    return new DescriptorImpl(copyMe);
	}
	
	/**
	 * This is a helper method that gets the metadata values from the
	 * {@link Metadata} annotations found in an annotation.
	 *  
	 * @param annotation The annotation to find {@link Metadata} values
	 * from.  May not be null.
	 * @param metadata A non-null metadata map.  The values found in the
	 * annotation will be added to this metadata map
	 * @throws IllegalArgumentException if annotation or metadata is null
	 * @throws MultiException if there was an error invoking the methods of the annotation
	 */
	public static void getMetadataValues(Annotation annotation, Map<String, List<String>> metadata) {
	    if (annotation == null || metadata == null) {
	        throw new IllegalArgumentException();
	    }
	    
	    final Class<? extends Annotation> annotationClass = annotation.annotationType();
	    Method annotationMethods[] = AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

            @Override
            public Method[] run() {
                return annotationClass.getDeclaredMethods();
            }
            
	    });
	    for (Method annotationMethod : annotationMethods) {
	        Metadata metadataAnno = annotationMethod.getAnnotation(Metadata.class);
	        if (metadataAnno == null) continue;
	        
	        String key = metadataAnno.value();
	        
	        Object addMe;
	        try {
	            addMe = ReflectionHelper.invoke(annotation, annotationMethod, new Object[0], false);
	        }
	        catch (Throwable th) {
	            throw new MultiException(th);
	        }
	        
	        if (addMe == null) continue;
	        
	        String addMeString;
	        if (addMe instanceof Class) {
	            addMeString = ((Class<?>) addMe).getName();
	        }
	        else if (addMe.getClass().isArray()) {
	            int length = Array.getLength(addMe);
	            
	            for (int lcv = 0; lcv < length; lcv++) {
	                Object iValue = Array.get(addMe, lcv);
	                
	                if (iValue == null) continue;
	                
	                if (iValue instanceof Class) {
	                    String cName = ((Class<?>) iValue).getName();
	                    
	                    ReflectionHelper.addMetadata(metadata, key, cName);
	                }
	                else {
	                    ReflectionHelper.addMetadata(metadata, key, iValue.toString());
	                }
	            }
	            
	            addMeString = null;
	        }
	        else {
	            addMeString = addMe.toString();
	        }
	        
	        if (addMeString != null) {
	            ReflectionHelper.addMetadata(metadata, key, addMeString);
	        }
	    }
	}
	
	/**
	 * Creates a ServiceHandle that will always return the given object from
	 * the {@link ServiceHandle#getService()} method.  The {@link ServiceHandle#getActiveDescriptor()}
	 * will return null and the {@link ServiceHandle#destroy()} method will
	 * do nothing
	 * 
	 * @param obj The object to be associated with this ServiceHandle.  May be null
	 * @return A {@link ServiceHandle} that will always return the given value
	 */
	public static <T> ServiceHandle<T> createConstantServiceHandle(final T obj) {
	    return new ServiceHandle<T>() {
	        private Object serviceData;

            @Override
            public T getService() {
                return obj;
            }

            @Override
            public ActiveDescriptor<T> getActiveDescriptor() {
                return null;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public void destroy() {
                // Do nothing
            }

            @Override
            public synchronized void setServiceData(Object serviceData) {
                this.serviceData = serviceData;
                
            }

            @Override
            public synchronized Object getServiceData() {
                return serviceData;
            }
	        
	    };
	    
	}
	
	/**
	 * Determines if the given descriptor matches the given filter.  A null
	 * filter matches every descriptor.  Takes into account if the {@link Filter}
	 * implements {@link IndexedFilter}.
	 * 
	 * @param baseDescriptor The non-null descriptor to match the filter against
	 * @param filter The filter to match.  If null will return true
	 * @return true if baseDescriptor matches, false otherwise
	 */
	public static boolean filterMatches(final Descriptor baseDescriptor, final Filter filter) {
	    if (baseDescriptor == null) throw new IllegalArgumentException();
	    
        if (filter == null) return true;

        if (filter instanceof IndexedFilter) {
            IndexedFilter indexedFilter = (IndexedFilter) filter;

            String indexContract = indexedFilter.getAdvertisedContract();
            if (indexContract != null) {
                if (!baseDescriptor.getAdvertisedContracts().contains(indexContract)) {
                    return false;
                }
            }

            String indexName = indexedFilter.getName();
            if (indexName != null) {
                if (baseDescriptor.getName() == null) return false;

                if (!indexName.equals(baseDescriptor.getName())) {
                    return false;
                }
            }

            // After all that we can run the match method!
        }

        return filter.matches(baseDescriptor);
    }
}
