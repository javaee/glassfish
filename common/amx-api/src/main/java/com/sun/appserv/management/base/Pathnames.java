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
import java.util.Set;

/**
    The Pathnames MBean--resolves path names to AMX MBeans.  Each AMX MBean
    has a "pathname"; see {@link AMX#getPathname} and {@link AMX#getPathnamePart}.  These
    dotted names are typically used by the 'asadmin' command line, but can be used by other
    clients as well.
    <p>
    The dotted name hierarchy starts at 'root'.  Examples of dotted names:
    <ul>
    <li>root -- represents {@link com.sun.appserv.management.DomainRoot}</li>
    <li>root.SystemStatus -- represents {@link com.sun.appserv.management.base.SystemStatus}</li>
    <li>root.domain -- represents {@link com.sun.appserv.management.config.DomainConfig}</li>
    <li>root.domain.servers.server -- represents {@link com.sun.appserv.management.config.ServerConfig 'server'}</li>
    </ul>
	<p>
    A 'target' of a dotted name is the AMX MBean to which it resolves.
    <p>
   @since GlassFish V3
 */
public interface Pathnames extends AMX, Singleton
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE	= "X-Pathnames";
    
    public static final String SEPARATOR = "/";
    
    /**
       Resolve a single pathname to an AMX.  Wildcards may <em>not</em> be used.
       
       @see AMX#getPathname
       @see AMX#getPathnamePart
       @return the AMX, or null if not found
        @since GlassFish V3
     */
    public AMX  getPathnameTarget( final String pathname );
    
    /**
       Resolve any number of path names.  The resulting Map is keyed by path name,
       and values are of type {@link AMX}.  Wildcards may <em>not</em> be used.  Path names which
       do not resolve will have a null value in the Map.  Attribute specifies should not
       be included in the pathname.
     */
    public Map<String,AMX>  getPathnameTargetMap( final String[] pathnames );
    
    /**
        Get all AMX MBeans, keyed by their path names.
        @since GlassFish V3
     */
    public Map<String,AMX>  getAllPathnameTargetsMap();
    
    /**
        Get all dotted names.  This is equivalent to {@link #getAllPathnameTargetsMap}.keySet().
        <b>Will be removed (probably)</b>
        @since GlassFish V3
     */
    public String[]  getAllPathnames();
    
    /**
        Get all values for a pathname.
     */
    public Map<String,String>  getPathnameValues( final String pathname );
    
    /**
        Get values for all the requested path names.  If a dotted name value does not exist,
        then it is not returned in the Map.  Passing null for the Set returns all values
        for all dotted names (relatively expensive operation).
        <p>
        <b>NOTE:</b> this method does not support wildcards.
        @since GlassFish V3
     */
    public Map<String,String> getManyPathnameValues( final Set<String> pathnames );
    
    
    /** temporary, will be removed */
    public String testResolve();
    
    /**
        Get the value of a single dotted name.
     */
    public String pathnameGetSingleValue( final String dottedName );
    
    
	/**
		Return an array of values corresponding to each dotted-name.
		Each slot in the array will contain either an Attribute or an Exception.
	 */
	public Object[]	pathnameGet( String[] names );
	
	/**
		Return a value for a dotted-name.  If a name does not exist,
		then null is returned.
		@param name
	 */
	public Object	pathnameGet( String name );
	
	/**
		List all valid prefixes for dotted names
		@param names
	 */
	public Object[]	pathnameList( String[] names );
	
	
	/**
		Set values for dotted names; each entry must be of the form:
		<pre>
            <i>dotted-name</i>=<i>value</i>
        </pre> 
		@param nameValuePairs
	 */
	public Object[]	pathnameSet( String[] nameValuePairs );

    public String dumpPathnames();
}



