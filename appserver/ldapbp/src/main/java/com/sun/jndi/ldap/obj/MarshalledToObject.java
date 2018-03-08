/*
 * @(#)MarshalledToObject.java	1.1 99/05/09
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

import javax.naming.spi.*;
import javax.naming.*;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.rmi.MarshalledObject;

/**
  * An DirObjectFactory that returns the unmarshalled object from a 
  * MarshalledObject.
  * For example, a Remote/JRMP object is stored as MarshalledObject. 
  * Use this factory to return its unmarshalled form (e.g., the Remote object).
  *
  * @author Rosanna Lee
  */

public class MarshalledToObject implements DirObjectFactory {

    public MarshalledToObject() {
    }

    /**
     * Unmarshals a MarshalledObject.
     *
     * @param orig The possibly null object to check.
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @param attrs The possibly attributes containing the "objectclass"
     * @return The non-null unmarshalled object if <tt>orig</tt> is a 
     * 	MarshalledObject; otherwise null
     * @exception IOException If problem unmarshalling the object
     * @exception ClassNotFoundException If cannot find class required to unmarshal.
     */
    public Object getObjectInstance(Object orig, Name name, Context ctx,
	Hashtable env, Attributes attrs) throws Exception {
	    Attribute oc;

	    if (orig instanceof MarshalledObject &&
		attrs != null && 
		(oc = attrs.get("objectclass")) != null &&
		(oc.contains("javaMarshalledObject") 
		    || oc.contains("javamarshalledobject"))) {
		return ((MarshalledObject)orig).get();
	    }
	    return null;
    }

    /**
     * Unmarshals a MarshalledObject.
     *
     * @param orig The possibly null object to check.
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @return The non-null unmarshalled object if <tt>orig</tt> is a 
     * 	MarshalledObject; otherwise null
     * @exception IOException If problem unmarshalling the object
     * @exception ClassNotFoundException If cannot find class required to unmarshal.
     */
    public Object getObjectInstance(Object orig, Name name, Context ctx,
	Hashtable env) throws Exception {

	    if (orig instanceof MarshalledObject) {
		return ((MarshalledObject)orig).get();
	    }

	    return null;
    }
}    
