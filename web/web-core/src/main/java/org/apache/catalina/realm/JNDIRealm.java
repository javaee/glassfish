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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.CommunicationException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NameParser;
import javax.naming.Name;
import javax.naming.AuthenticationException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.Base64;

/**
 * <p>Implementation of <strong>Realm</strong> that works with a directory
 * server accessed via the Java Naming and Directory Interface (JNDI) APIs.
 * The following constraints are imposed on the data structure in the
 * underlying directory server:</p>
 * <ul>
 *
 * <li>Each user that can be authenticated is represented by an individual
 *     element in the top level <code>DirContext</code> that is accessed
 *     via the <code>connectionURL</code> property.</li>
 *
 * <li>If a socket connection can not be made to the <code>connectURL</code>
 *     an attempt will be made to use the <code>alternateURL</code> if it
 *     exists.</li>
 *
 * <li>Each user element has a distinguished name that can be formed by
 *     substituting the presented username into a pattern configured by the
 *     <code>userPattern</code> property.</li>
 *
 * <li>Alternatively, if the <code>userPattern</code> property is not
 *     specified, a unique element can be located by searching the directory
 *     context. In this case:
 *     <ul>
 *     <li>The <code>userSearch</code> pattern specifies the search filter
 *         after substitution of the username.</li>
 *     <li>The <code>userBase</code> property can be set to the element that
 *         is the base of the subtree containing users.  If not specified,
 *         the search base is the top-level context.</li>
 *     <li>The <code>userSubtree</code> property can be set to
 *         <code>true</code> if you wish to search the entire subtree of the
 *         directory context.  The default value of <code>false</code>
 *         requests a search of only the current level.</li>
 *    </ul>
 * </li>
 *
 * <li>The user may be authenticated by binding to the directory with the
 *      username and password presented. This method is used when the
 *      <code>userPassword</code> property is not specified.</li>
 *
 * <li>The user may be authenticated by retrieving the value of an attribute
 *     from the directory and comparing it explicitly with the value presented
 *     by the user. This method is used when the <code>userPassword</code>
 *     property is specified, in which case:
 *     <ul>
 *     <li>The element for this user must contain an attribute named by the
 *         <code>userPassword</code> property.
 *     <li>The value of the user password attribute is either a cleartext
 *         String, or the result of passing a cleartext String through the
 *         <code>RealmBase.digest()</code> method (using the standard digest
 *         support included in <code>RealmBase</code>).
 *     <li>The user is considered to be authenticated if the presented
 *         credentials (after being passed through
 *         <code>RealmBase.digest()</code>) are equal to the retrieved value
 *         for the user password attribute.</li>
 *     </ul></li>
 *
 * <li>Each group of users that has been assigned a particular role may be
 *     represented by an individual element in the top level
 *     <code>DirContext</code> that is accessed via the
 *     <code>connectionURL</code> property.  This element has the following
 *     characteristics:
 *     <ul>
 *     <li>The set of all possible groups of interest can be selected by a
 *         search pattern configured by the <code>roleSearch</code>
 *         property.</li>
 *     <li>The <code>roleSearch</code> pattern optionally includes pattern
 *         replacements "{0}" for the distinguished name, and/or "{1}" for
 *         the username, of the authenticated user for which roles will be
 *         retrieved.</li>
 *     <li>The <code>roleBase</code> property can be set to the element that
 *         is the base of the search for matching roles.  If not specified,
 *         the entire context will be searched.</li>
 *     <li>The <code>roleSubtree</code> property can be set to
 *         <code>true</code> if you wish to search the entire subtree of the
 *         directory context.  The default value of <code>false</code>
 *         requests a search of only the current level.</li>
 *     <li>The element includes an attribute (whose name is configured by
 *         the <code>roleName</code> property) containing the name of the
 *         role represented by this element.</li>
 *     </ul></li>
 *
 * <li>In addition, roles may be represented by the values of an attribute
 * in the user's element whose name is configured by the
 * <code>userRoleName</code> property.</li>
 *
 * <li>Note that the standard <code>&lt;security-role-ref&gt;</code> element in
 *     the web application deployment descriptor allows applications to refer
 *     to roles programmatically by names other than those used in the
 *     directory server itself.</li>
 * </ul>
 *
 * <p><strong>TODO</strong> - Support connection pooling (including message
 * format objects) so that <code>authenticate()</code> does not have to be
 * synchronized.</p>
 *
 * <p><strong>WARNING</strong> - There is a reported bug against the Netscape
 * provider code (com.netscape.jndi.ldap.LdapContextFactory) with respect to
 * successfully authenticated a non-existing user. The
 * report is here: http://nagoya.apache.org/bugzilla/show_bug.cgi?id=11210 .
 * With luck, Netscape has updated their provider code and this is not an
 * issue. </p>
 *
 * @author John Holman
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:53 $
 */

