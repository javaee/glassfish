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

import javax.naming.spi.DirObjectFactory;
import javax.naming.*;
import javax.naming.directory.*;

import java.util.Hashtable;
import org.omg.CORBA.ORB;

import com.sun.jndi.toolkit.corba.CorbaUtils;

/**
  * A DirObjectFactory that returns a omg.org.CORBA.Object when given
  * attributes that contain the objectclass "corbaObject".
  * The form of <tt>getObjectInstance()</tt> that does not accept an
  * <tt>Attributes</tt> parameter always return null because using it
  * would impose too much overhead (incurs an extra search each time).
  * The caller should always use the form of <tt>getObjectInstance()</tt>
  * that accepts an <tt>Attributes</tt> parameter. This is the case if
  * the service provider uses <tt>DirectoryManager</tt>.
  *<p>
  * The LDAP schema for CORBA objects is:
  *<blockquote>
  * objectClass: top, corbaObject, corbaContainer
  * corbaIor: IOR of CORBA object
  *</blockquote>
  *
  * @author Rosanna Lee
  */
public class AttrsToCorba implements DirObjectFactory {
    public AttrsToCorba() {
    }

    /**
      * Returns a CORBA object if attributes represents a "corbaObject".
      * <p>
      * If <tt>attrs</tt>'s <tt>objectclass</tt> attribute has the value
      * "corbaObject" or "corbaobject", this factory will use the directory
      * entry's "corbaIor" attribute to get the stringified IOR, which is then
      * converted into a CORBA object. The "corbaIor" attribute is obtained
      * from <tt>attrs</tt>, and if not available from there, from the directory
      * using the <tt>DirContext</tt>, <tt>orig</tt>.
      *
      * @param orig The non-null DirContext object representing the directory entry;
      * 	If not a DirContext, return null.
      * @param name Ignored
      * @param ctx  Ignored
      * @param env The possibly null environment properties. The caller can pass
      *		the ORB to use (via the <tt>java.naming.corba.orb</tt> property)
      *		or pass properties like the applet and the org.omg.CORBA.* 
      *		properties for creating the ORB. If none of these is available,
      * 	a default (static) ORB is used.
      * @param attrs The possibly null attributes containing at least the
      *		directory entry's <tt>objectclass</tt> attribute. 
      * @return The CORBA object represented by the directory entry; null if 
      * 	the entry does not represent a CORBA object.
      * @exception NamingException If an error is encountered while getting the
      * 	corbaIor attribute using <tt>orig</tt>.
      * @exception Exception If an error occurred while converting the IOR to
      * 	a CORBA object or using the ORB.
      */
    public Object getObjectInstance(Object orig, Name name, Context ctx,
	Hashtable env, Attributes attrs) throws Exception {
	    Attribute oc;
	    if (attrs != null && 
		(oc = attrs.get("objectclass")) != null &&
		(oc.contains("corbaObject") || oc.contains("corbaobject")) && 
		orig instanceof DirContext) {

		// See if IOR already available
		Attribute iorAttr = attrs.get("corbaIor");
		
		if (iorAttr == null) {
		    // have to get from directory
		    attrs = ((DirContext)orig).getAttributes("",
			new String[]{"corbaIor"});
		    iorAttr = attrs.get("corbaIor");
		}

		String ior = null;
		if (iorAttr != null && (ior = (String)iorAttr.get()) != null) {

		    // Use ORB supplied or default ORB
		    ORB orb = (env != null) ? 
			(ORB)env.get("java.naming.corba.orb") : null;
		    if (orb == null) {
			orb = getDefaultOrb(env);
		    }

		    // Convert IOR to Object
		    return orb.string_to_object(ior);
		}

		// %%% else should we indicate any error or just ignore
	    }

	    // depend on 
	    return null; 
    }

    /**
     * Always returns null.
     * @param orig Ignored
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @return null
     * @exception Exception Never thrown
     */
    public Object getObjectInstance(Object orig, Name name, Context ctx,
	Hashtable env) throws Exception {
	    // Too expensive if we must fetch attributes each time,
	    // effectively doubling all calls
	    return null;
    }

    //private static ORB defaultOrb = null;

    /**
     * Return default ORB.
     * The ORB is used for turning a stringified IOR into an Object.
     *
     * @param env The possibly null environment properties. The caller can pass
     * 		the ORB to use (via the <tt>java.naming.corba.orb</tt> property)
     * 		or pass properties like the applet and the org.omg.CORBA.* 
     * 		properties for creating the ORB.
     * @return A non-null ORB.
     */
    private static ORB getDefaultOrb(Hashtable env) {
	return CorbaUtils.getOrb(null, -1, env);
    }
}
