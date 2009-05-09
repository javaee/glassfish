/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.realm;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.*;

import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Realm;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.RequestUtil;
import org.apache.tomcat.util.digester.Digester;
// START SJSWS 6324431
import org.apache.catalina.core.StandardContext; 
// END SJSWS 6324431


/**
 * <p>Implementation of the JAAS <strong>LoginModule</strong> interface,
 * primarily for use in testing <code>JAASRealm</code>.  It utilizes an
 * XML-format data file of username/password/role information identical to
 * that supported by <code>org.apache.catalina.realm.MemoryRealm</code>
 * (except that digested passwords are not supported).</p>
 *
 * <p>This class recognizes the following string-valued options, which are
 * specified in the configuration file (and passed to our constructor in
 * the <code>options</code> argument:</p>
 * <ul>
 * <li><strong>debug</strong> - Set to "true" to get debugging messages
 *     generated to System.out.  The default value is <code>false</code>.</li>
 * <li><strong>pathname</strong> - Relative (to the pathname specified by the
 *     "catalina.base" system property) or absolute pahtname to the
 *     XML file containing our user information, in the format supported by
 *     {@link MemoryRealm}.  The default value matches the MemoryRealm
 *     default.</li>
 * </ul>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - This class implements
 * <code>Realm</code> only to satisfy the calling requirements of the
 * <code>GenericPrincipal</code> constructor.  It does not actually perform
 * the functionality required of a <code>Realm</code> implementation.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2006/03/12 01:27:04 $
 */

public class JAASMemoryLoginModule extends MemoryRealm implements LoginModule, Realm {
    // We need to extend MemoryRealm to avoid class cast

    private static Logger log = Logger.getLogger(
        JAASMemoryLoginModule.class.getName());

    // ----------------------------------------------------- Instance Variables


    /**
     * The callback handler responsible for answering our requests.
     */
    protected CallbackHandler callbackHandler = null;


    /**
     * Has our own <code>commit()</code> returned successfully?
     */
    protected boolean committed = false;


    /**
     * Should we log debugging messages?
     */
    protected boolean debug = false;


    /**
     * The configuration information for this <code>LoginModule</code>.
     */
    protected Map options = null;


    /**
     * The absolute or relative pathname to the XML configuration file.
     */
    protected String pathname = "conf/tomcat-users.xml";


    /**
     * The <code>Principal</code> identified by our validation, or
     * <code>null</code> if validation falied.
     */
    protected Principal principal = null;


    /**
     * The set of <code>Principals</code> loaded from our configuration file.
     */
    protected HashMap principals = new HashMap();

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The state information that is shared with other configured
     * <code>LoginModule</code> instances.
     */
    protected Map sharedState = null;


    /**
     * The subject for which we are performing authentication.
     */
    protected Subject subject = null;


    // --------------------------------------------------------- Public Methods

    /**
     * Phase 2 of authenticating a <code>Subject</code> when Phase 1
     * fails.  This method is called if the <code>LoginContext</code>
     * failed somewhere in the overall authentication chain.
     *
     * @return <code>true</code> if this method succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the abort fails
     */
    public boolean abort() throws LoginException {

        // If our authentication was not successful, just return false
        if (principal == null)
            return (false);

        // Clean up if overall authentication failed
        if (committed)
            logout();
        else {
            committed = false;
            principal = null;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Abort");
        }

        return (true);
    }