public class JNDIRealm extends RealmBase {


    // ----------------------------------------------------- Instance Variables

    /**
     *  The type of authentication to use
     */
    protected String authentication = null;

    /**
     * The connection username for the server we will contact.
     */
    protected String connectionName = null;


    /**
     * The connection password for the server we will contact.
     */
    protected String connectionPassword = null;


    /**
     * The connection URL for the server we will contact.
     */
    protected String connectionURL = null;


    /**
     * The directory context linking us to our directory server.
     */
    protected DirContext context = null;


    /**
     * The JNDI context factory used to acquire our InitialContext.  By
     * default, assumes use of an LDAP server using the standard JNDI LDAP
     * provider.
     */
    protected String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info =
        "org.apache.catalina.realm.JNDIRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "JNDIRealm";


    /**
     * The protocol that will be used in the communication with the
     * directory server.
     */
    protected String protocol = null;


    /**
     * How should we handle referrals?  Microsoft Active Directory can't handle
     * the default case, so an application authenticating against AD must
     * set referrals to "follow".
     */
    protected String referrals = null;


    /**
     * The base element for user searches.
     */
    protected String userBase = "";


    /**
     * The message format used to search for a user, with "{0}" marking
     * the spot where the username goes.
     */
    protected String userSearch = null;


    /**
     * The MessageFormat object associated with the current
     * <code>userSearch</code>.
     */
    protected MessageFormat userSearchFormat = null;


    /**
     * Should we search the entire subtree for matching users?
     */
    protected boolean userSubtree = false;


    /**
     * The attribute name used to retrieve the user password.
     */
    protected String userPassword = null;


    /**
     * The message format used to form the distinguished name of a
     * user, with "{0}" marking the spot where the specified username
     * goes.
     */
    protected String userPattern = null;


    /**
     * The MessageFormat object associated with the current
     * <code>userPattern</code>.
     */
    protected MessageFormat userPatternFormat = null;


    /**
     * The base element for role searches.
     */
    protected String roleBase = "";


    /**
     * The MessageFormat object associated with the current
     * <code>roleSearch</code>.
     */
    protected MessageFormat roleFormat = null;


    /**
     * The name of an attribute in the user's entry containing
     * roles for that user
     */
    protected String userRoleName = null;


    /**
     * The name of the attribute containing roles held elsewhere
     */
    protected String roleName = null;


    /**
     * The message format used to select roles for a user, with "{0}" marking
     * the spot where the distinguished name of the user goes.
     */
    protected String roleSearch = null;


    /**
     * Should we search the entire subtree for matching memberships?
     */
    protected boolean roleSubtree = false;

    /**
     * An alternate URL, to which, we should connect if connectionURL fails.
     */
    protected String alternateURL;

    /**
     * The number of connection attempts.  If greater than zero we use the
     * alternate url.
     */
    protected int connectionAttempt = 0;

    // ------------------------------------------------------------- Properties

    /**
     * Return the type of authentication to use.
     */
    public String getAuthentication() {

        return authentication;

    }

