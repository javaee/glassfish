/*
 * @(#)AttrsToCorba.java	1.1 99/05/09
 *
 * Copyright 1999 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
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

    private static ORB defaultOrb = null;

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
