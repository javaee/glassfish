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
package com.sun.cli.jmxcmd.util;

/**
	Maps Attribute names to legal Java identifiers, so that they can
	be exposed in a proxy with get/set routines.
 */
public interface AttributeNameMangler
{
	/**
		Return a legal java identifier corresponding to the Attribute name.
		For names that are already legal, it is advised to not alter them,
		but not required to do so.
		
		@param attributeName	Attribute name
		@return legal Java identifier
	 */
	public String	mangleAttributeName( String attributeName );
}
