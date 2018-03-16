/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jndi.ldap.obj;

import java.security.Principal;
import javax.naming.NamingException;
import com.sun.jndi.ldap.LdapName;

/**
 * A principal from the LDAP directory.
 *
 * @author Vincent Ryan
 */
class LdapPrincipal implements Principal {

    private LdapName ldapName = null;
    private int hash = -1;
    private String name;

    /**
     * Create a principal.
     *
     * @param name The principal's string name.
     */
    public LdapPrincipal(String name) {
	this.name = name;
    }

    /**
     * Compares this principal to the specified object.
     *
     * @param object The object to compare this principal against.
     * @return true if they are equal; false otherwise.
     */
    public boolean equals(Object object) {
	if (ldapName == null) {
	    ldapName = getLdapName(name);
	}
	//this is a broken equals implementation. It doesnot have any dependency on Glassfish code
	/*if (object instanceof String) {
	    return ldapName.equals(getLdapName((String)object));
	}*/
	if (object instanceof Principal) {
	    return ldapName.equals(getLdapName(((Principal)object).getName()));
	}
	return false;
    }

    /**
     * Returns a hash code for this principal.
     *
     * @return The principal's hash code.
     */
    public int hashCode() {
	if (hash == -1) {
	    if (ldapName == null) {
		ldapName = getLdapName(name);
	    }
	    hash = ldapName.hashCode();
	}
	return hash;
    }

    /**
     * Returns the name of this principal.
     *
     * @return String The principal's string name.
     */
    public String getName() {
	return name;
    }

    /**
     * Returns a string representation of this principal.
     *
     * @return String The principal's string name.
     */
    public String toString() {
	return name;
    }

    private LdapName getLdapName(String name) {
	LdapName ldapName = null;
	try {
	    ldapName = new LdapName(name);
	} catch (NamingException e) {
	    // ignore
	}
	return ldapName;
    }
}
