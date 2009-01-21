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

import java.security.Principal;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.*;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.util.StringManager;
import org.apache.tomcat.util.digester.Digester;

/**
 * Simple implementation of <b>Realm</b> that reads an XML file to configure
 * the valid users, passwords, and roles.  The file format (and default file
 * location) are identical to those currently supported by Tomcat 3.X.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong>: It is assumed that the in-memory
 * collection representing our defined users (and their roles) is initialized
 * at application startup and never modified again.  Therefore, no thread
 * synchronization is performed around accesses to the principals collection.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2006/10/03 18:40:59 $
 */

public class MemoryRealm  extends RealmBase {

    private static Logger log = Logger.getLogger(MemoryRealm.class.getName());

    // ----------------------------------------------------- Instance Variables


    /**
     * The Digester we will use to process in-memory database files.
     */
    private static Digester digester = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected final String info =
        "org.apache.catalina.realm.MemoryRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */

    protected static final String name = "MemoryRealm";


    /**
     * The pathname (absolute or relative to Catalina's current working
     * directory) of the XML file containing our database information.
     */
    private String pathname = "conf/tomcat-users.xml";


    /**
     * The set of valid Principals for this Realm, keyed by user name.
     */
    private HashMap principals = new HashMap();


    /**
     * The string manager for this package.
     */
    private static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started?
     */
    private boolean started = false;


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
     * Return the pathname of our XML file containing user definitions.
     */
    public String getPathname() {

        return pathname;

    }


    /**
     * Set the pathname of our XML file containing user definitions.  If a
     * relative pathname is specified, it is resolved against "catalina.base".
     *
     * @param pathname The new pathname
     */
    public void setPathname(String pathname) {

        this.pathname = pathname;

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

        GenericPrincipal principal =
            (GenericPrincipal) principals.get(username);

        boolean validated = false;
        if (principal != null) {
            if (hasMessageDigest()) {
                // Hex hashes should be compared case-insensitive
                validated = (digest(credentials)
                             .equalsIgnoreCase(principal.getPassword()));
            } else {
                validated =
                    (digest(credentials).equals(principal.getPassword()));
            }
        }

        if (validated) {
            if (log.isLoggable(Level.FINE))
                log.fine(sm.getString("memoryRealm.authenticateSuccess", username));
            return (principal);
        } else {
            if (log.isLoggable(Level.FINE))
                log.fine(sm.getString("memoryRealm.authenticateFailure", username));
            return (null);
        }

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Add a new user to the in-memory database.
     *
     * @param username User's username
     * @param password User's password (clear text)
     * @param roles Comma-delimited set of roles associated with this user
     */
    void addUser(String username, String password, String roles) {

        // Accumulate the list of roles for this user
        ArrayList list = new ArrayList();
        roles += ",";
        while (true) {
            int comma = roles.indexOf(',');
            if (comma < 0)
                break;
            String role = roles.substring(0, comma).trim();
            list.add(role);
            roles = roles.substring(comma + 1);
        }

        // Construct and cache the Principal for this user
        GenericPrincipal principal =
            new GenericPrincipal(this, username, password, list);
        principals.put(username, principal);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return a configured <code>Digester</code> to use for processing
     * the XML input file, creating a new one if necessary.
     */
    protected synchronized Digester getDigester() {

        if (digester == null) {
            digester = new Digester();
            digester.setDebug(this.debug);
            digester.setValidating(false);
            digester.addRuleSet(new MemoryRuleSet());
        }
        return (digester);

    }


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

        GenericPrincipal principal =
            (GenericPrincipal) principals.get(username);
        if (principal != null) {
            return (principal.getPassword());
        } else {
            return (null);
        }

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (Principal) principals.get(username);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public synchronized void start() throws LifecycleException {

        // Validate the existence of our database file
        File file = new File(pathname);
        if (!file.isAbsolute())
            file = new File(System.getProperty("catalina.base"), pathname);
        if (!file.exists() || !file.canRead())
            throw new LifecycleException
                (sm.getString("memoryRealm.loadExist",
                              file.getAbsolutePath()));

        // Load the contents of the database file
        if (log.isLoggable(Level.FINE))
            log.fine(sm.getString("memoryRealm.loadPath",
                                  file.getAbsolutePath()));
        Digester digester = getDigester();
        try {
            synchronized (digester) {
                digester.push(this);
                digester.parse(file);
            }
        } catch (Exception e) {
            throw new LifecycleException("memoryRealm.readXml", e);
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

        // No shutdown activities required

    }


}
