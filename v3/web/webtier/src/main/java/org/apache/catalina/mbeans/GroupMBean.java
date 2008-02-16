

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
 * <code>org.apache.catalina.Group</code> component.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2006/10/23 23:40:31 $
 */

public class GroupMBean extends BaseModelMBean {


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
    public GroupMBean()
        throws MBeanException, RuntimeOperationsException {

        super();
        registry = MBeanUtils.createRegistry();
    }


    // ----------------------------------------------------- Instance Variables

    /**
     * The <code>MBeanServer</code> in which we are registered.
     */
    protected MBeanServer mserver = MBeanUtils.createServer();


    /**
     * The <code>ManagedBean</code> information describing this MBean.
     */
    protected ManagedBean managed =
        registry.findManagedBean("Group");


    // ------------------------------------------------------------- Attributes


    /**
     * Return the MBean Names of all authorized roles for this group.
     */
    public String[] getRoles() {

        Group group = (Group) this.resource;
        ArrayList results = new ArrayList();
        Iterator roles = group.getRoles();
        while (roles.hasNext()) {
            Role role = null;
            try {
                role = (Role) roles.next();
                ObjectName oname =
                    MBeanUtils.createObjectName(managed.getDomain(), role);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                IllegalArgumentException iae = new IllegalArgumentException
                    ("Cannot create object name for role " + role);
                iae.initCause(e);
                throw iae;
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    /**
     * Return the MBean Names of all users that are members of this group.
     */
    public String[] getUsers() {

        Group group = (Group) this.resource;
        ArrayList results = new ArrayList();
        Iterator users = group.getUsers();
        while (users.hasNext()) {
            User user = null;
            try {
                user = (User) users.next();
                ObjectName oname =
                    MBeanUtils.createObjectName(managed.getDomain(), user);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                IllegalArgumentException iae = new IllegalArgumentException
                    ("Cannot create object name for user " + user);
                iae.initCause(e);
                throw iae;
            }
        }
        return ((String[]) results.toArray(new String[results.size()]));

    }


    // ------------------------------------------------------------- Operations


    /**
     * Add a new {@link Role} to those this group belongs to.
     *
     * @param rolename Role name of the new role
     */
    public void addRole(String rolename) {

        Group group = (Group) this.resource;
        if (group == null) {
            return;
        }
        Role role = group.getUserDatabase().findRole(rolename);
        if (role == null) {
            throw new IllegalArgumentException
                ("Invalid role name '" + rolename + "'");
        }
        group.addRole(role);

    }


    /**
     * Remove a {@link Role} from those this group belongs to.
     *
     * @param rolename Role name of the old role
     */
    public void removeRole(String rolename) {

        Group group = (Group) this.resource;
        if (group == null) {
            return;
        }
        Role role = group.getUserDatabase().findRole(rolename);
        if (role == null) {
            throw new IllegalArgumentException
                ("Invalid role name '" + rolename + "'");
        }
        group.removeRole(role);

    }


}
