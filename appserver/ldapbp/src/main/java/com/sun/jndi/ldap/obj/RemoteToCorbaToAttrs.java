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

import java.rmi.Remote;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CORBA.ORB;

import com.sun.jndi.toolkit.corba.CorbaUtils;

/**
  * An DirStateFactory that returns an Attributes when
  * given an RMI-IIOP object.
  * This factory requires CORBA classes and RMI-IIOP for getting 
  * the CORBA object of an Remote object.
  * <p>
  * Try to get the CORBA object for the Remote object (using RMI-IIOP), 
  * and turn that CORBA object into attributes. Return null if the Remote
  * objectis a JRMP implementation or JRMP stub.
  * <p>
  * The LDAP schema for storing CORBA objects is:
  *<blockquote>
  * objectClass: top, corbaObject, corbaContainer
  * corbaIor: IOR of CORBA object
  *</blockquote>
  *
  * @author Rosanna Lee
  */

public class RemoteToCorbaToAttrs implements DirStateFactory {
    public RemoteToCorbaToAttrs() {
    }

    /**
      * Returns the attributes required for storing a CORBA object.
      * Uses the utility supplied by CorbaToAttrs.
      * @param orig A non-null Remote object.
      * @param name Ignored
      * @param ctx Ignored
      * @param env A possibly null environment. Used to get the ORB to use
      *   when getting the CORBA object for the Remote object using RMI-IIOP.
      * @param inAttrs The possibly null attributes included with the bind.
      * @return {null, attrs} where <tt>attrs</tt> is the union of 
      * <tt>inAttrs</tt> and attributes that represent the CORBA object (of)
      * <tt>orig</tt>. null if <tt>orig</tt> cannot be turned into a 
      * <tt>ObjectImpl</tt>, or if RMI-IIOP is not available.
      * @exception ConfigurationException If configuration problems encountered
      *   in getting <tt>orig</tt>'s CORBA object, such as RMI-IIOP not available.
      * @exception NamingException If some other error occurred.
      */
    public DirStateFactory.Result 
    getStateToBind(Object orig, Name name, Context ctx,
	Hashtable env, Attributes inAttrs) throws NamingException {
	    if (orig instanceof Remote) {
		try {
		    return remoteToCorbaToAttrs((Remote)orig, env, inAttrs);
		} catch (ClassNotFoundException e) {
		    // RMI-IIOP library not available
		    throw new ConfigurationException(
			"javax.rmi packages not available");
		}
	    }
	    return null;
    }

    /**
      * Gets the connected IIOP stub for Remote object.
      */
    static DirStateFactory.Result remoteToCorbaToAttrs(
	Remote orig, Hashtable env, Attributes inAttrs) 
	throws ClassNotFoundException, NamingException {
	    org.omg.CORBA.Object corbaObj;

	    if (orig instanceof org.omg.CORBA.Object) {
		// Implements both CORBA and Remote; no need for conversion
		corbaObj = (org.omg.CORBA.Object)orig;
	    } else {

		// Use ORB supplied or default ORB
		ORB orb = (env != null) ? (ORB)env.get("java.naming.corba.orb") 
		    : null;
		if (orb == null) {
		    orb = getDefaultOrb(env);
		}

		// Convert Remote to CORBA using RMI/IIOP
		corbaObj = CorbaUtils.remoteToCorba((Remote)orig, orb);
		if (corbaObj == null) {
		    return null;  // Cannot get CORBA obj; must be JRMP obj
		}
	    }

	    // Convert CORBA object to attributes
	    return new DirStateFactory.Result(null, 
		CorbaToAttrs.corbaToAttrs(
		    (org.omg.CORBA.portable.ObjectImpl)corbaObj, inAttrs));
    }

    /**
     * Always returns null.
     * @param orig Ignored
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @return null
     * @exception NamingException Not thrown.
     */
    public Object getStateToBind(Object orig, Name name, Context ctx,
	Hashtable env) throws NamingException {

	// Cannot just return obj; needs to return Attributes
        return null;
    }

    /**
     * Return default ORB.
     * The ORB is used for getting the tie of a RMI-IIOP object.
     * %%%
     * The assumption is that any ORB will *NOT* do. That's why we can't maintain
     * this as a static. If this assumption is false, save result in static
     * to avoid calling it each time.
     *
     * @param env The possibly null environment properties to use when
     * 		  creating the default ORB.
     * @return A non-null ORB.
     */
    private static ORB getDefaultOrb(Hashtable env) {
	return CorbaUtils.getOrb(null, -1, env);
    }
}

