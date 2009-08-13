/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package org.glassfish.admin.amx.util.jmx;

import javax.management.MBeanAttributeInfo;

/**
	This class contains various filters based on read/write status of
	an Attribute.
 */
public class ReadWriteAttributeFilter implements AttributeFilter
{
	protected		ReadWriteAttributeFilter( )	{}
	
	/**
	 */
		public boolean
	filterAttribute( final MBeanAttributeInfo info )
	{
		throw new RuntimeException( "Can't get here" );
	}
	
	public static final ReadWriteAttributeFilter	READ_ONLY_FILTER = new ReadWriteAttributeFilter( )
	{
		public boolean	filterAttribute( final MBeanAttributeInfo info )
		{
			return( info.isReadable() && ! info.isWritable() );
		}
	};
		
	public static final ReadWriteAttributeFilter	READABLE_FILTER =
		new ReadWriteAttributeFilter( )
	{
		public boolean	filterAttribute( final MBeanAttributeInfo info )
		{
			return( info.isReadable() );
		}
	};
		
	public static final ReadWriteAttributeFilter	WRITE_ONLY_FILTER =
		new ReadWriteAttributeFilter()
	{
		public boolean	filterAttribute( final MBeanAttributeInfo info )
		{
			return( info.isWritable() && ! info.isReadable() );
		}
	};
		
	public static final ReadWriteAttributeFilter	WRITEABLE_FILTER =
		new ReadWriteAttributeFilter()
	{
		public boolean	filterAttribute( final MBeanAttributeInfo info )
		{
			return( info.isWritable() );
		}
	};
		
	public static final ReadWriteAttributeFilter	READ_WRITE_FILTER =
		new ReadWriteAttributeFilter()
	{
		public boolean	filterAttribute( final MBeanAttributeInfo info )
		{
			return( info.isWritable() && info.isReadable() );
		}
	};
		
	public static final ReadWriteAttributeFilter	ALL_FILTER =
		new ReadWriteAttributeFilter()
	{
		public boolean	filterAttribute( final MBeanAttributeInfo info )
		{
			return( true );
		}
	};
}






