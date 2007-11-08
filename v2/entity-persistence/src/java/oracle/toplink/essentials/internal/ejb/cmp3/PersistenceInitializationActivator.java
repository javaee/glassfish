package oracle.toplink.essentials.internal.ejb.cmp3;

/**
 *  Any class that calls the initialize method on the JavaSECMPInitializer should implement this interface
 *  Implementers of this interface can restrict the provider that the initializer will initialize with.
 */
public interface PersistenceInitializationActivator {

    /**
     * Return whether the given class name identifies a persistence provider that is supported by
     * this PersistenceInitializationActivator
     */
    public boolean isPersistenceProviderSupported(String providerClassName);
}
