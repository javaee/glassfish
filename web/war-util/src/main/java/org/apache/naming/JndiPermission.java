

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Portions Copyright Apache Software Foundation.
 */ 


package org.apache.naming;

import java.security.BasicPermission;
import java.security.Permission;

/**
 * Java SecurityManager Permission class for JNDI name based file resources
 * <p>
 * The JndiPermission extends the BasicPermission.
 * The permission name is a full or partial jndi resource name.
 * An * can be used at the end of the name to match all named
 * resources that start with name.  There are no actions.</p>
 * <p>
 * Example that grants permission to read all JNDI file based resources:
 * <li> permission org.apache.naming.JndiPermission "*";</li>
 * </p>
 *
 * @author Glenn Nielsen
 * @version $Revision: 1.1.2.1 $ $Date: 2007/08/17 15:46:28 $
 */

public final class JndiPermission extends BasicPermission {

    // ----------------------------------------------------------- Constructors

    /**
     * Creates a new JndiPermission with no actions
     *
     * @param String - JNDI resource path name
     */
    public JndiPermission(String name) {
        super(name);
    }

    /**
     * Creates a new JndiPermission with actions
     *
     * @param String - JNDI resource path name
     * @param String - JNDI actions (none defined)
     */
    public JndiPermission(String name, String actions) {
        super(name,actions);
    }

}
