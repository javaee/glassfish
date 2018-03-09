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

        if (prefix.equals("")) {
            return suffix;
        } else if (suffix == null || suffix.equals("")) {
            return prefix;
        } else {
            StringBuffer buffer = new StringBuffer();
            return buffer.append(prefix).append(",").append(suffix).toString();
        }
    }
}
