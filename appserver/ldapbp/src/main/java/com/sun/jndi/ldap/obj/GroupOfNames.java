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
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import javax.naming.spi.NamingManager;
import com.sun.jndi.ldap.LdapURL;

/**
 * A representation of the LDAP groupOfNames object class.
 * This is a static group: its members are listed in the group's member
 * LDAP attribute.
 * <p>
 * Note that when a <tt>GroupOfNames</tt> object is created by the application 
 * program then most of its methods throw {@link IllegalStateException}
 * until the program binds the object in the directory. However, when a 
 * <tt>GroupOfNames</tt> object is returned to the application program then the 
 * object is already bound in the directory and its methods function normally.
 * <p>
 * A <tt>GroupOfNames</tt> instance is not synchronized against concurrent
 * multithreaded access. Multiple threads trying to access and modify a
 * <tt>GroupOfNames</tt> should lock the object.
 * <p>
 * In order to bind a <tt>GroupOfNames</tt> object in the directory, the 
 * following LDAP object class definition (RFC 2256) must be supported in the 
 * directory schema:
 * <pre>
 *     ( 2.5.6.9 NAME 'groupOfNames'
 *        SUP top
 *        STRUCTURAL
 *        MUST ( member $
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
 * The following code sample shows how the class may be used:
 * <pre>
 *
 *     // set the java.naming.factory.object property
 *     env.put(Context.OBJECT_FACTORIES,
 *         "com.sun.jndi.ldap.obj.LdapGroupFactory");
 *
 *     // set the java.naming.factory.state property
 *     env.put(Context.STATE_FACTORIES,
 *         "com.sun.jndi.ldap.obj.LdapGroupFactory");
 *
 *     // create an initial context using the supplied environment properties
 *     DirContext ctx = new InitialDirContext(env);
 *
 *     // create a set of members
 *     Set members = new HashSet();
 *     members.add("cn=bill,ou=people");
 *     members.add("cn=ben,ou=people");
 *     members.add("cn=sysadmins,ou=groups");
 *     Group administrators = new GroupOfNames(members);
 *
 *     // bind the group in the directory
 *     ctx.bind("cn=administrators,ou=groups", administrators);
 *
 *     // list all of the group's members
 *     listMembers(administrators);
 *     ...
 *
 *
 *     // list the members of a group (subgroups are expanded, by default)
 *     void listMembers(Group group) {
 *         for (Enumeration members = group.members();
 *                 members.hasMoreElements(); ) {
 *             Object object = members.nextElement();
 *             if (object instanceof Group) {
 *                 System.out.println("+" + ((Group) object).getName());
 *             } else if (object instanceof Principal) {
 *                 System.out.println(" " + ((Principal) object).getName());
 *             }
 *          }
 *      }
 * 
 * </pre>
 *
 * @author Vincent Ryan
 */
public class GroupOfNames implements Group {

    private static final boolean debug = false;
    private static final String OBJECT_CLASS = "groupOfNames";
    private static final String MEMBER_ATTR_ID = "member";
    private static final String MEMBER_FILTER_EXPR = "(member={0})";
    private static final String EXPAND_GROUP =
	"com.sun.jndi.ldap.obj.expandGroup";
    private static final Attribute OBJECT_CLASS_ATTR =
        new BasicAttribute("objectClass", "top");
    static {
	OBJECT_CLASS_ATTR.add(OBJECT_CLASS);
    }
    private static final SearchControls BASE_SEARCH_NO_ATTRS =
        new SearchControls();
    static {
	BASE_SEARCH_NO_ATTRS.setSearchScope(SearchControls.OBJECT_SCOPE);
	BASE_SEARCH_NO_ATTRS.setReturningAttributes(new String[0]); //no attrs
    }

    private boolean objectIsBound;
    private boolean expandGroup = true;
    private Attributes attributes = null;
    private Attribute memberAttr = null;
    private String memberAttrId;
    private String memberFilterExpr;
    private Object[] filterArgs = new Object[1];
    private ModificationItem[] modification = new ModificationItem[1];

