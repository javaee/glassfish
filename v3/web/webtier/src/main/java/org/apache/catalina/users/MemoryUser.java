

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


package org.apache.catalina.users;


import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;


/**
 * <p>Concrete implementation of {@link User} for the
 * {@link MemoryUserDatabase} implementation of {@link UserDatabase}.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:13 $
 * @since 4.1
 */

public class MemoryUser extends AbstractUser {


    // ----------------------------------------------------------- Constructors


    /**
     * Package-private constructor used by the factory method in
     * {@link MemoryUserDatabase}.
     *
     * @param database The {@link MemoryUserDatabase} that owns this user
     * @param username Logon username of the new user
     * @param password Logon password of the new user
     * @param fullName Full name of the new user
     */
    MemoryUser(MemoryUserDatabase database, String username,
               String password, String fullName) {

        super();
        this.database = database;
        setUsername(username);
        setPassword(password);
        setFullName(fullName);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The {@link MemoryUserDatabase} that owns this user.
     */
    protected MemoryUserDatabase database = null;


    /**
     * The set of {@link Group}s that this user is a member of.
     */
    protected ArrayList groups = new ArrayList();


    /**
     * The set of {@link Role}s associated with this user.
     */
    protected ArrayList roles = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of {@link Group}s to which this user belongs.
     */
    public Iterator getGroups() {

        synchronized (groups) {
            return (groups.iterator());
        }

    }


    /**
     * Return the set of {@link Role}s assigned specifically to this user.
     */
    public Iterator getRoles() {

        synchronized (roles) {
            return (roles.iterator());
        }

    }


    /**
     * Return the {@link UserDatabase} within which this User is defined.
     */
    public UserDatabase getUserDatabase() {

        return (this.database);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Group} to those this user belongs to.
     *
     * @param group The new group
     */
    public void addGroup(Group group) {

        synchronized (groups) {
            if (!groups.contains(group)) {
                groups.add(group);
            }
        }

    }


    /**
     * Add a new {@link Role} to those assigned specifically to this user.
     *
     * @param role The new role
     */
    public void addRole(Role role) {

        synchronized (roles) {
            if (!roles.contains(role)) {
                roles.add(role);
            }
        }

    }


    /**
     * Is this user in the specified group?
     *
     * @param group The group to check
     */
    public boolean isInGroup(Group group) {

        synchronized (groups) {
            return (groups.contains(group));
        }

    }


    /**
     * Is this user specifically assigned the specified {@link Role}?  This
     * method does <strong>NOT</strong> check for roles inherited based on
     * {@link Group} membership.
     *
     * @param role The role to check
     */
    public boolean isInRole(Role role) {

        synchronized (roles) {
            return (roles.contains(role));
        }

    }


    /**
     * Remove a {@link Group} from those this user belongs to.
     *
     * @param group The old group
     */
    public void removeGroup(Group group) {

        synchronized (groups) {
            groups.remove(group);
        }

    }


    /**
     * Remove all {@link Group}s from those this user belongs to.
     */
    public void removeGroups() {

        synchronized (groups) {
            groups.clear();
        }

    }


    /**
     * Remove a {@link Role} from those assigned to this user.
     *
     * @param role The old role
     */
    public void removeRole(Role role) {

        synchronized (roles) {
            roles.remove(role);
        }

    }


    /**
     * Remove all {@link Role}s from those assigned to this user.
     */
    public void removeRoles() {

        synchronized (roles) {
            roles.clear();
        }

    }


    /**
     * <p>Return a String representation of this user in XML format.</p>
     *
     * <p><strong>IMPLEMENTATION NOTE</strong> - For backwards compatibility,
     * the reader that processes this entry will accept either
     * <code>username</code> or </code>name</code> for the username
     * property.</p>
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("<user username=\"");
        sb.append(username);
        sb.append("\" password=\"");
        sb.append(password);
        sb.append("\"");
        if (fullName != null) {
            sb.append(" fullName=\"");
            sb.append(fullName);
            sb.append("\"");
        }
        synchronized (groups) {
            if (groups.size() > 0) {
                sb.append(" groups=\"");
                int n = 0;
                Iterator values = groups.iterator();
                while (values.hasNext()) {
                    if (n > 0) {
                        sb.append(',');
                    }
                    n++;
                    sb.append(((Group) values.next()).getGroupname());
                }
                sb.append("\"");
            }
        }
        synchronized (roles) {
            if (roles.size() > 0) {
                sb.append(" roles=\"");
                int n = 0;
                Iterator values = roles.iterator();
                while (values.hasNext()) {
                    if (n > 0) {
                        sb.append(',');
                    }
                    n++;
                    sb.append(((Role) values.next()).getRolename());
                }
                sb.append("\"");
            }
        }
        sb.append("/>");
        return (sb.toString());

    }


}
