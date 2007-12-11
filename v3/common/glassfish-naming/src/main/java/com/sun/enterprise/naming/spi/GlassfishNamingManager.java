package com.sun.enterprise.naming.spi;

import org.jvnet.hk2.annotations.Contract;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Collection;

/**
 * The NamingManager provides an interface for various components to use naming
 * functionality. It provides methods for binding and unbinding environment
 * properties, resource and ejb references.
 */

@Contract
public interface GlassfishNamingManager {

    /**
     * Get the initial context.
     */

    public Context getInitialContext();

    /**
     * Publish a name in the naming service.
     *
     * @param name   Object that needs to be bound.
     * @param obj    Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    public void publishObject(String name, Object obj, boolean rebind)
            throws NamingException;

    /**
     * Publish a name in the naming service.
     *
     * @param name   Object that needs to be bound.
     * @param obj    Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    public void publishObject(Name name, Object obj, boolean rebind)
            throws NamingException;

    /**
     * This method enumerates the env properties, ejb and resource references
     * etc for a J2EE component and binds them in the component's java:comp
     * namespace.
     */
    public void bindToComponentNamespace(String appName,
                                         String componentId, Collection<? extends JNDIBinding> bindings)
            throws NamingException;


    /**
     * Remove an object from the naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    public void unpublishObject(String name) throws NamingException;

    /**
     * Remove an object from the naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    public void unpublishObject(Name name) throws NamingException;


    public void unbindObjects(String componentId) throws NamingException;

    /**
     * Recreate a context for java:comp/env or one of its sub-contexts given the
     * context name.
     */
    public Context restoreJavaCompEnvContext(String contextName)
            throws NamingException;
}