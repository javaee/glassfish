

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
import org.apache.catalina.UserDatabase;


/**
 * <p>Convenience base class for {@link Role} implementations.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:12 $
 * @since 4.1
 */

public abstract class AbstractRole implements Role {


    // ----------------------------------------------------- Instance Variables


    /**
     * The description of this Role.
     */
    protected String description = null;


    /**
     * The role name of this Role.
     */
    protected String rolename = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the description of this role.
     */
    public String getDescription() {

        return (this.description);

    }


    /**
     * Set the description of this role.
     *
     * @param description The new description
     */
    public void setDescription(String description) {

        this.description = description;

    }


    /**
     * Return the role name of this role, which must be unique
     * within the scope of a {@link UserDatabase}.
     */
    public String getRolename() {

        return (this.rolename);

    }


    /**
     * Set the role name of this role, which must be unique
     * within the scope of a {@link UserDatabase}.
     *
     * @param rolename The new role name
     */
    public void setRolename(String rolename) {

        this.rolename = rolename;

    }


    /**
     * Return the {@link UserDatabase} within which this Role is defined.
     */
    public abstract UserDatabase getUserDatabase();


    // --------------------------------------------------------- Public Methods


    // ------------------------------------------------------ Principal Methods


    /**
     * Make the principal name the same as the role name.
     */
    public String getName() {

        return (getRolename());

    }


}
