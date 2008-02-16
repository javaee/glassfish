

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


import java.util.Iterator;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;


/**
 * <p>Convenience base class for {@link User} implementations.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:12 $
 * @since 4.1
 */

public abstract class AbstractUser implements User {


    // ----------------------------------------------------- Instance Variables


    /**
     * The full name of this user.
     */
    protected String fullName = null;


    /**
     * The logon password of this user.
     */
    protected String password = null;


    /**
     * The logon username of this user.
     */
    protected String username = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the full name of this user.
     */
    public String getFullName() {

        return (this.fullName);

    }


    /**
     * Set the full name of this user.
     *
     * @param fullName The new full name
     */
    public void setFullName(String fullName) {

        this.fullName = fullName;

    }


    /**
     * Return the set of {@link Group}s to which this user belongs.
     */
    public abstract Iterator getGroups();


    /**
     * Return the logon password of this user, optionally prefixed with the
     * identifier of an encoding scheme surrounded by curly braces, such as
     * <code>{md5}xxxxx</code>.
     */
    public String getPassword() {

        return (this.password);

    }


    /**
     * Set the logon password of this user, optionally prefixed with the
     * identifier of an encoding scheme surrounded by curly braces, such as
     * <code>{md5}xxxxx</code>.
     *
     * @param password The new logon password
     */
    public void setPassword(String password) {

        this.password = password;

    }


    /**
     * Return the set of {@link Role}s assigned specifically to this user.
     */
    public abstract Iterator getRoles();


    /**
     * Return the logon username of this user, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getUsername() {

        return (this.username);

    }


    /**
     * Set the logon username of this user, which must be unique within
     * the scope of a {@link UserDatabase}.
     *
     * @param username The new logon username
     */
    public void setUsername(String username) {

        this.username = username;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Group} to those this user belongs to.
     *
     * @param group The new group
     */
    public abstract void addGroup(Group group);


    /**
     * Add a new {@link Role} to those assigned specifically to this user.
     *
     * @param role The new role
     */
    public abstract void addRole(Role role);


    /**
     * Is this user in the specified {@link Group}?
     *
     * @param group The group to check
     */
    public abstract boolean isInGroup(Group group);


    /**
     * Is this user specifically assigned the specified {@link Role}?  This
     * method does <strong>NOT</strong> check for roles inherited based on
     * {@link Group} membership.
     *
     * @param role The role to check
     */
    public abstract boolean isInRole(Role role);


    /**
     * Remove a {@link Group} from those this user belongs to.
     *
     * @param group The old group
     */
    public abstract void removeGroup(Group group);


    /**
     * Remove all {@link Group}s from those this user belongs to.
     */
    public abstract void removeGroups();


    /**
     * Remove a {@link Role} from those assigned to this user.
     *
     * @param role The old role
     */
    public abstract void removeRole(Role role);


    /**
     * Remove all {@link Role}s from those assigned to this user.
     */
    public abstract void removeRoles();


    // ------------------------------------------------------ Principal Methods


    /**
     * Make the principal name the same as the group name.
     */
    public String getName() {

        return (getUsername());

    }


}
