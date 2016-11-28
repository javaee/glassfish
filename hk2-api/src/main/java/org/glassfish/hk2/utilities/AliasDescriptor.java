/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2016 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.general.GeneralUtilities;


/**
 * An active descriptor class that serves as an alias for another descriptor.
 * This alias can have only one contract that may or may not also be
 * advertised by the host descriptor.  An AliasDescriptor
 * can have a different name from the host descriptor as well
 *
 * @author tbeerbower
 * @param <T> The cache type
 */
public class AliasDescriptor<T> extends AbstractActiveDescriptor<T> {
    /**
     * This will be put in all Alias descriptors.  The value
     * will be ALIAS_FREE_DESCRIPTOR if the descritpor being
     * aliased does not have a locator and service id.  If
     * the descriptor being aliased does have a locator and
     * service id the value will be <locatorId>.<serviceId>
     */
    public final static String ALIAS_METADATA_MARKER = "__AliasOf";
    
    /**
     * This is the value the metadata field ALIAS_METADATA_MARKER will
     * take if the descriptor being aliased does not have a locator id
     * or a service id
     */
    public final static String ALIAS_FREE_DESCRIPTOR = "FreeDescriptor";

    /**
     * For serialization
     */
    private static final long serialVersionUID = 2609895430798803508L;

    /**
     * The service locator.
     */
    private ServiceLocator locator;

    /**
     * The descriptor that this descriptor will alias.
     */
    private ActiveDescriptor<T> descriptor;

    /**
     * The contract type of this descriptor.
     */
    private String contract;

    /**
     * The set of annotations for this descriptor.
     */
    private Set<Annotation> qualifiers;
    
    /**
     * The set of qualifier names for this descriptor.
     */
    private Set<String> qualifierNames;

    /**
     * Indicates whether or not this descriptor has been initialized.
     */
    private boolean initialized = false;


    // ----- Constants ------------------------------------------------------

    /**
     * Empty set of contracts used to construct this descriptor.
     */
    private static final Set<Type> EMPTY_CONTRACT_SET = new HashSet<Type>();

    /**
     * Empty set of annotations used to construct this descriptor.
     */
    private static final Set<Annotation> EMPTY_ANNOTATION_SET = new HashSet<Annotation>();

    // ----- Constructors ---------------------------------------------------
    /**
     * For serialization
     */
    public AliasDescriptor() {
        super();
    }

    /**
     * Construct an AliasDescriptor.
     *
     * @param locator     the service locator
     * @param descriptor  the descriptor to be aliased
     * @param contract    the contact
     * @param name        the name
     */
    public AliasDescriptor(ServiceLocator locator,
                           ActiveDescriptor<T> descriptor,
                           String contract,
                           String name) {
        // pass in an empty contract set, an empty annotation set and a null
        // scope since we are not really reified and we don't want to reify
        // the given descriptor yet
        super(EMPTY_CONTRACT_SET,
                null,
                name,
                EMPTY_ANNOTATION_SET,
                descriptor.getDescriptorType(),
                descriptor.getDescriptorVisibility(),
                descriptor.getRanking(),
                descriptor.isProxiable(),
                descriptor.isProxyForSameScope(),
                descriptor.getClassAnalysisName(),
                descriptor.getMetadata());
        this.locator    = locator;
        this.descriptor = descriptor;
        this.contract   = contract;
        addAdvertisedContract(contract);
        super.setScope(descriptor.getScope());
        super.addMetadata(ALIAS_METADATA_MARKER, getAliasMetadataValue(descriptor));
    }
    
    private static String getAliasMetadataValue(ActiveDescriptor<?> descriptor) {
        Long locatorId = descriptor.getLocatorId();
        Long serviceId = descriptor.getServiceId();
        
        if (locatorId == null || serviceId == null) return ALIAS_FREE_DESCRIPTOR;
        
        return locatorId + "." + serviceId;
    }

