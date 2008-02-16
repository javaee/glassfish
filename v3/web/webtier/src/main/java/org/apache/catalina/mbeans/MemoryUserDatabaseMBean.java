

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

package org.apache.catalina.mbeans;


import java.util.ArrayList;
import java.util.Iterator;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import org.apache.catalina.Group;
import org.apache.catalina.Role;
import org.apache.catalina.User;
import org.apache.catalina.UserDatabase;
import com.sun.org.apache.commons.modeler.BaseModelMBean;
import com.sun.org.apache.commons.modeler.ManagedBean;
import com.sun.org.apache.commons.modeler.Registry;


/**
 * <p>A <strong>ModelMBean</strong> implementation for the
 * <code>org.apache.catalina.users.MemoryUserDatabase</code> component.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2007/05/05 05:32:09 $
 */

public class MemoryUserDatabaseMBean extends BaseModelMBean {


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception RuntimeOperationsException if an IllegalArgumentException
     *  occurs
     */
    public MemoryUserDatabaseMBean()
        throws MBeanException, RuntimeOperationsException {

        super();

        registry = MBeanUtils.createRegistry();
        managed = registry.findManagedBean("MemoryUserDatabase");
        managedGroup = registry.findManagedBean("Group");
        managedRole = registry.findManagedBean("Role");
        managedUser = registry.findManagedBean("User");
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>MBeanServer</code> in which we are registered.
     */
    protected MBeanServer mserver = MBeanUtils.createServer();


    /**
     * The <code>ManagedBean</code> information describing this MBean.
     */
    protected ManagedBean managed;


    /**
     * The <code>ManagedBean</code> information describing Group MBeans.
     */
    protected ManagedBean managedGroup;

    /**
     * The <code>ManagedBean</code> information describing Group MBeans.
     */
    protected ManagedBean managedRole;

    /**
     * The <code>ManagedBean</code> information describing User MBeans.
     */
    protected ManagedBean managedUser;


    // ------------------------------------------------------------- Attributes


    /**
     * Return the MBean Names of all groups defined in this database.
     */
    public String[] getGroups() {

        UserDatabase database = (UserDatabase) this.resource;
        ArrayList results = new ArrayList();
        Iterator groups = database.getGroups();
        while (groups.hasNext()) {
            Group group = (Group) groups.next();
            results.add(findGroup(group.getGroupname()));
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    /**
     * Return the MBean Names of all roles defined in this database.
     */
    public String[] getRoles() {

        UserDatabase database = (UserDatabase) this.resource;
        ArrayList results = new ArrayList();
        Iterator roles = database.getRoles();
        while (roles.hasNext()) {
            Role role = (Role) roles.next();
            results.add(findRole(role.getRolename()));
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    /**
     * Return the MBean Names of all users defined in this database.
     */
    public String[] getUsers() {

        UserDatabase database = (UserDatabase) this.resource;
        ArrayList results = new ArrayList();
        Iterator users = database.getUsers();
        while (users.hasNext()) {
            User user = (User) users.next();
            results.add(findUser(user.getUsername()));
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    // ------------------------------------------------------------- Operations


    /**
     * Create a new Group and return the corresponding MBean Name.
     *
     * @param groupname Group name of the new group
     * @param description Description of the new group
     */
    public String createGroup(String groupname, String description) {

        UserDatabase database = (UserDatabase) this.resource;
        Group group = database.createGroup(groupname, description);
        try {
            MBeanUtils.createMBean(group);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Exception creating group " + group + " MBean");
            iae.initCause(e);
            throw iae;
        }
        return (findGroup(groupname));

    }


    /**
     * Create a new Role and return the corresponding MBean Name.
     *
     * @param rolename Group name of the new group
     * @param description Description of the new group
     */
    public String createRole(String rolename, String description) {

        UserDatabase database = (UserDatabase) this.resource;
        Role role = database.createRole(rolename, description);
        try {
            MBeanUtils.createMBean(role);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Exception creating role " + role + " MBean");
            iae.initCause(e);
            throw iae;
        }
        return (findRole(rolename));

    }


    /**
     * Create a new User and return the corresponding MBean Name.
     *
     * @param username User name of the new user
     * @param password Password for the new user
     * @param fullName Full name for the new user
     */
    public String createUser(String username, String password,
                             String fullName) {

        UserDatabase database = (UserDatabase) this.resource;
        User user = database.createUser(username, password, fullName);
        try {
            MBeanUtils.createMBean(user);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Exception creating user " + user + " MBean");
            iae.initCause(e);
            throw iae;
        }
        return (findUser(username));

    }


    /**
     * Return the MBean Name for the specified group name (if any);
     * otherwise return <code>null</code>.
     *
     * @param groupname Group name to look up
     */
    public String findGroup(String groupname) {

        UserDatabase database = (UserDatabase) this.resource;
        Group group = database.findGroup(groupname);
        if (group == null) {
            return (null);
        }
        try {
            ObjectName oname =
                MBeanUtils.createObjectName(managedGroup.getDomain(), group);
            return (oname.toString());
        } catch (MalformedObjectNameException e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Cannot create object name for group " + group);
            iae.initCause(e);
            throw iae;
        }

    }


    /**
     * Return the MBean Name for the specified role name (if any);
     * otherwise return <code>null</code>.
     *
     * @param rolename Role name to look up
     */
    public String findRole(String rolename) {

        UserDatabase database = (UserDatabase) this.resource;
        Role role = database.findRole(rolename);
        if (role == null) {
            return (null);
        }
        try {
            ObjectName oname =
                MBeanUtils.createObjectName(managedRole.getDomain(), role);
            return (oname.toString());
        } catch (MalformedObjectNameException e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Cannot create object name for role " + role);
            iae.initCause(e);
            throw iae;
        }

    }


    /**
     * Return the MBean Name for the specified user name (if any);
     * otherwise return <code>null</code>.
     *
     * @param username User name to look up
     */
    public String findUser(String username) {

        UserDatabase database = (UserDatabase) this.resource;
        User user = database.findUser(username);
        if (user == null) {
            return (null);
        }
        try {
            ObjectName oname =
                MBeanUtils.createObjectName(managedUser.getDomain(), user);
            return (oname.toString());
        } catch (MalformedObjectNameException e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Cannot create object name for user " + user);
            iae.initCause(e);
            throw iae;
        }

    }


    /**
     * Remove an existing group and destroy the corresponding MBean.
     *
     * @param groupname Group name to remove
     */
    public void removeGroup(String groupname) {

        UserDatabase database = (UserDatabase) this.resource;
        Group group = database.findGroup(groupname);
        if (group == null) {
            return;
        }
        try {
            MBeanUtils.destroyMBean(group);
            database.removeGroup(group);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Exception destroying group " + group + " MBean");
            iae.initCause(e);
            throw iae;
        }

    }


    /**
     * Remove an existing role and destroy the corresponding MBean.
     *
     * @param rolename Role name to remove
     */
    public void removeRole(String rolename) {

        UserDatabase database = (UserDatabase) this.resource;
        Role role = database.findRole(rolename);
        if (role == null) {
            return;
        }
        try {
            MBeanUtils.destroyMBean(role);
            database.removeRole(role);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Exception destroying role " + role + " MBean");
            iae.initCause(e);
            throw iae;
        }

    }


    /**
     * Remove an existing user and destroy the corresponding MBean.
     *
     * @param username User name to remove
     */
    public void removeUser(String username) {

        UserDatabase database = (UserDatabase) this.resource;
        User user = database.findUser(username);
        if (user == null) {
            return;
        }
        try {
            MBeanUtils.destroyMBean(user);
            database.removeUser(user);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Exception destroying user " + user + " MBean");
            iae.initCause(e);
            throw iae;
        }

    }


}
