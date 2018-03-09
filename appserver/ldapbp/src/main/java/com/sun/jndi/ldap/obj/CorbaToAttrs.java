/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1999-2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.naming.spi.DirStateFactory;
import javax.naming.*;
import javax.naming.directory.*;
import java.util.Hashtable;
import org.omg.CORBA.portable.ObjectImpl;

/**
  * A DirStateFactory that returns an Attributes when
  * given a omg.org.CORBA.Object.
  * The form of <tt>getStateToBind()</tt> that does not accept an
  * <tt>Attributes</tt> parameter always return null because this
  * factory needs to return <tt>Attributes</tt>.
  * The caller should always use the form of <tt>getStateToBind()</tt>
  * that accepts an <tt>Attributes</tt> parameter. This is the case if
  * the service provider uses <tt>DirectoryManager</tt>.
  *<p>
  * The LDAP schema for CORBA objects is:
  *<blockquote>
  * objectClass: top, corbaObject, corbaContainer, corbaObjectReference
  * corbaIor: IOR of CORBA object
  *</blockquote>
  *
  * @author Rosanna Lee
  */
public class CorbaToAttrs implements DirStateFactory {
    public CorbaToAttrs() {
    }

    /**
     * Returns attributes required for storing a CORBA object.
     * Get the IOR from <tt>orig</tt> and use it for the "corbaIor" attribute.
     * Add "corbaObject" to "objectclass" attribute. If there are no
     * other objectclass attribute values, the entry needs a structural
     * objectclass: add "corbaContainer" as an additional objectclass.
     *
     * @param orig The CORBA object to bind. If not an instance of
     * 		   org.omg.CORBA.portable.ObjectImpl, return null.
     * @param name Ignored
     * @param ctx  Ignored
     * @param env  Ignored
     * @param inAttrs A possibly null set of attributes that will accompany 
     * 		this bind. These attributes are combined with those required
     *		for storing <tt>orig</tt>.
     * @return {null, attrs} where <tt>attrs</tt> is the union of 
     * <tt>inAttrs</tt> and attributes that represent the CORBA object 
     * <tt>orig</tt>. null if <tt>orig</tt> is not an instance of
     * <tt>ObjectImpl</tt>.
     * @exception NamingException Not thrown.
     */
    public DirStateFactory.Result 
    getStateToBind(Object orig, Name name, Context ctx,
	Hashtable env, Attributes inAttrs) throws NamingException {
	    if (orig instanceof org.omg.CORBA.portable.ObjectImpl) {

		// Turn org.omg.CORBA.Object into attrs
		return new DirStateFactory.Result(null, 
		    corbaToAttrs((org.omg.CORBA.portable.ObjectImpl)orig, inAttrs));
	    }
	    return null; // pass and let next state factory try
    }

    /**
     * Always return null.
     * @param orig Ignored
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @exception NamingException Not thrown
     */
    public Object getStateToBind(Object orig, Name name, Context ctx,
	Hashtable env) throws NamingException {

	    // Cannot just return obj; needs to return Attributes
	    return null;
    }

    /**
     * Returns attributes required for storing a CORBA object.
     * Get the IOR from <tt>orig</tt> and use it for the "corbaIor" attribute.
     * Add "corbaObject" to "objectclass" attribute. If there are no
     * other objectclass attribute values, the entry needs a structural
     * objectclass: add "corbaContainer" as an additional objectclass.
     *
     * @param orig The non-null ObjectImpl from which to get the IOR
     * @param inAttrs The possibly attribute set that is to be merged with the
     * 		CORBA attributes.
     * @return A non-null Attributes containing the incoming attribute merged
     * with the CORBA attributes.
     */
    static Attributes
	corbaToAttrs(org.omg.CORBA.portable.ObjectImpl orig, Attributes inAttrs) {

	DirStateFactory.Result res;

	// Get holder for outgoing attributes
	Attributes outAttrs = (inAttrs != null) ?
	    (Attributes)inAttrs.clone() : new BasicAttributes(true);

	// Put IOR 
	String ior = orig._orb().object_to_string(orig);
	outAttrs.put("corbaIor", ior);

	// Put appropriate object class
	Attribute objectClass = (Attribute)outAttrs.get("objectClass");
	if (objectClass == null && !outAttrs.isCaseIgnored()) {
	    // %%% workaround 
	    objectClass = (Attribute)outAttrs.get("objectclass");
	}

	if (objectClass == null) {
	    // No objectclasses supplied
	    objectClass =  new BasicAttribute("objectClass", "top");
	    objectClass.add("corbaContainer");
	} else {
	    // Clone existing objectclass
	    objectClass = (Attribute)objectClass.clone();
	}

	objectClass.add("corbaObject");
	objectClass.add("corbaObjectReference");
	outAttrs.put(objectClass);

	return outAttrs;
    }
}