    /**
     * Phase 2 of authenticating a <code>Subject</code> when Phase 1
     * was successful.  This method is called if the <code>LoginContext</code>
     * succeeded in the overall authentication chain.
     *
     * @return <code>true</code> if the authentication succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the commit fails
     */
    public boolean commit() throws LoginException {
        if (log.isLoggable(Level.FINE)) {
            log.fine("commit " + principal);
        }

        // If authentication was not successful, just return false
        if (principal == null)
            return (false);

        // Add our Principal to the Subject if needed
        if (!subject.getPrincipals().contains(principal))
            subject.getPrincipals().add(principal);

        committed = true;
        return (true);

    }

    
    /**
     * Return the SecurityConstraints configured to guard the request URI for
     * this request, or <code>null</code> if there is no such constraint.
     *
     * @param request Request we are processing
     * @param context Context the Request is mapped to
     */
    public SecurityConstraint [] findSecurityConstraints(
            HttpRequest request, Context context) {

        ArrayList results = null;
        // Are there any defined security constraints?
        if (!context.hasConstraints()) {
            if (debug)
                log("  No applicable constraints defined");
            return (null);
        }

        // Check each defined security constraint
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        String uri = request.getDecodedRequestURI();
        String contextPath = hreq.getContextPath();
        if (contextPath.length() > 0)
            uri = uri.substring(contextPath.length());
        uri = RequestUtil.URLDecode(uri); // Before checking constraints
        String method = hreq.getMethod();
        List<SecurityConstraint> constraints = context.getConstraints();
        synchronized(constraints) {
            Iterator<SecurityConstraint> i = constraints.iterator(); 
            while (i.hasNext()) {
                SecurityConstraint constraint = i.next();
                /* SJSWS 6324431
                if (debug)
                    log("  Checking constraint '" + constraints[i] +
                        "' against " + method + " " + uri + " --> " +
                        constraints[i].included(uri, method));
                */
                // START SJSWS 6324431
                boolean caseSensitiveMapping = 
                    ((StandardContext)context).isCaseSensitiveMapping();
                if (debug) {
                    log("  Checking constraint '" + constraint +
                        "' against " + method + " " + uri + " --> " +
                        constraint.included(uri, method, 
                                            caseSensitiveMapping));
                }

                // END SJSWS 6324431
                /* SJSWS 6324431
                if (constraints[i].included(uri, method)) {
                */
                // START SJSWS 6324431
                if (constraint.included(uri, method, 
                                        caseSensitiveMapping)) {
                // END SJSWS 6324431
                    if(results == null) {
                        results = new ArrayList();
                    }
                    results.add(constraint);
                }
            }
        }

        // No applicable security constraint was found
        if (debug) {
            log("  No applicable constraint located");
        }

        if(results == null) {
            return null;
        }

        SecurityConstraint [] array = new SecurityConstraint[results.size()];
        System.arraycopy(results.toArray(), 0, array, 0, array.length);

        return array;
    }
    
    
    /**
     * Initialize this <code>LoginModule</code> with the specified
     * configuration information.
     *
     * @param subject The <code>Subject</code> to be authenticated
     * @param callbackHandler A <code>CallbackHandler</code> for communicating
     *  with the end user as necessary
     * @param sharedState State information shared with other
     *  <code>LoginModule</code> instances
     * @param options Configuration information for this specific
     *  <code>LoginModule</code> instance
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Init");
        }

        // Save configuration values
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        // Perform instance-specific initialization
        this.debug = "true".equalsIgnoreCase((String) options.get("debug"));
        if (options.get("pathname") != null)
            this.pathname = (String) options.get("pathname");

        // Load our defined Principals
        load();

    }


    /**
     * Phase 1 of authenticating a <code>Subject</code>.
     *
     * @return <code>true</code> if the authentication succeeded, or
     *  <code>false</code> if this <code>LoginModule</code> should be
     *  ignored
     *
     * @exception LoginException if the authentication fails
     */
    public boolean login() throws LoginException {

        // Set up our CallbackHandler requests
        if (callbackHandler == null)
            throw new LoginException("No CallbackHandler specified");
        Callback callbacks[] = new Callback[2];
        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);

        // Interact with the user to retrieve the username and password
        String username = null;
        String password = null;
        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            password =
                new String(((PasswordCallback) callbacks[1]).getPassword());
        } catch (IOException e) {
            throw new LoginException(e.toString());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.toString());
        }

        // Validate the username and password we have received
        principal = super.authenticate(username, password);

        if (log.isLoggable(Level.FINE)) {
            log.fine("login " + username + " " + principal);
        }

        // Report results based on success or failure
        if (principal != null) {
            return (true);
        } else {
            throw new
                FailedLoginException("Username or password is incorrect");
        }

    }


    /**
     * Log out this user.
     *
     * @return <code>true</code> in all cases because thie
     *  <code>LoginModule</code> should not be ignored
     *
     * @exception LoginException if logging out failed
     */
    public boolean logout() throws LoginException {

        subject.getPrincipals().remove(principal);
        committed = false;
        principal = null;
        return (true);

    }


    // ---------------------------------------------------------- Realm Methods
    // ------------------------------------------------------ Protected Methods


    /**
     * Load the contents of our configuration file.
     */
    protected void load() {

        // Validate the existence of our configuration file
        File file = new File(pathname);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), pathname);
        if (!file.exists() || !file.canRead()) {
            log("Cannot load configuration file " + file.getAbsolutePath());
            return;
        }

        // Load the contents of our configuration file
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addRuleSet(new MemoryRuleSet());
        try {
            digester.push(this);
            digester.parse(file);
        } catch (Exception e) {
            log("Error processing configuration file " +
                file.getAbsolutePath(), e);
            return;
        }

    }
}
