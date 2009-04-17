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

package org.glassfish.admin.amx.j2ee.statistics;

import org.glassfish.admin.amx.util.jmx.OpenMBeanUtil;

import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.openmbean.CompositeData;
import java.io.Serializable;
import java.util.Map;

/**
	Serializable implementation of a BoundaryStatistic
 */
public class BoundaryStatisticImpl extends StatisticImpl
	implements BoundaryStatistic, Serializable
{
	static final long serialVersionUID = -5190567251179453418L;
	
	private long	LowerBound;
	private long	UpperBound;
	
	
		public
	BoundaryStatisticImpl(
		final String	name,
		final String	description,
		final String	unit,
		final long		startTime,
		final long		lastSampleTime,
		final long		lower,
		final long		upper )
	{
		super( name, description, unit, startTime, lastSampleTime );
		
		if ( LowerBound > UpperBound )
		{
			throw new IllegalArgumentException();
		}
		
		LowerBound	= lower;
		UpperBound	= upper;
	}
	
	/**
		Base the Statistic on the {@link CompositeData}
	 */
		public
	BoundaryStatisticImpl( final CompositeData compositeData )
	{
		this( OpenMBeanUtil.compositeDataToMap( compositeData ) );
	}
	
		public
	BoundaryStatisticImpl( final Map<String,?> m )
	{
		this( new MapStatisticImpl( m ) );
	}
	
	
		public
	BoundaryStatisticImpl( final MapStatistic s )
	{
		super( s );
		
		LowerBound	= s.getlong( "LowerBound" );
		UpperBound	= s.getlong( "UpperBound" );
	}
	
		public
	BoundaryStatisticImpl( final BoundaryStatistic s )
	{
		super( s );
		
		LowerBound	= s.getLowerBound();
		UpperBound	= s.getUpperBound();
	}
	
		public long
	getLowerBound()
	{
		return( LowerBound );
	}
	
		public long
	getUpperBound()
	{
		return( UpperBound );
	}
}





