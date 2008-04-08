/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/AttributesAccess.java,v 1.1 2004/01/30 20:59:07 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2004/01/30 20:59:07 $
 */

package com.sun.cli.jmxcmd.util.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;

/**
	Formal access to Attributes exactly the same as a DynamicMBean
	offers, with a few additions.
	
	This interface is needed so that creation of client-side proxies to the MBean
	has an API for accessing the Attributes; they may or may not be exposed
	as getters and setters in the MBean interface.
	This interface ensures that all Attributes are available, even if the MBean
	interface used to create the client-side proxy does not declare some or any of them.
 */
public interface AttributesAccess
{
	public Object			getAttribute( String name ) throws AttributeNotFoundException;
	public AttributeList	getAttributes( String[] names );
	
	public void				setAttribute( Attribute attr )
								throws AttributeNotFoundException, InvalidAttributeValueException;
	public AttributeList	setAttributes( AttributeList attrs );
}