    // ----- ActiveDescriptor -----------------------------------------------

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        ensureInitialized();
        return descriptor.getImplementationClass();
    }
    
    @Override
    public Type getImplementationType() {
        ensureInitialized();
        return descriptor.getImplementationType();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) {
        ensureInitialized();
        return locator.getServiceHandle(descriptor).getService();
    }


    // ----- AbstractActiveDescriptor overrides -----------------------------

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
    */
    @Override
    public boolean isReified() {
        // always return true to get past the addActiveDescriptor checks
        // even though the underlying descriptor may not be reified yet
        return true;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementation()
     */
    @Override
    public String getImplementation() {
        return descriptor.getImplementation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getContractTypes()
     */
    @Override
    public Set<Type> getContractTypes() {
        ensureInitialized();
        return super.getContractTypes();
    }

    /* (non-Javadoc)
    * @see org.glassfish.hk2.api.ActiveDescriptor#getScopeAnnotation()
    */
    @Override
    public Class<? extends Annotation> getScopeAnnotation() {
        ensureInitialized();
        return descriptor.getScopeAnnotation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getQualifierAnnotations()
     */
    @Override
    public synchronized Set<Annotation> getQualifierAnnotations() {
        ensureInitialized();

        if (qualifiers == null) {
            qualifiers = new HashSet<Annotation>(descriptor.getQualifierAnnotations());
            if (getName() != null) {
                qualifiers.add(new NamedImpl(getName()));
            }
        }
        return qualifiers;
    }
    
    @Override
    public synchronized Set<String> getQualifiers() {
        if (qualifierNames != null) return qualifierNames;
        
        qualifierNames = new HashSet<String>(descriptor.getQualifiers());
        if (getName() != null) {
            qualifierNames.add(Named.class.getName());
        }
        
        return qualifierNames;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#getInjectees()
     */
    @Override
    public List<Injectee> getInjectees() {
        ensureInitialized();
        return descriptor.getInjectees();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public void dispose(T instance) {
        ensureInitialized();
        descriptor.dispose(instance);
    }


    // ----- accessors ------------------------------------------------

    /**
     * Get the descriptor being aliased.
     *
     * @return the descriptor
     */
    public ActiveDescriptor<T> getDescriptor() {
        return descriptor;
    }


    // ----- Utility methods ------------------------------------------------

    /**
     * Ensure that this descriptor has been initialized.
     */
    @SuppressWarnings("unchecked")
    private synchronized void ensureInitialized() {
        if (!initialized) {
            // reify the underlying descriptor if needed
            if (!descriptor.isReified()) {
                descriptor = (ActiveDescriptor<T>) locator.reifyDescriptor(descriptor);
            }

            if (contract == null) {
                initialized = true;
                return;
            }

            HK2Loader loader = descriptor.getLoader();

            Type contractType = null;
            try {
                if (loader != null) {
                    contractType = loader.loadClass(contract);
                }
                else {
                    Class<?> ic = descriptor.getImplementationClass();
                    ClassLoader cl = null;
                    if (ic != null) {
                        cl = ic.getClassLoader();
                    }
                    if (cl == null) {
                        cl = ClassLoader.getSystemClassLoader();
                    }

                    contractType = cl.loadClass(contract);
                }
            }
            catch (ClassNotFoundException e) {
                // do nothing
            }

            super.addContractType(contractType);

            initialized = true;
        }
    }
    
    @Override
    public int hashCode() {
        int retVal;
        synchronized (this) {
            retVal = descriptor.hashCode();
        }
        
        if (getName() != null) {
            retVal ^= getName().hashCode();
        }
        
        if (contract != null) {
            retVal ^= contract.hashCode();
        }
        
        return retVal;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof AliasDescriptor)) return false;
        
        AliasDescriptor<?> other = (AliasDescriptor<?>) o;
        
        if (!other.descriptor.equals(descriptor)) return false;
        if (!GeneralUtilities.safeEquals(other.getName(), getName())) return false;
        return GeneralUtilities.safeEquals(other.contract, contract);
    }

    
}
