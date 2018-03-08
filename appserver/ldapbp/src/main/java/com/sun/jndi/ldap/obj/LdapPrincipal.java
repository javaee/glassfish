/*
 * @(#)LdapPrincipal.java	1.2 03/04/25
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
	if (object instanceof String) {
	    return ldapName.equals(getLdapName((String)object));
	}
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
