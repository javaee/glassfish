

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


package org.apache.catalina;


import java.util.Iterator;


/**
 * <p>Abstract representation of a database of {@link User}s and
 * {@link Group}s that can be maintained by an application,
 * along with definitions of corresponding {@link Role}s, and
 * referenced by a {@link Realm} for authentication and access control.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:21 $
 * @since 4.1
 */

public interface UserDatabase {


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of {@link Group}s defined in this user database.
     */
    public Iterator getGroups();


    /**
     * Return the unique global identifier of this user database.
     */
    public String getId();


    /**
     * Return the set of {@link Role}s defined in this user database.
     */
    public Iterator getRoles();


    /**
     * Return the set of {@link User}s defined in this user database.
     */
    public Iterator getUsers();


    // --------------------------------------------------------- Public Methods


    /**
     * Finalize access to this user database.
     *
     * @exception Exception if any exception is thrown during closing
     */
    public void close() throws Exception;


    /**
     * Create and return a new {@link Group} defined in this user database.
     *
     * @param groupname The group name of the new group (must be unique)
     * @param description The description of this group
     */
    public Group createGroup(String groupname, String description);


    /**
     * Create and return a new {@link Role} defined in this user database.
     *
     * @param rolename The role name of the new role (must be unique)
     * @param description The description of this role
     */
    public Role createRole(String rolename, String description);


    /**
     * Create and return a new {@link User} defined in this user database.
     *
     * @param username The logon username of the new user (must be unique)
     * @param password The logon password of the new user
     * @param fullName The full name of the new user
     */
    public User createUser(String username, String password,
                           String fullName);


    /**
     * Return the {@link Group} with the specified group name, if any;
     * otherwise return <code>null</code>.
     *
     * @param groupname Name of the group to return
     */
    public Group findGroup(String groupname);


    /**
     * Return the {@link Role} with the specified role name, if any;
     * otherwise return <code>null</code>.
     *
     * @param rolename Name of the role to return
     */
    public Role findRole(String rolename);


    /**
     * Return the {@link User} with the specified user name, if any;
     * otherwise return <code>null</code>.
     *
     * @param username Name of the user to return
     */
    public User findUser(String username);


    /**
     * Initialize access to this user database.
     *
     * @exception Exception if any exception is thrown during opening
     */
    public void open() throws Exception;


    /**
     * Remove the specified {@link Group} from this user database.
     *
     * @param group The group to be removed
     */
    public void removeGroup(Group group);


    /**
     * Remove the specified {@link Role} from this user database.
     *
     * @param role The role to be removed
     */
    public void removeRole(Role role);


    /**
     * Remove the specified {@link User} from this user database.
     *
     * @param user The user to be removed
     */
    public void removeUser(User user);


    /**
     * Save any updated information to the persistent storage location for
     * this user database.
     *
     * @exception Exception if any exception is thrown during saving
     */
    public void save() throws Exception;


}