    /**
     * Set the type of authentication to use.
     *
     * @param authentication The authentication
     */
    public void setAuthentication(String authentication) {

        this.authentication = authentication;

    }

    /**
     * Return the connection username for this Realm.
     */
    public String getConnectionName() {

        return (this.connectionName);

    }


    /**
     * Set the connection username for this Realm.
     *
     * @param connectionName The new connection username
     */
    public void setConnectionName(String connectionName) {

        this.connectionName = connectionName;

    }


    /**
     * Return the connection password for this Realm.
     */
    public String getConnectionPassword() {

        return (this.connectionPassword);

    }


    /**
     * Set the connection password for this Realm.
     *
     * @param connectionPassword The new connection password
     */
    public void setConnectionPassword(String connectionPassword) {

        this.connectionPassword = connectionPassword;

    }


    /**
     * Return the connection URL for this Realm.
     */
    public String getConnectionURL() {

        return (this.connectionURL);

    }


    /**
     * Set the connection URL for this Realm.
     *
     * @param connectionURL The new connection URL
     */
    public void setConnectionURL(String connectionURL) {

        this.connectionURL = connectionURL;

    }


    /**
     * Return the JNDI context factory for this Realm.
     */
    public String getContextFactory() {

        return (this.contextFactory);

    }


    /**
     * Set the JNDI context factory for this Realm.
     *
     * @param contextFactory The new context factory
     */
    public void setContextFactory(String contextFactory) {

        this.contextFactory = contextFactory;

    }


    /**
     * Return the protocol to be used.
     */
    public String getProtocol() {

        return protocol;

    }

    /**
     * Set the protocol for this Realm.
     *
     * @param protocol The new protocol.
     */
    public void setProtocol(String protocol) {

        this.protocol = protocol;

    }


    /**
     * Returns the current settings for handling JNDI referrals.
     */
    public String getReferrals () {
        return referrals;
    }


    /**
     * How do we handle JNDI referrals? ignore, follow, or throw
     * (see javax.naming.Context.REFERRAL for more information).
     */
    public void setReferrals (String referrals) {
        this.referrals = referrals;
    }


    /**
     * Return the base element for user searches.
     */
    public String getUserBase() {

        return (this.userBase);

    }


    /**
     * Set the base element for user searches.
     *
     * @param userBase The new base element
     */
    public void setUserBase(String userBase) {

        this.userBase = userBase;

    }


    /**
     * Return the message format pattern for selecting users in this Realm.
     */
    public String getUserSearch() {

        return (this.userSearch);

    }


    /**
     * Set the message format pattern for selecting users in this Realm.
     *
     * @param userSearch The new user search pattern
     */
    public void setUserSearch(String userSearch) {

        this.userSearch = userSearch;
        if (userSearch == null)
            userSearchFormat = null;
        else
            userSearchFormat = new MessageFormat(userSearch);

    }


    /**
     * Return the "search subtree for users" flag.
     */
    public boolean getUserSubtree() {

        return (this.userSubtree);

    }


    /**
     * Set the "search subtree for users" flag.
     *
     * @param userSubtree The new search flag
     */
    public void setUserSubtree(boolean userSubtree) {

        this.userSubtree = userSubtree;

    }


    /**
     * Return the user role name attribute name for this Realm.
     */
    public String getUserRoleName() {

        return userRoleName;
    }


    /**
     * Set the user role name attribute name for this Realm.
     *
     * @param userRoleName The new userRole name attribute name
     */
    public void setUserRoleName(String userRoleName) {

        this.userRoleName = userRoleName;

    }


    /**
     * Return the base element for role searches.
     */
    public String getRoleBase() {

        return (this.roleBase);

    }


    /**
     * Set the base element for role searches.
     *
     * @param roleBase The new base element
     */
    public void setRoleBase(String roleBase) {

        this.roleBase = roleBase;

    }


    /**
     * Return the role name attribute name for this Realm.
     */
    public String getRoleName() {

        return (this.roleName);

    }


