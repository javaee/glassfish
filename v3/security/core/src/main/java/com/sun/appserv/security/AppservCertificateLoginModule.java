/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.appserv.security;

import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.web.integration.PrincipalGroupFactory;
import com.sun.logging.LogDomains;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.x500.X500Principal;
import org.glassfish.security.common.Group;

/**
 * Abstract base class for certificate-based login modules.
 *
 * <P>Subclasses need to implement the authenticateUser() method and later
 * call commitUserAuthentication().
 *
 */
public abstract class AppservCertificateLoginModule implements LoginModule {

    private Subject subject;
    /**
     * State shared with other login modules.
     */
    protected Map<String, ?> _sharedState;
    /**
     * Options configured for this LoginModule.
     */
    protected Map<String, ?> _options;
    /**
     * Logger.
     */
    protected final Logger _logger =
            LogDomains.getLogger(AppservCertificateLoginModule.class, LogDomains.SECURITY_LOGGER);
    private CallbackHandler callbackHandler;
    private boolean success = false;
    private String[] groups = null;
    private boolean commitsuccess = false;
    private X509Certificate[] certs = null;
    private X500Principal x500Principal;
    private String appName = null;

    public final void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this._sharedState = sharedState;
        this._options = options;
        this.callbackHandler = callbackHandler;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Login module initialized: "
                    + this.getClass().toString());
        }
    }

    public final boolean login() throws LoginException {
        //Extract the certificates from the subject.
        extractCredentials();

        // Delegate the actual authentication to subclass.
        authenticateUser();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JAAS login complete.");
        }
        return true;
    }

    public final boolean commit() throws LoginException {
        if (!success || groups == null) {
            return false;
        }
        Set<Principal> principalSet = subject.getPrincipals();
        for (int i = 0; i < groups.length; i++) {
            if (groups[i] != null) {
                Group g = PrincipalGroupFactory.getGroupInstance(groups[i], CertificateRealm.AUTH_TYPE);
                principalSet.add(g);
            }
            groups[i] = null;
        }
        groups = null;
        commitsuccess = true;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JAAS authentication committed.");
        }
        return true;
    }

    final public boolean abort() throws LoginException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JAAS authentication aborted.");
        }

        if (!success) {
            return false;
        } else if (success && !commitsuccess) {
            // login succeeded but overall authentication failed
            success = false;
            for (int i = 0; i < groups.length; i++) {
                groups[i] = null;
            }
            groups = null;
            if (certs != null) {
                for (int i = 0; i < certs.length; i++) {
                    certs[i] = null;
                }
                certs = null;
            }
            x500Principal = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;

    }

    final public boolean logout() throws LoginException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "JAAS logout for: " + subject.toString());
        }

        subject.getPrincipals().clear();
        subject.getPublicCredentials().clear();
        subject.getPrivateCredentials().clear();

        success = false;
        commitsuccess = false;
        if (groups != null) {
            for (int i = 0; i < groups.length; i++) {
                groups[i] = null;
            }
            groups = null;
        }
        if (certs != null) {
            for (int i = 0; i < certs.length; i++) {
                certs[i] = null;
            }
            certs = null;
        }
        x500Principal = null;
        return true;
    }

    private final void extractCredentials() throws LoginException {
        // Certificates are available as a List object in the Public Credentials.
        Set<List> creds = subject.getPublicCredentials(List.class);
        Iterator<List> itr = creds.iterator();
        if (!itr.hasNext()) {
            success = false;
            throw new LoginException("No Certificate Credential found.");
        }
        List certCred = itr.next();
        if (certCred == null || certCred.isEmpty()) {
            success = false;
            throw new LoginException("No Certificate(s) found.");
        }
        try {
            certs = (X509Certificate[]) certCred.toArray();
        } catch (Exception ex) {
            throw (LoginException) new LoginException("No Certificate(s) found.").initCause(ex);
        }
        x500Principal = certs[0].getSubjectX500Principal();

        // Callback to get the application name.
        CertificateRealm.AppContextCallback appContext = new CertificateRealm.AppContextCallback();
        try {
            callbackHandler.handle(new Callback[]{appContext});
            appName = appContext.getAppName();
        } catch (Exception ex) {
        }
    }

    /**
     *
     * <P>This is a convenience method which can be used by subclasses
     *
     * <P>Note that this method is called after the authentication
     * has succeeded. If authentication failed do not call this method.
     *
     * Global instance field succeeded is set to true by this method.
     *
     * @param groups String array of group memberships for user (could be
     *     empty).
     */
    protected final void commitUserAuthentication(final String[] groups) {
        this.groups = groups;
        this.success = true;
    }

    /**
     * Perform authentication decision.
     *
     * Method returns silently on success and returns a LoginException
     * on failure.
     *
     * <p>Must be overridden to add custom functionality.
     * @throws LoginException on authentication failure.
     *
     */
    protected abstract void authenticateUser() throws LoginException;

    /**
     * Get the application name.
     *
     * <p> This may be useful when a single LoginModule has to handle
     * multiple applications that use certificates.
     *
     * @return the application name. Non-null only for web container.
     */
    protected String getAppName() {
        return appName;
    }

    /**
     * Get the certificate chain presented by the client.
     *
     * @return the certificate chain from the client.
     */
    protected X509Certificate[] getCerts() {
        return certs;
    }

    /**
     * Returns the subject (subject distinguished name) value from the
     * first certificate, in the client certificate chain, as an
     * <code>X500Principal</code>. If the subject value is empty, then
     * the <code>getName()</code> method of the returned
     * <code>X500Principal</code> object returns an empty string ("").
     *
     * @return an <code>X500Principal</code> representing the subject
     *		distinguished name from thr first certificate, in the
     *          client certificate chain;
     */
    protected X500Principal getX500Principal() {
        return x500Principal;
    }

    /**
     * Return the subject being authenticated.
     *
     * @return the subject being authenticated.
     */
    protected Subject getSubject() {
        return subject;
    }
}
