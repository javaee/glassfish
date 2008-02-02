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
package com.sun.enterprise.management.support;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.HashMap;
import java.lang.reflect.Field;

import com.sun.appserv.management.base.AMX;

import com.sun.appserv.management.util.misc.ArrayUtil;
import com.sun.appserv.management.util.misc.TimingDelta;


/**
	Map all types from XTypes to their respective MBean interfaces.
	
	@see AllTypesMapper
	@see com.sun.appserv.management.j2ee.J2EETypesMapper
 */
public class TypesMapper
{
	private final Map<String,Class<? extends AMX>>	mTypeToInterface;
    
    /**
        Array[i] should be the type, [i+1] should be the interface, [i+2] the impl class
     */
        public
	TypesMapper( final Object[] typesAndInterfaces )
	{
		mTypeToInterface	= new HashMap<String,Class<? extends AMX>>();
        
        for( int i = 0; i < typesAndInterfaces.length; i +=2  )
        {
            final String j2eeType = (String)typesAndInterfaces[i];
            final Class<? extends AMX>          intf = (Class<? extends AMX>)typesAndInterfaces[i+1];
            mTypeToInterface.put( j2eeType, intf );
        }
	}
	
	/**
		Return the Class associated with a given type.
	 */
		public Class<? extends AMX>
	getInterfaceForType( final String type )
	{
		final Class theClass	= mTypeToInterface.get( type );
		
		return( theClass );
	}
	
	    public Set<String>
	getJ2EETypes()
	{
	    return Collections.unmodifiableSet( mTypeToInterface.keySet() );
	}
	
	    public Set<Class<? extends AMX>>
	getClasses()
	{
	    final Set<Class<? extends AMX>>    classes = new HashSet<Class<? extends AMX>>();
	    classes.addAll( mTypeToInterface.values() );
	    return Collections.unmodifiableSet( classes );
	}
}