    /**
     * Set the role name attribute name for this Realm.
     *
     * @param roleName The new role name attribute name
     */
    public void setRoleName(String roleName) {

        this.roleName = roleName;

    }


    /**
     * Return the message format pattern for selecting roles in this Realm.
     */
    public String getRoleSearch() {

        return (this.roleSearch);

    }


    /**
     * Set the message format pattern for selecting roles in this Realm.
     *
     * @param roleSearch The new role search pattern
     */
    public void setRoleSearch(String roleSearch) {

        this.roleSearch = roleSearch;
        if (roleSearch == null)
            roleFormat = null;
        else
            roleFormat = new MessageFormat(roleSearch);

    }


    /**
     * Return the "search subtree for roles" flag.
     */
    public boolean getRoleSubtree() {

        return (this.roleSubtree);

    }


    /**
     * Set the "search subtree for roles" flag.
     *
     * @param roleSubtree The new search flag
     */
    public void setRoleSubtree(boolean roleSubtree) {

        this.roleSubtree = roleSubtree;

    }


    /**
     * Return the password attribute used to retrieve the user password.
     */
    public String getUserPassword() {

        return (this.userPassword);

    }


    /**
     * Set the password attribute used to retrieve the user password.
     *
     * @param userPassword The new password attribute
     */
    public void setUserPassword(String userPassword) {

        this.userPassword = userPassword;

    }


    /**
     * Return the message format pattern for selecting users in this Realm.
     */
    public String getUserPattern() {

        return (this.userPattern);

    }


    /**
     * Set the message format pattern for selecting users in this Realm.
     *
     * @param userPattern The new user pattern
     */
    public void setUserPattern(String userPattern) {

        this.userPattern = userPattern;
        if (userPattern == null)
            userPatternFormat = null;
        else
            userPatternFormat = new MessageFormat(userPattern);

    }

    /**
     * Getter for property alternateURL.
     *
     * @return Value of property alternateURL.
     */
    public String getAlternateURL() {

        return this.alternateURL;

    }

    /**
     * Setter for property alternateURL.
     *
     * @param alternateURL New value of property alternateURL.
     */
    public void setAlternateURL(String alternateURL) {

        this.alternateURL = alternateURL;

    }


    // ---------------------------------------------------------- Realm Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * If there are any errors with the JDBC connection, executing
     * the query or anything we return null (don't authenticate). This
     * event is also logged, and the connection will be closed so that
     * a subsequent request will automatically re-open it.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        DirContext context = null;
        Principal principal = null;

        try {

            // Ensure that we have a directory context available
            context = open();

            // Occassionally the directory context will timeout.  Try one more
            // time before giving up.
            try {

                // Authenticate the specified username if possible
                principal = authenticate(context, username, credentials);

            } catch (CommunicationException e) {


                // If contains the work closed. Then assume socket is closed.
                // If message is null, assume the worst and allow the
                // connection to be closed.
                if (e.getMessage()!=null &&
                    e.getMessage().indexOf("closed") < 0)
                    throw(e);

                // log the exception so we know it's there.
                log(sm.getString("jndiRealm.exception"), e);

                // close the connection so we know it will be reopened.
                if (context != null)
                    close(context);

                // open a new directory context.
                context = open();

                // Try the authentication again.
                principal = authenticate(context, username, credentials);

            }


            // Release this context
            release(context);

            // Return the authenticated Principal (if any)
            return (principal);

        } catch (NamingException e) {

            // Log the problem for posterity
            log(sm.getString("jndiRealm.exception"), e);

            // Close the connection so that it gets reopened next time
            if (context != null)
                close(context);

            // Return "not authenticated" for this request
            return (null);

        }

    }


