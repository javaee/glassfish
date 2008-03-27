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
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/stringifier/MBeanFeatureInfoStringifierOptions.java,v 1.2 2005/11/08 22:40:26 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2005/11/08 22:40:26 $
 */
 
package com.sun.cli.jmxcmd.util.jmx.stringifier;

	
public final class MBeanFeatureInfoStringifierOptions
{
	public boolean	mIncludeDescription;
	public String	mArrayDelimiter;
	public boolean	mPretty;
	
	public static final MBeanFeatureInfoStringifierOptions	DEFAULT = new MBeanFeatureInfoStringifierOptions();
	
	
		public
	MBeanFeatureInfoStringifierOptions()
	{
		this( true, "," );
	}
	
	
		public
	MBeanFeatureInfoStringifierOptions( boolean includeDescription, String arrayDelimiter )
	{
		mPretty	= true;
		
		mIncludeDescription	= includeDescription;
		mArrayDelimiter		= arrayDelimiter;
	}
}
