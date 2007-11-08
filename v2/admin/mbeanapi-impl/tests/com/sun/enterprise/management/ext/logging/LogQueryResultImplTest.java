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
package com.sun.enterprise.management.ext.logging;

import java.util.Map;
import java.util.Date;
import java.util.logging.Level;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;

import com.sun.appserv.management.ext.logging.LogQueryResult;
import com.sun.appserv.management.ext.logging.LogQueryResultImpl;
import com.sun.appserv.management.ext.logging.LogQueryEntryImpl;


import com.sun.enterprise.management.Capabilities;

/**
 */
public final class LogQueryResultImplTest extends junit.framework.TestCase
{
		public
	LogQueryResultImplTest( )
	{
	}
	
	private static final String[] FIELD_NAMES  = new String[]
	{
	    "#",
	    "DATE",
	    "LEVEL",
	    "PRODUCT NAME",
	    "MODULE",
	    "MESSAGE",
	    "MESSAGE ID",
	    "NAME VALUE PAIRS",
	};
	
	    public LogQueryResultImpl
    createDummy()
    {
        final LogQueryEntryImpl entry   = LogQueryEntryImplTest.createDummy();
        
        final LogQueryResultImpl impl   =
            new LogQueryResultImpl( FIELD_NAMES, new LogQueryEntryImpl[] { entry } );
       
       return impl;
    }
    
	    public void
    testCreate()
    {
        createDummy().toString().hashCode(); // non-null check
        
        assertEquals( createDummy(), createDummy() );
    }
    
    
        public void
    testEquals()
    {
        final LogQueryResult d   = createDummy();
        
        assertEquals( d, d );
        
        final LogQueryResult dCopy  =
            new LogQueryResultImpl( d.getFieldNames(), d.getEntries() );
       
        assertEquals( d, dCopy );
        assertEquals( dCopy, d );
    }
    
	    public void
    testGetters()
        throws OpenDataException
    {
        final LogQueryResultImpl d   = createDummy();
        
        d.getFieldNames();
        d.getEntries();
       // d.getCompositeType();
        //d.asCompositeData();
    }
    
    /*
	    public void
    testCompositeData()
        throws OpenDataException
    {
        final LogQueryResultImpl d   = createDummy();
        
        final CompositeData       data    = d.asCompositeData();
        final LogQueryResultImpl  copy = new LogQueryResultImpl( data );
        
        assertEquals( d, copy );
    }
    */
	
}