    // -------------------------------------------------------- Package Methods


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param context The directory context
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     *
     * @exception NamingException if a directory server error occurs
     */
    public synchronized Principal authenticate(DirContext context,
                                               String username,
                                               String credentials)
        throws NamingException {

        if (username == null || username.equals("")
            || credentials == null || credentials.equals(""))
            return (null);

        // Retrieve user information
        User user = getUser(context, username);
        if (user == null)
            return (null);

        // Check the user's credentials
        if (!checkCredentials(context, user, credentials))
            return (null);

        // Search for additional roles
        List roles = getRoles(context, user);

        // Create and return a suitable Principal for this user
        return (new GenericPrincipal(this, username, credentials, roles));

    }


    /**
     * Return a User object containing information about the user
     * with the specified username, if found in the directory;
     * otherwise return <code>null</code>.
     *
     * If the <code>userPassword</code> configuration attribute is
     * specified, the value of that attribute is retrieved from the
     * user's directory entry. If the <code>userRoleName</code>
     * configuration attribute is specified, all values of that
     * attribute are retrieved from the directory entry.
     *
     * @param context The directory context
     * @param username Username to be looked up
     *
     * @exception NamingException if a directory server error occurs
     */
    protected User getUser(DirContext context, String username)
        throws NamingException {

        User user = null;

        // Get attributes to retrieve from user entry
        ArrayList list = new ArrayList();
        if (userPassword != null)
            list.add(userPassword);
        if (userRoleName != null)
            list.add(userRoleName);
        String[] attrIds = new String[list.size()];
        list.toArray(attrIds);

        // Use pattern or search for user entry
        if (userPatternFormat != null) {
            user = getUserByPattern(context, username, attrIds);
        } else {
            user = getUserBySearch(context, username, attrIds);
        }

        return user;
    }


    /**
     * Use the <code>UserPattern</code> configuration attribute to
     * locate the directory entry for the user with the specified
     * username and return a User object; otherwise return
     * <code>null</code>.
     *
     * @param context The directory context
     * @param username The username
     * @param attrIds String[]containing names of attributes to
     * retrieve.
     *
     * @exception NamingException if a directory server error occurs
     */
    protected User getUserByPattern(DirContext context,
                                              String username,
                                              String[] attrIds)
        throws NamingException {

        if (debug >= 2)
            log("lookupUser(" + username + ")");

        if (username == null || userPatternFormat == null)
            return (null);

        // Form the dn from the user pattern
        String dn = userPatternFormat.format(new String[] { username });
        if (debug >= 3) {
            log("  dn=" + dn);
        }

        // Return if no attributes to retrieve
        if (attrIds == null || attrIds.length == 0)
            return new User(username, dn, null, null);

        // Get required attributes from user entry
        Attributes attrs = null;
        try {
            attrs = context.getAttributes(dn, attrIds);
        } catch (NameNotFoundException e) {
            return (null);
        }
        if (attrs == null)
            return (null);

        // Retrieve value of userPassword
        String password = null;
        if (userPassword != null)
            password = getAttributeValue(userPassword, attrs);

        // Retrieve values of userRoleName attribute
        ArrayList roles = null;
        if (userRoleName != null)
            roles = addAttributeValues(userRoleName, attrs, roles);

        return new User(username, dn, password, roles);
    }


