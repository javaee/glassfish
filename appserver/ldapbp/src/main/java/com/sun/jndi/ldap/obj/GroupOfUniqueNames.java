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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import javax.naming.*;
import javax.naming.directory.*;

/**
 * A representation of the LDAP groupOfUniqueNames object class.
 * This is a static group: its members are listed in the group's
 * uniqueMember LDAP attribute.
 * <p>
 * Note that when a <tt>GroupOfUniqueNames</tt> object is created by the 
 * application program then most of its methods throw 
 * {@link IllegalStateException}
 * until the program binds the object in the directory. However, when a 
 * <tt>GroupOfUniqueNames</tt> object is returned to the application program 
 * then the object is already bound in the directory and its methods function 
 * normally.
 * <p>
 * A <tt>GroupOfUniqueNames</tt> instance is not synchronized against concurrent
 * multithreaded access. Multiple threads trying to access and modify a
 * <tt>GroupOfUniqueNames</tt> should lock the object.
 * <p>
 * In order to bind a <tt>GroupOfUniqueNames</tt> object in the directory, the 
 * following LDAP object class definition (RFC 2256) must be supported in the 
 * directory schema:
 * <pre>
 *     ( 2.5.6.17 NAME 'groupOfUniqueNames'
 *        SUP top
 *        STRUCTURAL
 *        MUST ( uniqueMember $
 *               cn )
 *        MAY ( businessCategory $
 *              seeAlso $
 *              owner $
 *              ou $
 *              o $
 *              description ) )
 * </pre>
 * See
 * {@link javax.naming.directory.DirContext#bind(javax.naming.Name, 
 * java.lang.Object, javax.naming.directory.Attributes) DirContext.bind}
 * for details on binding an object in the directory.
 * <p>
 * The code sample in {@link GroupOfNames} shows how the class may be used.
 *
 * @author Vincent Ryan
 */
public class GroupOfUniqueNames extends GroupOfNames {

    private static final boolean debug = false;
    private static final String OBJECT_CLASS = "groupOfUniqueNames";
    private static final String MEMBER_ATTR_ID = "uniqueMember";
    private static final String MEMBER_FILTER_EXPR = "(uniquemember={0})";
    private static final Attribute OBJECT_CLASS_ATTR;
    static {
	OBJECT_CLASS_ATTR = new BasicAttribute("objectClass", "top");
	OBJECT_CLASS_ATTR.add(OBJECT_CLASS);
    }

    /**
     * Create an empty group object.
     * <p>
     * Note that the newly constructed object does not represent a group in 
     * the directory until it is bound by using 
     * {@link javax.naming.directory.DirContext#bind(javax.naming.Name, 
     * java.lang.Object, javax.naming.directory.Attributes) DirContext.bind}.
     */
    public GroupOfUniqueNames() {
	super(OBJECT_CLASS_ATTR, MEMBER_ATTR_ID, MEMBER_FILTER_EXPR, null);
    }

    /**
     * Create a group object with an initial set of members.
     * <p>
     * Note that the newly constructed object does not represent a group in 
     * the directory until it is bound by using 
     * {@link javax.naming.directory.DirContext#bind(javax.naming.Name, 
     * java.lang.Object, javax.naming.directory.Attributes) DirContext.bind}.
     *
     * @param members The set of initial members. It may be null. 
     *                Each element is of class {@link String} or
     *                {@link java.security.Principal}
     */
    public GroupOfUniqueNames(Set members) {
	super(OBJECT_CLASS_ATTR, MEMBER_ATTR_ID, MEMBER_FILTER_EXPR, members);
    }

    /*
     * Create a group object from its entry in the directory.
     */
    private GroupOfUniqueNames(String groupDN, DirContext ctx, Name name,
	    Hashtable env, Attributes attributes) {
	super(OBJECT_CLASS_ATTR, MEMBER_ATTR_ID, MEMBER_FILTER_EXPR, null,
	    groupDN, ctx, name, env, attributes);
    }

    /**
     * Create a group object from its entry in the directory.
     * This method is called by {@link LdapGroupFactory}
     *
     * @param groupDN The group's distinguished name.
     * @param ctx An LDAP context.
     * @param name The group's name relative to the context.
     * @param env The context's environment properties.
     * @param attributes The group's LDAP attributes.
     * @return Object The new object instance.
     */
    // package private (used by LdapGroupFactory)
    static Object getObjectInstance(String groupDN, DirContext ctx, Name name,
	    Hashtable env, Attributes attributes) {
	if (debug) {
	    System.out.println("[debug] creating a group named: " + name);
	}
	return new GroupOfUniqueNames(groupDN, ctx, name, env, attributes);
    }

    /**
     * Determines whether the supplied LDAP objectClass attribute matches that 
     * of the group. A match occurs if the argument contains the value 
     * "GroupOfUniqueNames".
     *
     * @param objectClass The non-null objectClass attribute to check against.
     * @return true if the objectClass attributes match; false otherwise.
     */
    // package private (used by LdapGroupFactory)
    static boolean matches(Attribute objectClass) {

	try {
	    for (Enumeration values = objectClass.getAll();
		values.hasMoreElements(); ) {
		if (OBJECT_CLASS.equalsIgnoreCase(
		    (String)values.nextElement())) {
		    return true;
		}
	    }
	} catch (NamingException e) {
	    if (debug) {
		System.out.println("[debug] error matching objectClass: " + e);
	    }
	}
	return false;
    }
}