    private String groupDN = null;
    private String bindDN = null;
    private DirContext rootCtx = null;
    private DirContext ctx = null;
    private DirContext bindCtx = null;
    private Name name = null;
    private Name bindName = null;
    private Hashtable env = null;

    /**
     * Create an empty group object.
     * <p>
     * Note that the newly constructed object does not represent a group in 
     * the directory until it is bound by using 
     * {@link javax.naming.directory.DirContext#bind(javax.naming.Name, 
     * java.lang.Object, javax.naming.directory.Attributes) DirContext.bind}.
     */
    public GroupOfNames() {
	if (debug) {
	    System.out.println("[debug] constructing an empty group");
	}
	initializeState(OBJECT_CLASS_ATTR, MEMBER_ATTR_ID, MEMBER_FILTER_EXPR,
	    null);
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
     *                {@link java.security.Principal}.
     */
    public GroupOfNames(Set members) {
	if (debug) {
	    System.out.println("[debug] constructing a group");
	}
	initializeState(OBJECT_CLASS_ATTR, MEMBER_ATTR_ID, MEMBER_FILTER_EXPR,
	    members);
    }

    /**
     * Create a group object.
     * This method is called by {@link GroupOfUniqueNames}.
     *
     * @param objectClass The LDAP objectClass attribute.
     * @param memberAttrId The LDAP attribute ID which identifies the members.
     * @param memberfilterExpr The filter expression used to find a member.
     * @param members The set of initial members. It may be null. 
     */
    // package private (used by GroupOfUniqueNames)
    GroupOfNames(Attribute objectClass, String memberAttrId,
	    String memberFilterExpr, Set members) {
	initializeState(objectClass, memberAttrId, memberFilterExpr, members);
    }

    /**
     * Create a group object from its entry in the directory.
     * This method is called by {@link GroupOfUniqueNames}.
     *
     * @param objectClass The LDAP objectClass attribute.
     * @param memberAttrId The LDAP attribute ID which identifies the members.
     * @param memberfilterExpr The filter expression used to find a member.
     * @param members The set of initial members. It may be null. 
     * @param groupDN The group's distinguished name.
     * @param name The group's LDAP distinguished name.
     * @param ctx An LDAP context.
     * @param name The group's name relative to the context.
     * @param env The context's environment properties.
     * @param attributes The group's LDAP attributes.
     */
    // package private (used by GroupOfUniqueNames)
    GroupOfNames(Attribute objectClass, String memberAttrId,
	    String memberFilterExpr, Set members, String groupDN,
	    DirContext ctx, Name name, Hashtable env, Attributes attributes) {

	initializeState(objectClass, memberAttrId, memberFilterExpr, members);
	initializeBoundState(groupDN, ctx, name, env, attributes);
    }

    /**
     * Create a group object from its entry in the directory.
     * This method is called by {@link LdapGroupFactory}.
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
	    System.out.println("[debug] creating a group named: " + groupDN);
	}
	return new GroupOfNames(OBJECT_CLASS_ATTR, MEMBER_ATTR_ID,
	    MEMBER_FILTER_EXPR, null, groupDN, ctx, name, env, attributes);
    }

    /**
     * Adds a member to the group.
     * Performs an LDAP modify to add the member.
     *
     * @param member The name of the member to be added.
     * @return boolean true if the addition was successful; false otherwise.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public boolean addMember(Principal member) {
	try {
	    return addMember(member.getName());
	} catch (NamingException e) {
	    if (debug) {
		System.out.println("[debug] error adding the member: " + e);
	    }
	    return false;
	}
    }

    /**
     * Adds a member to the group.
     * Performs an LDAP modify to add the member.
     *
     * @param dn The distinguished name (RFC 2253) of the member to be added.
     * @return boolean true if the addition was successful; false otherwise.
     * @throws NamingException The exception is thrown if an error occurs while
     *                         performing LDAP modify.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public boolean addMember(String dn) throws NamingException {

	if (! isBound()) {
	    throw new IllegalStateException(); 
	}
	if (debug) {
	    System.out.println("[debug] adding the member: " + dn);
	}
	return modifyMember(dn, DirContext.ADD_ATTRIBUTE);
    }

    /**
     * Checks if the supplied name is a member of the group.
     * Performs LDAP searches to determine membership.
     * <p>
     * By default, subgroups are also checked. 
     * As subgroup expansion is potentially an expensive activity the feature
     * may be disabled by setting the environment property
     * "com.sun.jndi.ldap.obj.expandGroup"
     * to the string value "false".
     *
     * @param member The name of the member to be checked.
     * @return boolean true if membership is confirmed; false otherwise.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public boolean isMember(Principal member) {
	try {
	    return isMember(member.getName());
	} catch (NamingException e) {
	    if (debug) {
		System.out.println("[debug] error testing for membership: " +
		    e);
	    }
	    return false;
	}
    }

    /**
     * Checks if the supplied name is a member of the group.
     * Performs LDAP searches to determine membership.
     * <p>
     * By default, subgroups are also checked. 
     * As subgroup expansion is potentially an expensive activity the feature
     * may be disabled by setting the environment property
     * "com.sun.jndi.ldap.obj.expandGroup"
     * to the string value "false".
     *
     * @param dn The distinguished name (RFC 2253) of the member to be checked.
     * @return boolean true if membership is confirmed; false otherwise.
     * @throws NamingException The exception is thrown if an error occurs while
     *                         performing LDAP search.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public boolean isMember(String dn) throws NamingException {

	if (! isBound()) {
	    throw new IllegalStateException(); 
	}
	if (debug) {
	    System.out.println(
		"[debug] checking if \"" + dn + "\" is a member");
	}

	// Check cache
	if (memberAttr != null && memberAttr.contains(dn)) {
	    return true;
	}

	// Check directory group
	filterArgs[0] = dn;
	NamingEnumeration results =
	    ctx.search(name, memberFilterExpr, filterArgs,
		BASE_SEARCH_NO_ATTRS);

	// Membership is confirmed if any results are returned
	if (results != null && results.hasMore()) {
	    results.close(); // cleanup
	    return true;
	}

	// Check directory subgroups
	if (expandGroup) {
	    return isSubgroupMember(dn);
	}

	return false;
    }

    /**
     * Returns the members of the group. 
     * Performs LDAP searches to retrieve the members.
     * <p>
     * By default, subgroups and their members are also included. 
     * As subgroup expansion is potentially an expensive activity the feature
     * may be disabled by setting the environment property
     * "com.sun.jndi.ldap.obj.expandGroup"
     * to the string value "false". When the feature is disabled only the
     * group's direct members are returned.
     *
     * @return Enumeration The list of members of the group. 
     *         When only the {@link LdapGroupFactory} object factory is active 
     *         then each element in the enumeration is of class 
     *         {@link java.security.acl.Group} or
     *         {@link java.security.Principal}. However, when additional
     *         object factories are active then the enumeration may contain
     *         elements of a different class.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public Enumeration members() {

	if (! isBound()) {
	    throw new IllegalStateException(); 
	}
	if (debug) {
	    System.out.println("[debug] enumerating the members");
	}

	try {
	    // Retrieve the group's member attribute unless already cached
	    if ((memberAttr != null) ||
		(memberAttr =
		    ctx.getAttributes(name, new String[] {memberAttrId})
		        .get(memberAttrId)) != null) {
		return new Members(memberAttr.getAll());
	    } 

	} catch (NamingException e) {
	    if (debug) {
		System.out.println("[debug] error enumerating the members: " +
		    e);
	    }
	    // ignore
	}
	return new Members(); // empty
    }

    /**
     * Removes a member from the group.
     * Performs an LDAP modify to remove the member.
     *
     * @param member The name of the member to be removed.
     * @return boolean true if the removal was successful; false otherwise.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public boolean removeMember(Principal member) {
	try {
	    return removeMember(member.getName());
	} catch (NamingException e) {
	    if (debug) {
		System.out.println("[debug] error removing the member: " + e);
	    }
	    return false;
	}
    }

    /**
     * Removes a member from the group.
     * Performs an LDAP modify to remove the member.
     *
     * @param dn The distinguished name (RFC 2253) of the member to be removed.
     * @return boolean true if the removal was successful; false otherwise.
     * @throws NamingException The exception is thrown if an error occurs while
     *                         performing LDAP modify.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public boolean removeMember(String dn) throws NamingException {

	if (! isBound()) {
	    throw new IllegalStateException(); 
	}
	if (debug) {
	    System.out.println("[debug] removing the member: " + dn);
	}
	return modifyMember(dn, DirContext.REMOVE_ATTRIBUTE);
    }

    /**
     * Retrieves the distinguished name of the group.
     * 
     * @return String The distinguished name of the group.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public String getName() {

	if (! isBound()) {
	    throw new IllegalStateException(); 
	}
	return groupDN;
    }

    /**
     * Sets the distinguished name of the group.
     * This method is called by {@link LdapGroupFactory}.
     *
     * @param groupDN The group's distinguished name.
     * @param ctx An LDAP context.
     * @param name The group's name relative to the context.
     */
    // package private (used by LdapGroupFactory)
    void setName(String groupDN, DirContext ctx, Name name) {
	bindDN = groupDN;
	bindCtx = ctx;
	bindName = name;
    }

    /**
     * Creates a string representation of the group.
     *
     * @return String A string listing the distinguished name of the group and
     *         the contents of the group's attribute set. See
     * {@link javax.naming.directory.BasicAttributes#toString()}
     *         for details. The name is omitted if the group is not bound in
     *         the directory and null is returned if no attributes are 
     *         available.
     */
    public String toString() {
	isBound(); // refresh attributes (if necessary)
	if (groupDN != null) {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("{name: ").append(groupDN).append("}");
	    if (attributes != null) {
		buffer.append(attributes.toString());
	    }
	    return buffer.toString();
	} else {	   
	    return attributes == null ? "" : attributes.toString();
	}
    }

    /**
     * Retrieves the group's attributes.
     * This method is called by {@link LdapGroupFactory}.
     *
     * @return Attribute The group's attributes.
     */
    // package private (used by LdapGroupFactory)
    Attributes getAttributes() {
	return attributes;
    }

    /**
     * Determines whether the supplied LDAP objectClass attribute matches that 
     * of the group. A match occurs if the argument contains the value 
     * "GroupOfNames".
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
	    // ignore
	}
	return false;
    }

    /*
     * Determines whether the group object is bound in the directory.
     *
     * A group object is considered to be bound in the directory when 
     * each of the values of its objectClass attribute match those of 
     * an object in the directory having this group's distinguished name.
     *
     * @return true if the object is bound; false otherwise.
     */
    private boolean isBound() {
	if (objectIsBound) {
	    return true;

	} else if (bindCtx != null && bindName != null && attributes != null)  {
	    try {
		// Retrieve the group's attributes
		Attributes bindAttrs = bindCtx.getAttributes(bindName);
		Attribute bindObjectClass = bindAttrs.get("objectClass");
		// Check whether the objectClass attributes match
		if (bindObjectClass != null && 
		    bindObjectClass.equals(attributes.get("objectClass"))) {
		    // Set the group's bound state
		    initializeBoundState(bindDN, bindCtx, bindName, env,
			bindAttrs);
		    return true;
		}

	    } catch (NameNotFoundException e) {
		if (debug) {
		    System.out.println("[debug] object is not bound: " + e);
		}
		// ignore

	    } catch (NamingException e) {
		if (debug) {
		    System.out.println("[debug] error checking if bound: " + e);
		}
		// ignore
	    }
	    // Reset state to unbound
	    bindDN = null;
	    bindCtx = null;
	    bindName = null;
	}
	return false;
    }

    /**
     * Releases the naming context created by this group.
     * Closes the root naming context if one had been created.
     *
     * @throws NamingException The exception is thrown if a problem is
     *                         encountered while closing the naming context.
     */
    public void close() throws NamingException {
	if (rootCtx != null && rootCtx != ctx) {
	    rootCtx.close();
	    rootCtx = null;
	}
    }

    /*
     * Initialize the group's state when unbound.
     */
    private void initializeState(Attribute objectClass, String memberAttrId,
	String memberFilterExpr, Set members) {

	objectIsBound = false;
	this.memberAttrId = memberAttrId;
	this.memberFilterExpr = memberFilterExpr;

	// initialize the group's attribute set.
	attributes = new BasicAttributes(true);
	attributes.put(objectClass);

	if (members != null && (! members.isEmpty())) {
	    memberAttr = new BasicAttribute(memberAttrId);
	    for (Iterator i = members.iterator(); i.hasNext(); ) {
		Object object = i.next();
		if (object instanceof Principal) {
		    memberAttr.add(((Principal)object).getName());
		} else {
		    memberAttr.add(object);
		}
	    }
	    attributes.put(memberAttr);
	}
    }

    /*
     * Initialize the group's state when bound.
     */
    private void initializeBoundState(String groupDN, DirContext ctx, Name name,
	    Hashtable env, Attributes attributes) {

	objectIsBound = true;
	this.groupDN = groupDN;
	this.ctx = ctx;
	this.name = name;
	this.env = env;
	if (env == null && ctx != null) {
	    try {
		this.env = ctx.getEnvironment();
	    } catch (NamingException e) {
		// ignore
	    }
	}
	if (env != null) {
	    String expandGroup = (String)env.get(EXPAND_GROUP);
	    if ("false".equalsIgnoreCase(expandGroup)) {
		this.expandGroup = false;
	    }
	}
	if (attributes != null) {
	    this.attributes = attributes;
	    memberAttr = attributes.get(memberAttrId);
	}
    }

    /*
     * Add or remove a value from the member attribute.
     */
    private boolean modifyMember(String member, int mod_op) 
	    throws NamingException {
	Attribute memberAttr = new BasicAttribute(memberAttrId, member);
	modification[0] = new ModificationItem(mod_op, memberAttr);
	ctx.modifyAttributes(name, modification);
	this.memberAttr = null; // invalidate the cache
	return true;
    }

    /*
     * Checks if the supplied name is a member of any subgroups.
     * All the members are retrieved and any subgroups are explored.
     */
    private boolean isSubgroupMember(String dn) throws NamingException {
        for (NamingEnumeration members = (NamingEnumeration)members();
		members.hasMore(); ) {
            Object obj = members.next();
            if (obj instanceof GroupOfNames &&
		((GroupOfNames)obj).isMember(dn)) {
		members.close(); // cleanup
                return true;
            } else if (obj instanceof GroupOfURLs &&
		((GroupOfURLs)obj).isMember(dn)) {
		members.close(); // cleanup
                return true;
	    }
        }
        return false;
    }

    /*
     * Generate environment properties suitable for the root context using
     * the supplied set of properties. The following properties are modified 
     * if necessary:
     * <ul>
     * <li> java.naming.provider.url property:
     *        trim any components after hostname/port.
     *        (identifies the root context).
     * </ul>
     *
     * @param env A set of environment properties. It is cloned only when a
     *            modification is necessary.
     * @return A set of environment properties suitable for the root context.
     */
    // package private (used by GroupOfURLs)
    static Hashtable generateRootContextProperties(Hashtable env) {
	String url = null;

	if (env != null) {
	    if ((url = (String) env.get(Context.PROVIDER_URL)) != null) {

		try {
		    // java.net.URI is cleaner but depends on J2SE v 1.4
		    LdapURL ldapUrl = new LdapURL(url);
		    String dn = ldapUrl.getDN();
		    // check if a non-empty DN is present
		    if (dn != null && dn.length() > 0) {
			String host = ldapUrl.getHost();
			int port = ldapUrl.getPort();
			url = "ldap://" +
			    ((host != null) ? host : "") +
			    ((port != -1) ? (":" + port) : "");
		    } else {
			url = null; // reset flag
		    }
		} catch (NamingException e) {
		    throw new IllegalArgumentException(url);
		}
	    }
	    // only clone if making mods
	    if (url != null) {
		env = (Hashtable) env.clone();
		env.put(Context.PROVIDER_URL, url);
	    }
	}

	return env;
    }


/**
 * The members of a static group.
 */
class Members implements NamingEnumeration {

    private NamingEnumeration memberDNs = null;
    private boolean expandSubgroups;
    private ArrayList subgroups = null;
    private NamingEnumeration subgroupMembers = null;

    /*
     * Empty members object derived from a static group.
     */
    Members() {
	if (debug) {
	    System.out.println("[debug] constructing an empty GroupOfNames.Members object");
	}
        expandSubgroups = expandGroup; // GroupOfNames.expandGroup
    }

    /*
     * Members object derived from a static group.
     */
    Members(NamingEnumeration memberDNs) {
	if (debug) {
	    System.out.println("[debug] constructing a GroupOfNames.Members object");
	}
	this.memberDNs = memberDNs;
        expandSubgroups = expandGroup; // GroupOfNames.expandGroup
    }

    /**
     * Check if the group has more members.
     *
     * @return true if the group has another member.
     */
    public boolean hasMoreElements() {
	try {
	    return hasMore();
	} catch (NamingException e) {
	    if (debug) {
		System.out.println("[debug] error checking for more members: " +
		    e);
	    }
	    return false;
	}
    }

    /**
     * Check if the group has more members.
     *
     * @return true if the group has another member.
     * @throws NamingException If a problem is encountered while checking 
     *                         whether the group has any more members.
     */
    public boolean hasMore() throws NamingException {

	if (memberDNs == null) {
	    return false; // empty
	}

	if (memberDNs.hasMore()) {
	    return true;
	}

	// Check subgroups
	if (expandSubgroups && subgroups != null) {
	    if (subgroupMembers == null && (! subgroups.isEmpty())) {
		// Retrieve the first subgroup's members
		subgroupMembers =
		    (NamingEnumeration)((Group)subgroups.remove(0)).members();
	    }
	    if (null != subgroupMembers && subgroupMembers.hasMore()) {
		return true;
	    } else if (! subgroups.isEmpty()) {
		// Retrieve the next subgroup's members
		subgroupMembers = 
		    (NamingEnumeration)((Group)subgroups.remove(0)).members();
		return subgroupMembers.hasMore();
	    }
	}

	return false;
    }

    /**
     * Retrieve the next member of the group.
     * Some members may themselves be groups. Such a member is returned as 
     * an object of class {@link java.security.acl.Group}.
     * <p>
     * Note that in order to determine whether a member is itself a group
     * this method reads each member's LDAP entry. As this is potentially an
     * expensive activity the feature may be disabled by setting the 
     * environment property 
     * "com.sun.jndi.ldap.obj.expandGroup" 
     * to the string value "false".  When the feature is
     * disabled then an object of class {@link java.security.Principal}
     * is returned. By default, the feature is enabled.
     *
     * @return The next member of the group.
     *         When only the {@link LdapGroupFactory} object factory is active 
     *         then an object of class 
     *         {@link java.security.Principal} or
     *         {@link java.security.acl.Group} is returned.
     *         However, when additional object factories are active then an
     *         object of a different class may be returned.
     * @throws NoSuchElementException If no more members exist or if a
     *         {@link javax.naming.NamingException} was encountered while 
     *         retrieving the next element.
     */
    public Object nextElement() {
	try {
	    return next();
	} catch (NamingException e) {
	    // Exception.initCause is cleaner but depends on J2SE v 1.4
	    throw new NoSuchElementException(e.toString());
	}
    }

    /**
     * Retrieve the next member of the group.
     * Some members may themselves be groups. Such a member is returned as 
     * an object of class {@link java.security.acl.Group}.
     * <p>
     * Note that in order to determine whether a member is itself a group
     * this method reads each member's LDAP entry. As this is potentially an
     * expensive activity the feature may be disabled by setting the 
     * environment property 
     * "com.sun.jndi.ldap.obj.expandGroup" 
     * to the string value "false".  When the feature is
     * disabled then an object of class {@link java.security.Principal}
     * is returned. By default, the feature is enabled.
     *
     * @return The next member of the group.
     *         When only the {@link LdapGroupFactory} object factory is active 
     *         then an object of class 
     *         {@link java.security.Principal} or
     *         {@link java.security.acl.Group} is returned.
     *         However, when additional object factories are active then an
     *         object of a different class may be returned.
     * @throws NamingException If a problem is encountered while retrieving the
     *                         next member of the group.
     * @throws NoSuchElementException If no more members exist.
     */
    public Object next() throws NamingException {

	if (memberDNs == null) {
	    throw new NoSuchElementException(); // empty
	}

	String memberDN = null;

	try {
	    if (memberDNs.hasMore()) {
		memberDN = (String)memberDNs.next();

		// Skip lookup when expandGroup=false
		if (! expandGroup) {
		    return new LdapPrincipal(memberDN);
		}

		// Create the root context
		if (rootCtx == null) {
		    rootCtx = getRootContext();
		}
		// Perform the lookup from the root context
		Object object =
		    rootCtx.lookup(new CompositeName().add(memberDN));

		if (object instanceof Group) {
		    if (expandSubgroups) {
			if (subgroups == null) {
			    subgroups = new ArrayList();
			}
			subgroups.add(object);
		    }
		    // Subgroups are members too 
		    return (Group)object;

		} else if (object instanceof DirContext) {
		    ((DirContext)object).close(); // cleanup
		    return new LdapPrincipal(memberDN);

		} else {
		    return object; // additional object factories are active
		}
	    }
	} catch (NameNotFoundException e) {
	    // Cannot find the member's LDAP entry so return an LdapPrincipal
	    return new LdapPrincipal(memberDN);
	}

        // Check subgroups
        if (expandSubgroups && subgroups != null) {
            if (subgroupMembers == null && (! subgroups.isEmpty())) {
                // Retrieve the first subgroup's members
                subgroupMembers = 
		    (NamingEnumeration)((Group)subgroups.remove(0)).members();
            }
            if (null != subgroupMembers && subgroupMembers.hasMore()) {
                return subgroupMembers.next();
            } else if (! subgroups.isEmpty()) {
                // Retrieve the next subgroup's members
                subgroupMembers = 
		    (NamingEnumeration)((Group)subgroups.remove(0)).members();
                return subgroupMembers.next();
            }
        }

        throw new NoSuchElementException();
    }

    /**
     * Closes the enumeration and releases its resources.
     *
     * @throws NamingException If a problem is encountered while
     *                         closing the enumeration.
     */
    public void close() throws NamingException {
	if (subgroupMembers != null) {
	    subgroupMembers.close(); // cleanup
	}
    }

    private DirContext getRootContext() throws NamingException {
	DirContext rootCtx = null;
	// test for the root of the namespace (the empty name)
	if (ctx.getNameInNamespace().length() == 0) {
	    rootCtx = ctx; // context is already a root context
	} else {
	    // Make properties suitable for the root context
	    env = generateRootContextProperties(env);
	    rootCtx = (DirContext) NamingManager.getInitialContext(env);
	    // Propagate any context request controls (to rootCtx)
	    if (ctx instanceof LdapContext) {
		((LdapContext)rootCtx).setRequestControls(
		    ((LdapContext)ctx).getRequestControls());
	    }
	}
	return rootCtx;
    }
}
}
