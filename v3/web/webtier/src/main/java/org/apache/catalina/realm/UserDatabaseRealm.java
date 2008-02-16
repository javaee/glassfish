

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.realm;


import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Engine;
import org.apache.catalina.Group;
import org.apache.catalina.Logger;
import org.apache.catalina.Realm;
import org.apache.catalina.Role;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import com.sun.org.apache.commons.digester.Digester;


/**
 * <p>Implementation of {@link Realm} that is based on an implementation of
 * {@link UserDatabase} made available through the global JNDI resources
 * configured for this instance of Catalina.  Set the <code>resourceName</code>
 * parameter to the global JNDI resources name for the configured instance
 * of <code>UserDatabase</code> that we should consult.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2006/03/12 01:27:05 $
 * @since 4.1
 */

public class UserDatabaseRealm
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>UserDatabase</code> we will use to authenticate users
     * and identify associated roles.
     */
    protected UserDatabase database = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected final String info =
        "org.apache.catalina.realm.UserDatabaseRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "UserDatabaseRealm";


    /**
     * The global JNDI name of the <code>UserDatabase</code> resource
     * we will be utilizing.
     */
    protected String resourceName = "UserDatabase";


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Realm implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return info;

    }


    /**
     * Return the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     */
    public String getResourceName() {

        return resourceName;

    }


    /**
     * Set the global JNDI name of the <code>UserDatabase</code> resource
     * we will be using.
     *
     * @param resourceName The new global JNDI name
     */
    public void setResourceName(String resourceName) {

        this.resourceName = resourceName;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        // Does a user with this username exist?
        User user = database.findUser(username);
        if (user == null) {
            return (null);
        }

        // Do the credentials specified by the user match?
        // FIXME - Update all realms to support encoded passwords
        boolean validated = false;
        if (hasMessageDigest()) {
            // Hex hashes should be compared case-insensitive
            validated = (digest(credentials)
                         .equalsIgnoreCase(user.getPassword()));
        } else {
            validated =
                (digest(credentials).equals(user.getPassword()));
        }
        if (!validated) {
            if (debug >= 2) {
                log(sm.getString("userDatabaseRealm.authenticateFailure",
                                 username));
            }
            return (null);
        }

        // Construct a GenericPrincipal that represents this user
        if (debug >= 2) {
            log(sm.getString("userDatabaseRealm.authenticateSuccess",
                             username));
        }
        ArrayList combined = new ArrayList();
        Iterator roles = user.getRoles();
        while (roles.hasNext()) {
            Role role = (Role) roles.next();
            String rolename = role.getRolename();
            if (!combined.contains(rolename)) {
                combined.add(rolename);
            }
        }
        Iterator groups = user.getGroups();
        while (groups.hasNext()) {
            Group group = (Group) groups.next();
            roles = group.getRoles();
            while (roles.hasNext()) {
                Role role = (Role) roles.next();
                String rolename = role.getRolename();
                if (!combined.contains(rolename)) {
                    combined.add(rolename);
                }
            }
        }
        return (new GenericPrincipal(this, user.getUsername(),
                                     user.getPassword(), combined));

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return a short name for this Realm implementation.
     */
    protected String getName() {

        return (this.name);

    }


    /**
     * Return the password associated with the given principal's user name.
     */
    protected String getPassword(String username) {

        return (null);

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (null);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        try {
            StandardServer server = (StandardServer) ServerFactory.getServer();
            Context context = server.getGlobalNamingContext();
            database = (UserDatabase) context.lookup(resourceName);
        } catch (Throwable e) {
            e.printStackTrace();
            log(sm.getString("userDatabaseRealm.lookup", resourceName), e);
            database = null;
        }
        if (database == null) {
            throw new LifecycleException
                (sm.getString("userDatabaseRealm.noDatabase", resourceName));
        }

        // Perform normal superclass initialization
        super.start();

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public synchronized void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

        // Release reference to our user database
        database = null;

    }


}
