package org.glassfish.hk2.core.utilities;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.NamedImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A descriptor class that serves as an alias for another descriptor.
 *
 * @author tbeerbower
 */
public class AliasDescriptor<T> extends AbstractActiveDescriptor<T> {

    /**
     * The service locator.
     */
    private final ServiceLocator locator;

    /**
     * The descriptor that this descriptor will alias.
     */
    private final ActiveDescriptor<T> descriptor;

    /**
     * The contract type of this descriptor.
     */
    private final Type contract;

    /**
     * The set of annotations for this descriptor.
     */
    private Set<Annotation> qualifiers;

    /**
     * Indicates whether or not this descriptor has been initialized.
     */
    private boolean initialized = false;

    // ----- Constants ------------------------------------------------------

    private static final Set<Type> EMPTY_CONTRACT_SET = new HashSet<Type>();
    private static final Set<Annotation> EMPTY_ANNOTATION_SET = new HashSet<Annotation>();


    // ----- Constructors ---------------------------------------------------

    /**
     * Construct an AliasDescriptor.
     *
     * @param locator     the service locator
     * @param descriptor  the descriptor to be aliased
     * @param contract    the contact
     * @param name        the name
     */
    public AliasDescriptor(ServiceLocator locator, ActiveDescriptor<T> descriptor, Class<?> contract, String name) {

        super(EMPTY_CONTRACT_SET, null, name,
                EMPTY_ANNOTATION_SET, descriptor.getDescriptorType(),
                descriptor.getRanking());
        this.locator    = locator;
        this.descriptor = descriptor;
        this.contract   = contract;
        addAdvertisedContract(contract.getName());
        super.setScope(descriptor.getScope());
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#isReified()
     */
    @Override
    public boolean isReified() {
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
     * @see org.glassfish.hk2.api.ActiveDescriptor#getImplementationClass()
     */
    @Override
    public Class<?> getImplementationClass() {
        ensureInitialized();
        return descriptor.getImplementationClass();
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
    public Set<Annotation> getQualifierAnnotations() {
        ensureInitialized();

        if (qualifiers == null) {
            qualifiers = new HashSet<Annotation>(descriptor.getQualifierAnnotations());
            qualifiers.add(new NamedImpl(getName()));
        }
        return qualifiers;
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
     * @see org.glassfish.hk2.api.ActiveDescriptor#create(org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public T create(ServiceHandle<?> root) {
        ensureInitialized();
        return locator.getServiceHandle(descriptor).getService();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ActiveDescriptor#dispose(java.lang.Object, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public void dispose(T instance) {
        ensureInitialized();
        descriptor.dispose(instance);
    }

    /**
     * Ensure that this descriptor has been initialized.
     */
    private void ensureInitialized() {
        if (!initialized) {
            // reify the underlying descriptor if needed
            if (!descriptor.isReified()) {
                locator.reifyDescriptor(descriptor);
            }
            super.addContractType(contract);

            initialized = true;
        }
    }
}