    /**
     * Search the directory to return a User object containing
     * information about the user with the specified username, if
     * found in the directory; otherwise return <code>null</code>.
     *
     * @param context The directory context
     * @param username The username
     * @param attrIds String[]containing names of attributes to retrieve.
     *
     * @exception NamingException if a directory server error occurs
     */
    protected User getUserBySearch(DirContext context,
                                           String username,
                                           String[] attrIds)
        throws NamingException {

        if (username == null || userSearchFormat == null)
            return (null);

        // Form the search filter
        String filter = userSearchFormat.format(new String[] { username });

        // Set up the search controls
        SearchControls constraints = new SearchControls();

        if (userSubtree) {
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        }
        else {
            constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        }

        // Specify the attributes to be retrieved
        if (attrIds == null)
            attrIds = new String[0];
        constraints.setReturningAttributes(attrIds);

        if (debug > 3) {
            log("  Searching for " + username);
            log("  base: " + userBase + "  filter: " + filter);
        }

        NamingEnumeration results =
            context.search(userBase, filter, constraints);


        // Fail if no entries found
        if (results == null || !results.hasMore()) {
            if (debug > 2) {
                log("  username not found");
            }
            return (null);
        }

        // Get result for the first entry found
        SearchResult result = (SearchResult)results.next();

        // Check no further entries were found
        if (results.hasMore()) {
            log("username " + username + " has multiple entries");
            return (null);
        }

        // Get the entry's distinguished name
        NameParser parser = context.getNameParser("");
        Name contextName = parser.parse(context.getNameInNamespace());
        Name baseName = parser.parse(userBase);
        Name entryName = parser.parse(result.getName());
        Name name = contextName.addAll(baseName);
        name = name.addAll(entryName);
        String dn = name.toString();

        if (debug > 2)
            log("  entry found for " + username + " with dn " + dn);

        // Get the entry's attributes
        Attributes attrs = result.getAttributes();
        if (attrs == null)
            return null;

        // Retrieve value of userPassword
        String password = null;
        if (userPassword != null)
            password = getAttributeValue(userPassword, attrs);

        // Retrieve values of userRoleName attribute
        ArrayList roles = null;
        if (userRoleName != null)
            roles = addAttributeValues(userRoleName, attrs, roles);

        return new User(username, dn, password, roles);
    }


    /**
     * Check whether the given User can be authenticated with the
     * given credentials. If the <code>userPassword</code>
     * configuration attribute is specified, the credentials
     * previously retrieved from the directory are compared explicitly
     * with those presented by the user. Otherwise the presented
     * credentials are checked by binding to the directory as the
     * user.
     *
     * @param context The directory context
     * @param user The User to be authenticated
     * @param credentials The credentials presented by the user
     *
     * @exception NamingException if a directory server error occurs
     */
    protected boolean checkCredentials(DirContext context,
                                     User user,
                                     String credentials)
         throws NamingException {

         boolean validated = false;

         if (userPassword == null) {
             validated = bindAsUser(context, user, credentials);
         } else {
             validated = compareCredentials(context, user, credentials);
         }

         if (debug >= 2) {
             if (validated) {
                 log(sm.getString("jndiRealm.authenticateSuccess",
                                  user.username));
             } else {
                 log(sm.getString("jndiRealm.authenticateFailure",
                                  user.username));
             }
         }
         return (validated);
     }



    /**
     * Check whether the credentials presented by the user match those
     * retrieved from the directory.
     *
     * @param context The directory context
     * @param user The User to be authenticated
     * @param credentials Authentication credentials
     *
     * @exception NamingException if a directory server error occurs
     */
    protected boolean compareCredentials(DirContext context,
                                         User info,
                                         String credentials)
        throws NamingException {

        if (info == null || credentials == null)
            return (false);

        String password = info.password;
        if (password == null)
            return (false);

        // Validate the credentials specified by the user
        if (debug >= 3)
            log("  validating credentials");

        boolean validated = false;
        if (hasMessageDigest()) {
            // iPlanet support if the values starts with {SHA1}
            // The string is in a format compatible with Base64.encode not
            // the Hex encoding of the parent class.
            if (password.startsWith("{SHA}")) {
                /* sync since super.digest() does this same thing */
                synchronized (this) {
                    password = password.substring(5);
                    md.reset();
                    md.update(credentials.getBytes());
                    String digestedPassword = new String(Base64.encode(md.digest()));
                    validated = password.equals(digestedPassword);
                }
            } else {
                // Hex hashes should be compared case-insensitive
                validated = (digest(credentials).equalsIgnoreCase(password));
            }
        } else
            validated = (digest(credentials).equals(password));
        return (validated);

    }



