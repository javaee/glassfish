/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.cli.jmxcmd.util;

import javax.management.ObjectName;

public final class ObjectNameComparator implements java.util.Comparator<ObjectName>
{
	public static final ObjectNameComparator	INSTANCE	= new ObjectNameComparator();
	
	private	ObjectNameComparator()	{}
	
		public int
	compare( final ObjectName o1, final ObjectName o2 )
	{
	    int result  = 0;
	    
	    if ( o1 == null && o2 == null )
	    {
	        result  = 0;
	    }
	    else if ( o1 == null )
	    {
	        result  = -1;
	    }
	    else if ( o2 == null )
	    {
	        result  = 1;
	    }
	    else
	    {
    		final String name1	= o1.getCanonicalName();
    		final String name2	= o2.getCanonicalName();
    		
    		result  = name1.toString().compareTo( name2.toString() );
		}
		
		return result;
	}
	
		public boolean
	equals( final Object other )
	{
		return( other instanceof ObjectNameComparator );
	}
}


