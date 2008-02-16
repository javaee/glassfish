

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


import java.security.Principal;
import java.util.Iterator;


/**
 * <p>Abstract representation of a group of {@link User}s in a
 * {@link UserDatabase}.  Each user that is a member of this group
 * inherits the {@link Role}s assigned to the group.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:16 $
 * @since 4.1
 */

public interface Group extends Principal {


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this group.
     */
    public String getDescription();


    /**
     * Set the description of this group.
     *
     * @param description The new description
     */
    public void setDescription(String description);


    /**
     * Return the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getGroupname();


    /**
     * Set the group name of this group, which must be unique
     * within the scope of a {@link UserDatabase}.
     *
     * @param groupname The new group name
     */
    public void setGroupname(String groupname);


    /**
     * Return the set of {@link Role}s assigned specifically to this group.
     */
    public Iterator getRoles();


    /**
     * Return the {@link UserDatabase} within which this Group is defined.
     */
    public UserDatabase getUserDatabase();


    /**
     * Return the set of {@link User}s that are members of this group.
     */
    public Iterator getUsers();


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new {@link Role} to those assigned specifically to this group.
     *
     * @param role The new role
     */
    public void addRole(Role role);


    /**
     * Is this group specifically assigned the specified {@link Role}?
     *
     * @param role The role to check
     */
    public boolean isInRole(Role role);


    /**
     * Remove a {@link Role} from those assigned to this group.
     *
     * @param role The old role
     */
    public void removeRole(Role role);


    /**
     * Remove all {@link Role}s from those assigned to this group.
     */
    public void removeRoles();


}