    /**
     * Check credentials by binding to the directory as the user
     *
     * @param context The directory context
     * @param user The User to be authenticated
     * @param credentials Authentication credentials
     *
     * @exception NamingException if a directory server error occurs
     */
     protected boolean bindAsUser(DirContext context,
                                  User user,
                                  String credentials)
         throws NamingException {
         Attributes attr;

         if (credentials == null || user == null)
             return (false);

         String dn = user.dn;
         if (dn == null)
             return (false);

         // Validate the credentials specified by the user
         if (debug >= 3) {
             log("  validating credentials by binding as the user");
        }

        // Set up security environment to bind as the user
        context.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
        context.addToEnvironment(Context.SECURITY_CREDENTIALS, credentials);

        // Elicit an LDAP bind operation
        boolean validated = false;
        try {
            if (debug > 2) {
                log("  binding as "  + dn);
            }
            attr = context.getAttributes("", null);
            validated = true;
        }
        catch (AuthenticationException e) {
            if (debug > 2) {
                log("  bind attempt failed");
            }
        }

        // Restore the original security environment
        if (connectionName != null) {
            context.addToEnvironment(Context.SECURITY_PRINCIPAL,
                                     connectionName);
        } else {
            context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
        }

        if (connectionPassword != null) {
            context.addToEnvironment(Context.SECURITY_CREDENTIALS,
                                     connectionPassword);
        }
        else {
            context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
        }

        return (validated);
     }


    /**
     * Return a List of roles associated with the given User.  Any
     * roles present in the user's directory entry are supplemented by
     * a directory search. If no roles are associated with this user,
     * a zero-length List is returned.
     *
     * @param context The directory context we are searching
     * @param user The User to be checked
     *
     * @exception NamingException if a directory server error occurs
     */
    protected List getRoles(DirContext context, User user)
        throws NamingException {

        if (user == null)
            return (null);

        String dn = user.dn;
        String username = user.username;

        if (dn == null || username == null)
            return (null);

        if (debug >= 2)
            log("  getRoles(" + dn + ")");

        // Start with roles retrieved from the user entry
        ArrayList list = user.roles;
        if (list == null) {
            list = new ArrayList();
        }

        // Are we configured to do role searches?
        if ((roleFormat == null) || (roleName == null))
            return (list);

        // Set up parameters for an appropriate search
        String filter = roleFormat.format(new String[] { dn, username });
        SearchControls controls = new SearchControls();
        if (roleSubtree)
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        else
            controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        controls.setReturningAttributes(new String[] {roleName});

        // Perform the configured search and process the results
        if (debug >= 3) {
            log("  Searching role base '" + roleBase + "' for attribute '" +
                roleName + "'");
            log("  With filter expression '" + filter + "'");
        }
        NamingEnumeration results =
            context.search(roleBase, filter, controls);
        if (results == null)
            return (list);  // Should never happen, but just in case ...
        while (results.hasMore()) {
            SearchResult result = (SearchResult) results.next();
            Attributes attrs = result.getAttributes();
            if (attrs == null)
                continue;
            list = addAttributeValues(roleName, attrs, list);
        }


        if (debug >= 2) {
            if (list != null) {
                log("  Returning " + list.size() + " roles");
                for (int i=0; i<list.size(); i++)
                    log(  "  Found role " + list.get(i));
            } else {
                log("  getRoles about to return null ");
            }
        }

        return (list);
    }


    /**
     * Return a String representing the value of the specified attribute.
     *
     * @param attrId Attribute name
     * @param attrs Attributes containing the required value
     *
     * @exception NamingException if a directory server error occurs
     */
    private String getAttributeValue(String attrId, Attributes attrs)
        throws NamingException {

        if (debug >= 3)
            log("  retrieving attribute " + attrId);

        if (attrId == null || attrs == null)
            return null;

        Attribute attr = attrs.get(attrId);
        if (attr == null)
            return (null);
        Object value = attr.get();
        if (value == null)
            return (null);
        String valueString = null;
        if (value instanceof byte[])
            valueString = new String((byte[]) value);
        else
            valueString = value.toString();

        return valueString;
    }



