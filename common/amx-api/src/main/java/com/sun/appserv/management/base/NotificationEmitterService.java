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

package com.sun.appserv.management.base;

import javax.management.Notification;

/**
	Enables the emitting of JMX Notifications by any code with
	access to an MBean implementing this interfaces. Targeted for use by
	server code which has no corresponding MBean from which to emit
	a Notification.
	<p>
	@since AppServer 9.0
 */
public interface NotificationEmitterService
	extends AMX
{
	/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE			= XTypes.NOTIFICATION_EMITTER_SERVICE;
	
	/**
		Emit an already-formed Notification. Any entity may invoke
		this method, even clients, but by convention only server
		code should do so.  
		<p>
		Notifications emitted through this method usually return
		an Object from getSource() which is not an ObjectName; if an
		ObjectName is available then the Notification should usually be
		emitted by that MBean directly.  There may be exceptions to this,
		for example Notifications emitted on behalf of MBeans found in
		remote server instances. Otherwise, by convention getSource()
		should return a String representing the source, and the same
		String should always be used for the same source.  Furthermore,
		the String should never be translated so that the same source
		will be seen regardless of the language in which the server
		is running.
		<p>
		Notifications are not checked for duplication; callers should
		ensure that for any given source (as returned from Notification.getSource())
		that the sequence number is monotonically increasing.
		<p>
		Excluding standard JMX Notification types, Notifications emitted
		by an AMX MBean implementing this interface must follow the AMX conventions:
		<ul>
		<li>getUserData() must return a java.util.Map (possibly null)</li>
		<li>the Notification as a whole must be Serializable</li>
		<li>for instances of this interface which are part of AMX,
			getType() must return a String defined in an AMX interface.</li>
		</ul>
		<p>
		@param notif
		@see com.sun.appserv.management.util.jmx.NotificationBuilder
		
	 */
	public void emitNotification( Notification notif );
	
	/**
		@return number of listeners
	 */
	public int	getListenerCount();
}
