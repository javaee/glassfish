/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/jsr77/J2EEManagementEvent.java,v 1.4 2004/10/14 19:07:03 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/10/14 19:07:03 $
 */
 
package com.sun.cli.jmxcmd.jsr77;



/**
 */
public interface J2EEManagementEvent
{
	/**
		The name of the managed object that generated this event.
		
 		@return the ObjectName of the object, as a String
	 */
	public String	getSource();
	
	/**
		The time of the event represented as a long, whose value is
		the number of milliseconds since January 1, 1970, 00:00:00.
	 */
	public long			getWhen();
	
	/**
		The sequence number of the event.
		Identifies the position of the event in a stream
		of events. The sequence number provides a means of
		determining the order of sequential events that
		occurred with the same timestamp (within the
		minimum supported unit of time).
	 */
	public long			getSequence();
	
	/**
		The type of the event. State manageable objects generate a
		J2EEEvent object with the type attribute set to "STATE"
		whenever they reach the RUNNING, STOPPED or FAILED states.
	 */
	public String		getType();
	
	/**
		An informational message about the event.
	 */
	public String		getMessage();
}