    /**
     * Add values of a specified attribute to a list
     *
     * @param attrId Attribute name
     * @param attrs Attributes containing the new values
     * @param values ArrayList containing values found so far
     *
     * @exception NamingException if a directory server error occurs
     */
    private ArrayList addAttributeValues(String attrId,
                                         Attributes attrs,
                                         ArrayList values)
        throws NamingException{

        if (debug >= 3)
            log("  retrieving values for attribute " + attrId);
        if (attrId == null || attrs == null)
            return values;
        if (values == null)
            values = new ArrayList();
        Attribute attr = attrs.get(attrId);
        if (attr == null)
            return (values);
        NamingEnumeration e = attr.getAll();
        while(e.hasMore()) {
            String value = (String)e.next();
            values.add(value);
        }
        return values;
    }


    /**
     * Close any open connection to the directory server for this Realm.
     *
     * @param context The directory context to be closed
     */
    protected void close(DirContext context) {

        // Do nothing if there is no opened connection
        if (context == null)
            return;

        // Close our opened connection
        try {
            if (debug >= 1)
                log("Closing directory context");
            context.close();
        } catch (NamingException e) {
            log(sm.getString("jndiRealm.close"), e);
        }
        this.context = null;

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

        return (null);

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (null);

    }



    /**
     * Open (if necessary) and return a connection to the configured
     * directory server for this Realm.
     *
     * @exception NamingException if a directory server error occurs
     */
    protected DirContext open() throws NamingException {

        // Do nothing if there is a directory server connection already open
        if (context != null)
            return (context);

        try {

            // Ensure that we have a directory context available
            context = new InitialDirContext(getDirectoryContextEnvironment());

        } catch (NamingException e) {

            connectionAttempt = 1;

            // log the first exception.
            log(sm.getString("jndiRealm.exception"), e);

            // Try connecting to the alternate url.
            context = new InitialDirContext(getDirectoryContextEnvironment());

        } finally {

            // reset it in case the connection times out.
            // the primary may come back.
            connectionAttempt = 0;

        }

        return (context);

    }

    /**
     * Create our directory context configuration.
     *
     * @return java.util.Hashtable the configuration for the directory context.
     */
    protected Hashtable getDirectoryContextEnvironment() {

        Hashtable env = new Hashtable();

        // Configure our directory context environment.
        if (debug >= 1 && connectionAttempt == 0)
            log("Connecting to URL " + connectionURL);
        else if (debug >= 1 && connectionAttempt > 0)
            log("Connecting to URL " + alternateURL);
        env.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        if (connectionName != null)
            env.put(Context.SECURITY_PRINCIPAL, connectionName);
        if (connectionPassword != null)
            env.put(Context.SECURITY_CREDENTIALS, connectionPassword);
        if (connectionURL != null && connectionAttempt == 0)
            env.put(Context.PROVIDER_URL, connectionURL);
        else if (alternateURL != null && connectionAttempt > 0)
            env.put(Context.PROVIDER_URL, alternateURL);
        if (authentication != null)
            env.put(Context.SECURITY_AUTHENTICATION, authentication);
        if (protocol != null)
            env.put(Context.SECURITY_PROTOCOL, protocol);
        if (referrals != null)
            env.put(Context.REFERRAL, referrals);

        return env;

    }


    /**
     * Release our use of this connection so that it can be recycled.
     *
     * @param context The directory context to release
     */
    protected void release(DirContext context) {

        ; // NO-OP since we are not pooling anything

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public void start() throws LifecycleException {

        // Validate that we can open our connection
        try {
            open();
        } catch (NamingException e) {
            throw new LifecycleException(sm.getString("jndiRealm.open"), e);
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
    public void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

        // Close any open directory server connection
        close(this.context);

    }


}

// ------------------------------------------------------ Private Classes

/**
 * A private class representing a User
 */
class User {
    String username = null;
    String dn = null;
    String password = null;
    ArrayList roles = null;


    User(String username,
             String dn,
             String password,
             ArrayList roles) {
        this.username = username;
        this.dn = dn;
        this.password = password;
        this.roles = roles;
    }

}
