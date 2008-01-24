/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.appserv.management.base;

import java.util.Map;

import java.io.Serializable;



/**
	Interface which states that the object can be converted into a Map.
	Type parameter <T> must be Object or Serializable.
 */
public interface MapCapable
{
	/**
		To be capable of being serialized and deserialized by generic clients, the following
		rules must be observed for the resulting Map.
		<p>
		The Map itself and any items it references, directly or indirectly must be of a class
		of one of the following:
		<ul>
		<li>any JMX OpenType</li>
		<li>any serializable Collection in java.util whose items meet these rules</li>
		<li>arrays of items whose values meet these rules</li>
		<li>any Throwable found in java. or javax. </li>
		<li>any MapCapable which can produce a Map following these rules</li>
		</ul>
		@return an equivalent Map representing the original object.
	 */
	public Map<String,Serializable> asMap();
	
	/**
		Return the interface that this Map represents
		(the Java classname).
	 */
	public String	getMapClassName();
	
	/**
		The key for the type of Object this Map represents;
		all MapCapable objects will place their classname keyed by this key
		as a String into the Map returned from asMap().
	 */
	public static final String	MAP_CAPABLE_CLASS_NAME_KEY	= "MapCapableClassName";
}








