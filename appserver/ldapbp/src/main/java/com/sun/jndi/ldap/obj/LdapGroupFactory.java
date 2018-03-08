/*
 * @(#)LdapGroupFactory.java	1.5 03/04/25
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.jndi.ldap.obj;

import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.spi.*;

/**
 * A state factory and an object factory for handling LDAP groups.
 * The following group objects are supported:
 * <ul>
 * <li> {@link GroupOfNames} 
 * <li> {@link GroupOfUniqueNames} 
 * <li> {@link GroupOfURLs} 
 * </ul>
 *
 * @author Vincent Ryan
 */
public class LdapGroupFactory implements DirStateFactory, DirObjectFactory {

    private static final boolean debug = false;

    public LdapGroupFactory() {
	if (debug) {
	    System.out.println("[debug] constructing LdapGroupFactory object");
	}
    }

    /**
     * The method is not applicable to this class.
     *
     * @return null is always returned.
     */
    public Object getStateToBind(Object obj, Name name, Context nameCtx, 
	    Hashtable environment) throws NamingException {

	return null;
    }

    /**
     * Extracts the attributes that represent the LDAP object.
     */
    public DirStateFactory.Result getStateToBind(Object obj, Name name,
	    Context nameCtx, Hashtable environment, Attributes inAttrs)
	    throws NamingException {

	if (nameCtx instanceof DirContext) {

	    // build the group's distinguished name
	    String groupDN = getName(nameCtx, name);

	    Attributes outAttrs = null;
	    // interested only in group objects
	    if (obj instanceof GroupOfURLs) {
		outAttrs = ((GroupOfURLs)obj).getAttributes();
		// set the group's LDAP context and name
		((GroupOfURLs)obj)
		    .setName(groupDN, (DirContext)nameCtx, name);

	    } else if (obj instanceof GroupOfUniqueNames) {
		outAttrs = ((GroupOfUniqueNames)obj).getAttributes();
		// set the group's LDAP context and name
		((GroupOfUniqueNames)obj)
		    .setName(groupDN, (DirContext)nameCtx, name);

	    } else if (obj instanceof GroupOfNames) {
		outAttrs = ((GroupOfNames)obj).getAttributes();
		// set the group's LDAP context and name
		((GroupOfNames)obj)
		    .setName(groupDN, (DirContext)nameCtx, name);
	    }

	    // merge attribute sets (group's attributes may be overwritten)
	    if (inAttrs != null) {
		for (Enumeration attrs = ((Attributes)inAttrs.clone()).getAll();
		    attrs.hasMoreElements(); ) {
		    outAttrs.put((Attribute)attrs.nextElement());
		}
	    }
	    return new DirStateFactory.Result(null, outAttrs);
	}
	return null;
    }

    /**
     * The method is not applicable to this class.
     *
     * @return null is always returned.
     */
    public Object getObjectInstance(Object obj, Name name, Context ctx,
	Hashtable environment) throws Exception {

	return null;
    }

    /**
     * Creates an object that represents an LDAP object at directory context.
     * The LDAP objectClass attribute is examined to determine which object 
     * to create.
     *
     * @return An LDAP object or null.
     */
    public Object getObjectInstance(Object obj, Name name, Context ctx,
	Hashtable environment, Attributes attributes) throws Exception {

	if (obj instanceof DirContext && ctx instanceof DirContext) {
	    // examine the objectClass attribute
 	    Attribute objectClass =
		(attributes != null ? attributes.get("objectClass") : null);
	    if (objectClass != null) {
		// interested only in group objects
		if (GroupOfURLs.matches(objectClass)) {
		    return GroupOfURLs.getObjectInstance(
			((DirContext)obj).getNameInNamespace(), (DirContext)ctx,
			    name, environment, attributes);

		} else if (GroupOfUniqueNames.matches(objectClass)) {
		    return GroupOfUniqueNames.getObjectInstance(
			((DirContext)obj).getNameInNamespace(), (DirContext)ctx,
			    name, environment, attributes);

		} else if (GroupOfNames.matches(objectClass)) {
		    return GroupOfNames.getObjectInstance(
			((DirContext)obj).getNameInNamespace(), (DirContext)ctx,
			    name, environment, attributes);
		}
	    }
	}
	return null;
    }

    /*
     * Builds a fully distinguished name from a context and relative name.
     */
    private String getName(Context ctx, Name name) throws NamingException {
        String suffix = ctx.getNameInNamespace();
        String prefix = name.toString();

        if (prefix == null || prefix.equals("")) {
            return suffix;
        } else if (suffix == null || suffix.equals("")) {
            return prefix;
        } else {
            StringBuffer buffer = new StringBuffer();
            return buffer.append(prefix).append(",").append(suffix).toString();
        }
    }
}
