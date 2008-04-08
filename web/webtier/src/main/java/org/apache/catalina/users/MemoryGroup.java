

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
 * <p>Concrete implementation of {@link Group} for the
 * {@link MemoryUserDatabase} implementation of {@link UserDatabase}.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:12 $
 * @since 4.1
 */

public class MemoryGroup extends AbstractGroup {


    // ----------------------------------------------------------- Constructors


    /**
     * Package-private constructor used by the factory method in
     * {@link MemoryUserDatabase}.
     *
     * @param database The {@link MemoryUserDatabase} that owns this group
     * @param groupname Group name of this group
     * @param description Description of this group
     */
    MemoryGroup(MemoryUserDatabase database,
                String groupname, String description) {

        super();
        this.database = database;
        setGroupname(groupname);
        setDescription(description);

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The {@link MemoryUserDatabase} that owns this group.
     */
    protected MemoryUserDatabase database = null;


    /**
     * The set of {@link Role}s associated with this group.
     */
    protected ArrayList roles = new ArrayList();


    // ------------------------------------------------------------- Properties


    /**
     * Return the set of {@link Role}s assigned specifically to this group.
     */
    public Iterator getRoles() {

        synchronized (roles) {
            return (roles.iterator());
        }

    }


    /**
     * Return the {@link UserDatabase} within which this Group is defined.
     */
    public UserDatabase getUserDatabase() {

        return (this.database);

    }


    /**
     * Return the set of {@link User}s that are members of this group.
     */
    public Iterator getUsers() {

        ArrayList results = new ArrayList();
        Iterator users = database.getUsers();
        while (users.hasNext()) {
            MemoryUser user = (MemoryUser) users.next();
            if (user.isInGroup(this)) {
                results.add(user);
            }
        }
        return (results.iterator());

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Role} to those assigned specifically to this group.
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
     * Is this group specifically assigned the specified {@link Role}?
     *
     * @param role The role to check
     */
    public boolean isInRole(Role role) {

        synchronized (roles) {
            return (roles.contains(role));
        }

    }


    /**
     * Remove a {@link Role} from those assigned to this group.
     *
     * @param role The old role
     */
    public void removeRole(Role role) {

        synchronized (roles) {
            roles.remove(role);
        }

    }


    /**
     * Remove all {@link Role}s from those assigned to this group.
     */
    public void removeRoles() {

        synchronized (roles) {
            roles.clear();
        }

    }


    /**
     * <p>Return a String representation of this group in XML format.</p>
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("<group groupname=\"");
        sb.append(groupname);
        sb.append("\"");
        if (description != null) {
            sb.append(" description=\"");
            sb.append(description);
            sb.append("\"");
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
                    sb.append((String) ((Role) values.next()).getRolename());
                }
                sb.append("\"");
            }
        }
        sb.append("/>");
        return (sb.toString());

    }


}
