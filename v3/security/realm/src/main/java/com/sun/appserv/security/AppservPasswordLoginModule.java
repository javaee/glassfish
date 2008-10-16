/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 */

package com.sun.appserv.security;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import com.sun.enterprise.security.common.AppservPasswordLoginModuleInterface;
import com.sun.enterprise.security.common.Util;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author kumar.jayanti
 */
public abstract class AppservPasswordLoginModule implements LoginModule {

    private AppservPasswordLoginModuleInterface delegate = null;
    
    public AppservPasswordLoginModule() {
        Habitat habitat = Util.getDefaultHabitat();
        delegate = habitat.getByContract(AppservPasswordLoginModuleInterface.class);
        assert(delegate != null);
        delegate.setLoginModuleForAuthentication(this);
    }
    /**
     * Initialize this login module.
     *
     * @param subject - the Subject to be authenticated.
     * @param callbackHandler - a CallbackHandler for obtaining the subject
     *    username and password.
     * @param sharedState - state shared with other configured LoginModules.
     * @param options - options specified in the login Configuration for
     *    this particular LoginModule.
     *
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        delegate.initialize(subject, callbackHandler, sharedState, options);
    }

    /**
     * Perform login.
     *
     * <P>The callback handler is used to obtain authentication info
     * for the subject and a login is attempted. This PasswordLoginModule
     * expects to find a PasswordCredential in the private credentials
     * of the Subject. If not present the login fails. The callback
     * handler is ignored as it is not really relevant on the server side.
     * Finally, the authenticateUser() method is invoked.
     *
     * @returns true if login succeeds, otherwise an exception is thrown.
     * @throws LoginException Thrown if login failed, or on other problems.
     *
     */
    public boolean login() throws LoginException {
        return delegate.login();
    }

    /**
     * Commit the authentication.
     *
     * <P>Commit is called after all necessary login modules have succeeded.
     * It adds (if not present) a PrincipalImpl principal and a
     * LocalCredentials public credential to the Subject.
     *
     * @throws LoginException If commit fails.
     *
     */
    public boolean commit() throws LoginException {
        return delegate.commit();
    }

    /**
     * Abort the authentication process.
     *
     */
    public boolean abort() throws LoginException {
        return delegate.abort();
    }

    /**
     * Log out the subject.
     *
     */
    public boolean logout() throws LoginException {
        return delegate.logout();
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
    public  void commitUserAuthentication (final String[] groups) {
        delegate.commitUserAuthentication(groups);
    }
    /**
     * @return the subject being authenticated.
     * use case:
     * A custom login module could overwrite commit() method, and call getSubject()
     * to get subject being authenticated inside its commit(). Custom principal
     * then can be added to subject. By doing this,custom principal will be stored
     * in calling thread's security context and participate in following Appserver's
     * authorization.
     *
     */
    public Subject getSubject() {
      return delegate.getSubject();   
    }
    /**
     * Perform authentication decision.
     *
     * Method returns silently on success and returns a LoginException
     * on failure.
     * @throws LoginException on authentication failure.
     *
     */
    public abstract void authenticateUser() throws LoginException;
}
