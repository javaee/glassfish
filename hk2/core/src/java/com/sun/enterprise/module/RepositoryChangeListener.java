package com.sun.enterprise.module;

import java.net.URI;

/**
 * Listener interface to listen to repository changes. Implementations of this listener
 * interface will be notified when repositories they registered to are changing.
 *
 * @author Jerome Dochez
 */
public interface RepositoryChangeListener {

    /**
     * A new libary jar file was added to the repository.
     *
     * @param location the new jar file location
     */
    public void jarAdded(URI location);

    /**
     * A library jar file was removed from the repository
     *
     * @param location of the removed file
     */
    public void jarRemoved(URI location);

    /**
     * A new module jar file was added to the repository.
     *
     * @param definition the new module definition
     */
    public void moduleAdded(ModuleDefinition definition);

    /**
     * A module file was removed from the repository
     *
     * @param definition the module definition of the removed module
     */
    public void moduleRemoved(ModuleDefinition definition);
}
