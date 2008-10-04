package org.glassfish.webservices.monitoring;

import com.sun.enterprise.deployment.BundleDescriptor;

import java.security.Principal;


/**
 * This interface allows to register interest in authentication events
 * in the web service container.
 *
 * @author Jerome Dochez
 */
public interface AuthenticationListener {

    /**
     * notification that a user properly authenticated while making
     * a web service invocation.
     */
    public void authSucess(BundleDescriptor desc, Endpoint endpoint, Principal principal);

    /**
     * notification that a user authentication attempt has failed.
     * @param endpoint the endpoint selector
     * @param principal Optional principal that failed
     */
    public void authFailure(BundleDescriptor desc, Endpoint endpoint, Principal principal);
}