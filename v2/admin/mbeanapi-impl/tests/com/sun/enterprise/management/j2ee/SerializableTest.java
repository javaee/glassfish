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
package com.sun.enterprise.management.j2ee;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Serializable;


import javax.management.j2ee.statistics.Statistic;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.j2ee.statistics.*;

import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.TypeCast;


import com.sun.enterprise.management.Capabilities;



/**
    Test serialization on the AMX Stats/Statistics classes which travel
    from server to client.
 */
public final class SerializableTest extends junit.framework.TestCase
{
		public
	SerializableTest( )
	{
	}
	
	static final Class[] TESTEES	= new Class[]
	{
		StatsImpl.class,
		CountStatisticImpl.class,
		RangeStatisticImpl.class,
		BoundedRangeStatisticImpl.class,
		BoundaryStatisticImpl.class,
		MapGetterInvocationHandler.class,
		GetterInvocationHandler.class,
	};
	
		protected void
	serializeTest( final Object toSerialize )
		throws IOException, ClassNotFoundException
	{
		final ByteArrayOutputStream	os	= new ByteArrayOutputStream( 2048 );
		
		final ObjectOutputStream	oos	= new ObjectOutputStream( os );
		
		oos.writeObject( toSerialize );
		oos.close();
		
		final byte[]	bytes	= os.toByteArray();
		
		final ObjectInputStream	is	= new ObjectInputStream( new ByteArrayInputStream( bytes ) );
		
		final Object	result	= is.readObject();
		
		assert( result.equals( toSerialize ) ) :
			"Deserialized object not equal: " + toSerialize + " != " + result;
	}
	
	
		public void
	testChecked()
	{
	    final Collection<String>   c   = new HashSet<String>();
	    assert( TypeCast.checkedStringCollection(c) instanceof Serializable );
	    
	    final Set<String>   s   = new HashSet<String>();
	    assert( TypeCast.checkedStringSet(s) instanceof Serializable );
	    
	    final List<String>   l   = new ArrayList<String>();
	    assert( TypeCast.checkedStringList(l) instanceof Serializable );
	    
	    final Map<String,String>   m   = new HashMap<String,String>();
	    assert( TypeCast.checkedStringMap(m) instanceof Serializable );
	}
	
		public void
	testStatsImplRequiresStatistics()
		throws IOException, ClassNotFoundException
	{
		try
		{
		    Statistic x = null;
		    
		    final Statistic s   = new CountStatisticImpl( "x","x","x",0,0,0);
			final Map<String,Statistic>	m	= MapUtil.newMap( "foo", s );
			final StatsImpl	si	= new StatsImpl( m );
			serializeTest( si );
		}
		catch( IllegalArgumentException e )
		{
			// good
		}
	}
	
		public void
	testStatsImpl()
		throws IOException, ClassNotFoundException
	{
		final Map<String,Statistic>	m	= new HashMap<String,Statistic>();
		
		final CountStatisticImpl	c	= new CountStatisticImpl( "Count", "", "number", 0, 0, 99);
		final RangeStatisticImpl	r	= new RangeStatisticImpl( "Range", "", "number", 0, 0, 0, 50, 100);
		final BoundaryStatisticImpl	b	= new BoundaryStatisticImpl( "Boundary", "", "number", 0, 0, 0, 100);
		final BoundedRangeStatisticImpl	br	= new BoundedRangeStatisticImpl( "BoundedRange", "", "number", 0, 0, 0, 50, 100, 0, 100);
		final TimeStatisticImpl	t	= new TimeStatisticImpl( "Time", "", "number", 0, 0, 0, 10, 100, 1000 );

		m.put( c.getName(), c );
		m.put( r.getName(), r );
		m.put( br.getName(), br );
		m.put( b.getName(), b );
		m.put( t.getName(), t );
		
		final StatsImpl	si	= new StatsImpl( m );
		serializeTest( si );
	}

}

