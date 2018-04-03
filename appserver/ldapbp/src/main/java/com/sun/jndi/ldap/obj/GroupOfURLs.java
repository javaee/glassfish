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
import com.sun.jndi.ldap.LdapName;
import com.sun.jndi.ldap.LdapURL;

/**
 * A representation of the LDAP groupOfURLs object class.
 * This is a dynamic group: its membership is determined by evaluating the 
 * group's LDAP URLs.
 * <p>
 * Note that when a <tt>GroupOfURLs</tt> object is created by the application 
 * program then most of its methods throw {@link IllegalStateException}
 * until the program binds the object in the directory. However, when a 
 * <tt>GroupOfURLs</tt> object is returned to the application program then the 
 * object is already bound in the directory and its methods function normally.
 * <p>
 * A <tt>GroupOfURLs</tt> instance is not synchronized against concurrent
 * multithreaded access. Multiple threads trying to access and modify a
 * <tt>GroupOfURLs</tt> should lock the object.
 * <p>
 * In order to bind a <tt>GroupOfURLs</tt> object in the directory, the 
 * following LDAP object class definition must be supported in the directory 
 * schema:
 * <pre>
 *     ( 2.16.840.1.113730.3.2.33 NAME 'groupOfURLs' 
 *        SUP top 
 *        STRUCTURAL 
 *        MUST cn 
 *        MAY ( memberURL $ 
 *              businessCategory $ 
 *              description $ 
 *              o $ 
 *              ou $ 
 *              owner $ 
 *              seeAlso ) )
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
 *     // create a set of member URLs
 *     Set members = new HashSet();
 *     members.add(
 *         "ldap:///" + ctx.getNameInNamespace() + "??sub?(title=Manager)");
 *     Group managers = new GroupOfURLs(members);
 *
 *     // bind the group in the directory
 *     ctx.bind("cn=managers,ou=groups", managers);
 *
 *     // list all of the group's members 
 *     listMembers(managers);
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
public class GroupOfURLs implements Group {

    private static final boolean debug = false;
    private static final String OBJECT_CLASS = "groupOfURLs";
    private static final String MEMBER_ATTR_ID = "memberURL";
    private static final String EXPAND_GROUP = 
	"com.sun.jndi.ldap.obj.expandGroup";
    private static final String GROUPS_ONLY = 
	"(|(objectClass=groupOfNames)(objectClass=groupOfURLs))";
    private static final Attribute OBJECT_CLASS_ATTR =
        new BasicAttribute("objectClass", "top");
    static {
	OBJECT_CLASS_ATTR.add(OBJECT_CLASS);
    }

    private boolean objectIsBound;
    private boolean expandGroup = true;
    private Attributes attributes = null;
    private Attribute memberAttr = null;
    private SearchControls searchNoAttrs = null;
    private SearchControls objectSearch = null;
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
    public GroupOfURLs() {
	if (debug) {
	    System.out.println("[debug] constructing an empty group");
	}
	objectIsBound = false;
	// initialize the group's attribute set
	attributes = new BasicAttributes(true);
	attributes.put(OBJECT_CLASS_ATTR);
    }

    /**
     * Create a group object with an initial set of member URLs.
     * <p>
     * Note that the newly constructed object does not represent a group in 
     * the directory until it is bound by using 
     * {@link javax.naming.directory.DirContext#bind(javax.naming.Name, 
     * java.lang.Object, javax.naming.directory.Attributes) DirContext.bind}.
     *
     * @param memberURLs The set of initial member URLs. It may be null. 
     *                   Each element is a string LDAP URL (RFC 2255).
     */
    public GroupOfURLs(Set memberURLs) {
	if (debug) {
	    System.out.println("[debug] constructing a group");
	}
	objectIsBound = false;
	// initialize the group's attribute set
	attributes = new BasicAttributes(true);
	attributes.put(OBJECT_CLASS_ATTR);
	if (memberURLs != null && (! memberURLs.isEmpty())) {
	    memberAttr = new BasicAttribute(MEMBER_ATTR_ID);
	    for (Iterator i = memberURLs.iterator(); i.hasNext(); ) {
		memberAttr.add(i.next());
	    }
	    attributes.put(memberAttr);
	}
    }

    /*
     * Create a group object from its entry in the directory.
     */
    private GroupOfURLs(String groupDN, DirContext ctx, Name name,
	    Hashtable env, Attributes attributes) {

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
	return new GroupOfURLs(groupDN, ctx, name, env, attributes);
    }

    /**
     * A member cannot be added to the group directly.
     * Instead, members are added by adding a group LDAP URL.
     * See {@link #addMembers(String)}
     *
     * @param member The name of the member to be added.
     * @return The exception is always thrown.
     * @throws UnsupportedOperationException A member cannot be added directly.
     */
    public boolean addMember(Principal member) {
	throw new UnsupportedOperationException();
    }

    /**
     * Adds members to the group.
     * Performs an LDAP modify to add the LDAP URL.
     *
     * @param members The string LDAP URL describing the members to be added.
     * @return boolean true if the addition was successful; false otherwise.
     * @throws NamingException The exception is thrown if an error occurs
     *         while performing LDAP modify.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public void addMembers(String members) throws NamingException {

        if (! isBound()) {
            throw new IllegalStateException();
        }
	if (debug) {
	    System.out.println("[debug] adding the members: " + members);
	}
	modifyMembers(members, DirContext.ADD_ATTRIBUTE);
    }

    /**
     * Checks if the supplied name is a member of the group.
     * Performs an LDAP search to determine membership.
     * <p>
     * By default, subgroups are also checked. 
     * As subgroup expansion is potentially an expensive activity the feature
     * may be disabled by setting the environment property
     * "com.sun.jndi.ldap.obj.expandGroup"
     * to the string value "false.
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
     * Performs an LDAP search to determine membership.
     * <p>
     * By default, subgroups are also checked. 
     * As subgroup expansion is potentially an expensive activity the feature
     * may be disabled by setting the environment property
     * "com.sun.jndi.ldap.obj.expandGroup"
     * to the string value "false.
     *
     * @param dn The distinguished name (RFC 2253) of the member to be checked.
     * @return boolean true if membership is confirmed; false otherwise.
     * @throws NamingException The exception is thrown if an error occurs
     *         while performing LDAP search.
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

	// Retrieve the group's member attribute unless already cached
	if ((memberAttr != null) ||
	    (memberAttr = ctx.getAttributes(name, new String[] {MEMBER_ATTR_ID})
		.get(MEMBER_ATTR_ID)) != null) {

	    if (searchNoAttrs == null) {
		searchNoAttrs = new SearchControls();
		// Request no attributes
		searchNoAttrs.setReturningAttributes(new String[0]);
	    }

	    // Process each LDAP URL
	    for (Enumeration values = memberAttr.getAll();
		values.hasMoreElements(); ) {

		NamingEnumeration results =
		    searchUsingLdapUrl((String)values.nextElement(),
			searchNoAttrs, dn);

		// Membership is confirmed if any results are returned
		if (results != null && results.hasMore()) {
		    results.close(); // cleanup
		    return true;
		}
	    }

	    // Check subgroups
	    if (expandGroup) {
		return isSubgroupMember(dn);
	    }
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
     * to the string value "false. When the feature is disabled only the group's
     * direct members are returned.
     *
     * @return Enumeration The list of members of the group.
     *         When only the {@link LdapGroupFactory} object factory is active 
     *         then each element in the enumeration is of class 
     *         {@link java.security.acl.Group} or
     *         {@link java.security.Principal} However, when additional
     *         object factories are active then the enumeration may contain
     *         elements of a different class.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public Enumeration members() {
	try {
	    return members(null);
	} catch (NamingException e) {
	    if (debug) {
		System.out.println("[debug] error enumerating the members: " +
		    e);
	    }
	    return new Members(); // empty
	}
    }

    /**
     * Returns the members of the group that satisfy the search filter.
     * Performs LDAP searches to retrieve the members.
     * <p>
     * By default, subgroups and their members are also included. 
     * As subgroup expansion is potentially an expensive activity the feature
     * may be disabled by setting the environment property
     * "com.sun.jndi.ldap.obj.expandGroup"
     * to the string value "false. When the feature is disabled only the group's
     * direct members are returned.
     *
     * @param filter The string filter to apply to the members of the group.
     *               If the argument is null then no filtering is performed.
     * @return Enumeration The list of members that satisfy the filter.
     *         When only the {@link LdapGroupFactory} object factory is active 
     *         then each element in the enumeration is of class 
     *         {@link java.security.acl.Group} or
     *         {@link java.security.Principal} However, when additional
     *         object factories are active then the enumeration may contain
     *         elements of a different class.
     * @throws NamingException The exception is thrown if an error occurs
     *         while performing LDAP search.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public Enumeration members(String filter) throws NamingException {

        if (! isBound()) {
            throw new IllegalStateException();
        }
	if (debug) {
	    System.out.println("[debug] enumerating the members");
	}

	// Retrieve the group's member attribute unless already cached
	if ((memberAttr != null) ||
	    (memberAttr = ctx.getAttributes(name, new String[] {MEMBER_ATTR_ID})
		.get(MEMBER_ATTR_ID)) != null) {

	    if (objectSearch == null) {
		objectSearch = new SearchControls();
		// Request objects
		objectSearch.setReturningObjFlag(true);
	    }
	    return new Members(memberAttr.getAll(), filter, objectSearch);
	}

	return new Members(); // empty
    }

    /**
     * A member cannot be removed from the group directly.
     * Instead, members are removed by removing a group LDAP URL.
     * See {@link #removeMembers(String)}
     *
     * @param member The name of the member to be removed.
     * @return The exception is always thrown.
     * @throws UnsupportedOperationException A member cannot be removed 
     *                                       directly.
     */
    public boolean removeMember(Principal member) {
	throw new UnsupportedOperationException();
    }

    /**
     * Removes members from the group.
     * Performs an LDAP modify to remove the LDAP URL.
     *
     * @param members The LDAP URL describing the members to be removed.
     * @return boolean true if the removal was successful; false otherwise.
     * @throws NamingException The exception is thrown if an error occurs
     *         while performing LDAP modify.
     * @throws IllegalStateException The exception is thrown if the
     *         group does not represent a group in the directory.
     */
    public void removeMembers(String members) throws NamingException {

        if (! isBound()) {
            throw new IllegalStateException();
        }
	if (debug) {
	    System.out.println("[debug] removing the members: " + members);
	}
	modifyMembers(members, DirContext.REMOVE_ATTRIBUTE);
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
     * This method is called by {@link LdapGroupFactory}
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
     *         the directory and empty string is returned if no attributes are 
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
     * "GroupOfURLs".
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
     * @throws NamingException If a naming exception is encountered while
     *                         closing the root context.
     */
    public void close() throws NamingException {
	if (rootCtx != null && rootCtx != ctx) {
	    rootCtx.close();
	    rootCtx = null;
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
	this.attributes = attributes;
	memberAttr = attributes != null ? attributes.get(MEMBER_ATTR_ID) : null;
    }

    /*
     * Add or remove a value from the memberURL attribute.
     */
    private void modifyMembers(String members, int mod_op) 
	    throws NamingException {

	Attribute memberURL = new BasicAttribute(MEMBER_ATTR_ID, members);
	modification[0] = new ModificationItem(mod_op, memberURL);
	ctx.modifyAttributes(name, modification);
	memberAttr = null; // invalidate the cache
    }

    /*
     * Checks if the supplied name is a member of any subgroups.
     * Only subgroups are retrieved and explored.
     */
    private boolean isSubgroupMember(String dn) throws NamingException {
	for (NamingEnumeration members =
		(NamingEnumeration)members(GROUPS_ONLY);
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
     * Search for the member using a group LDAP URL.
     */
    private NamingEnumeration searchUsingLdapUrl(String memberUrl,
	SearchControls searchControls, String memberDn) throws NamingException {

	return searchUsingLdapUrl(memberDn, memberUrl, null, searchControls);
    }

    /*
     * Search for the members using a group LDAP URL.
     */
    private NamingEnumeration searchUsingLdapUrl(String memberUrl,
	String filter, SearchControls searchControls) throws NamingException {
	return searchUsingLdapUrl(null, memberUrl, filter, searchControls);
    }

    /*
     * Search for the members using a group LDAP URL.
     * When memberDn is not null then the URL filter is modified to include 
     * matching the values of its RDNs.
     * When andFilter is not null then the URL filter is modified to "and" it
     * with the supplied filter.
     */
    private NamingEnumeration searchUsingLdapUrl(String memberDn,
	String memberUrl, String andFilter, SearchControls searchControls) 
	    throws NamingException {

	LdapURL url = new LdapURL(memberUrl);

	String filter = url.getFilter();
	Object[] filterArgs = null;

	// Modify the URL filter to include the member's DN
	if (memberDn != null) {
	    ArrayList filterAndArgs = restrictFilter(filter, memberDn);
	    filter = (String)filterAndArgs.remove(filterAndArgs.size() - 1);
	    filterArgs = filterAndArgs.toArray();

	// Modify the URL filter to include the supplied filter
	} else if (andFilter != null) {
	    StringBuffer filterBuffer =
		new StringBuffer(filter.length() + andFilter.length() + 3);
	    filter =
		filterBuffer.append("(&").append(filter).append(andFilter)
		    .append(")").toString();
	}

	// Set the scope of the search
	String scope = url.getScope();
	if ("sub".equalsIgnoreCase(scope)) {
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	} else if ("base".equalsIgnoreCase(scope)) {
	    searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
	} else if ("one".equalsIgnoreCase(scope)) {
	    searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
	}

	// Create the root context
	if (rootCtx == null) {
	    // test for the root of the namespace (the empty name)
	    if (ctx.getNameInNamespace().length() == 0) {
		rootCtx = ctx; // context is already a root context
	    } else {
		// Make properties suitable for the root context
		env = GroupOfNames.generateRootContextProperties(env);
		rootCtx = (DirContext) NamingManager.getInitialContext(env);
		// Propagate any context request controls (to rootCtx)
		if (ctx instanceof LdapContext) {
		    ((LdapContext)rootCtx).setRequestControls(
			((LdapContext)ctx).getRequestControls());
		}
	    }
	}

	// Perform the search from the root context
	return memberDn != null 
	    ? rootCtx.search(url.getDN(), filter, filterArgs, searchControls)
	    : rootCtx.search(url.getDN(), filter, searchControls);
    }

    /*
     * Limit the scope of a filter to include the supplied distinguished name.
     *
     * The filter generated by this method is guaranteed to match the entry 
     * that both satisfies the supplied filter and that has the supplied 
     * distinguished name. Note however that applying the generated filter 
     * may return additional entries. This is because while the components of
     * a distinguished name are ordered the components of a filter are not. 
     * Thus the filter will match any distinguished name where all of its 
     * RDNs match the filter in any order.
     * <p>
     * The generated filter is returned as the last element of the returned 
     * ArrayList. It uses the filter expression syntax supported by 
     * {@link javax.naming.directory.DirContext#search(javax.naming.Name,
     * java.lang.String, javax.naming.directory.SearchControls) DirContext.search}
     * <p>
     * For example, a filter such as "(objectClass=person)" and the
     * distinguished name "cn=joe,o=abc,c=us" generates the new filter:
     * "(&(objectClass=person)(cn={0})(o:dn:={1})(c:dn:={2}))"
     * where the variables <code>{i}</code> are attribute values from the
     * distinguished name and are returned in the filter arguments array.
     *
     * @param filter The filter to be modified.
     * @param dn The distinguished name to be included in the filter.
     * @return A list of attribute values extracted from the distinguished name.
     *         Its final element contains the generated filter string.
     * @throws InvalidNameException If the distinguished name is invalid. 
     */
    private static ArrayList restrictFilter(String filter, String dn)
	    throws InvalidNameException {

	StringBuffer filterBuffer =
	    new StringBuffer(filter.length() + 2 * dn.length());
	filterBuffer.append("(&").append(filter);
	int filterIndex = 0;
	ArrayList filterArgs = new ArrayList();
	LdapName name = new LdapName(dn);
	int rdnCount = name.size();
	for (int i = rdnCount; i > 0; i--) {
	    String rdn = (String)name.get(i - 1);

	    // handle multi-valued RDNs
	    int plus = 0;
	    int start = 0;
	    while ((plus = rdn.indexOf('+', start)) >= 0 &&
		rdn.charAt(plus - 1) != '\\') {
		filterArgs.add(appendFilterComponent(filterBuffer,
		    filterIndex++, rdn.substring(start, plus), i == rdnCount));
		start = plus + 1;
	    }
	    if (start == 0) {
		rdn = rdn.substring(start);
	    }
	    filterArgs.add(appendFilterComponent(filterBuffer, filterIndex++,
		rdn, i == rdnCount));
	}
	filterBuffer.append(")");

	// Ugh. Stuff the generated filter into the last element of filterArgs.
	filterArgs.add(filterBuffer.toString());
	return filterArgs;
    }

    /*
     * Generates a filter component from an RDN, appends it to the supplied 
     * filter and returns the RDN's attribute value. The filter component 
     * uses the filter expression syntax supported by 
     * {@link javax.naming.directory.DirContext#search(javax.naming.Name,
     * java.lang.String, javax.naming.directory.SearchControls) DirContext.search}
     * <p>
     * For example, the RDN "cn=joe" produces the filter component "(cn={i})".
     *
     * @param filterBuffer        The filter to append to.
     * @param filterIndex         An index into the filter arguments.
     * @param rdn                 The RDN to examine.
     * @param leastSignificantRdn A flag to indicate the least significant RDN.
     * @return The attribute value in a String or byte array.
     */
    private static Object appendFilterComponent(StringBuffer filterBuffer,
	    int filterIndex, String rdn, boolean leastSignificantRdn) {

	// locate the separator
        int equals = rdn.indexOf('=');
	// extract the attribute ID
        String attrID = rdn.substring(0, equals);
	// extract the (unescaped) attribute value
        Object attrValue =
            LdapName.unescapeAttributeValue(rdn.substring(equals + 1));
	// all but the least significant RDN activate DN-attributes matching
        if (leastSignificantRdn) {
            filterBuffer.append("(").append(attrID).append("={")
                .append(filterIndex).append("})");
        } else {
            filterBuffer.append("(").append(attrID).append(":dn:={")
                .append(filterIndex).append("})");
        }
        return attrValue;
    }


/**
 * The members of a dynamic group.
 */
class Members implements NamingEnumeration {

    private NamingEnumeration memberUrls = null;
    private String filter = null;
    private SearchControls searchControls = null;
    private NamingEnumeration results = null;
    private boolean expandSubgroups;
    private ArrayList subgroups = null;
    private NamingEnumeration subgroupMembers = null;

    /*
     * Create an object for enumerating the members of a dynamic group.
     */
    Members() {
	if (debug) {
	    System.out.println("[debug] constructing an empty GroupOfURLs.Members object");
	}
	expandSubgroups = expandGroup; // GroupOfURLs.expandGroup
    }

    /*
     * Create an object for enumerating the members of a dynamic group.
     *
     * @param memberUrls The group's LDAP URLs.
     * @param filter A filter to apply to the group's members; it may be null.
     * @param searchControls The search constraints used when retrieving group 
     *                       members.
     */
    Members(NamingEnumeration memberUrls, String filter, 
	    SearchControls searchControls) {

	if (debug) {
	    System.out.println("[debug] constructing a GroupOfURLs.Members object");
	}
	this.memberUrls = memberUrls;
	this.filter = filter;
	this.searchControls = searchControls;
	expandSubgroups = expandGroup; // GroupOfURLs.expandGroup
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

	if (memberUrls == null) {
	    return false; // empty
	}

	// Get the first batch of members (if necessary)
	if (results == null) {
	    results = searchUsingLdapUrl((String)memberUrls.next(), filter,
		searchControls);
	}

	if (results.hasMore()) {
	    return true;
	} else if (memberUrls.hasMore()) {
	    // Resolve the next member URL to get the next batch of members
	    results = searchUsingLdapUrl((String)memberUrls.next(),
		filter, searchControls);
	    if (hasMore()) {
		return true;
	    }
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
     * @throws NoSuchElementException if no more members exist or if a
     *         {@link javax.naming.NamingException} was encountered while 
     *         retrieving the next element.
     */
    public Object next() throws NamingException {

	Object object;
	String memberDN;

	if (memberUrls == null) {
	    throw new NoSuchElementException(); // empty
	}

	// Get the first batch of members (if necessary)
	if (results == null) {
	    results = searchUsingLdapUrl((String)memberUrls.next(), filter,
		searchControls);
	}

	if (results.hasMore()) {
	    object = ((SearchResult) results.next()).getObject();

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
		memberDN = ((DirContext)object).getNameInNamespace();
		((DirContext)object).close(); // cleanup
		return (Principal) new LdapPrincipal(memberDN);

	    } else {
		return object; // additional object factories are active
	    }

	} else if (memberUrls.hasMore()) {
	    // Resolve the next member URL to get the next batch of members
	    results = searchUsingLdapUrl((String)memberUrls.next(), filter,
		searchControls);
	    if (hasMore()) {
		return next();
	    }
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
	if (results != null) {
	    results.close(); // cleanup
	}
	if (subgroupMembers != null) {
	    subgroupMembers.close(); // cleanup
	}
    }
}
}
