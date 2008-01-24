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


/**
	Base interface for all OperationStatus objects.
 */
public interface OperationStatus extends MapCapable
{
	/**
		Key used to look up the Throwable (if any) from the Map.
		The value returned is a java.lang.Throwable.
	 */
	public static final String	THROWABLE_KEY		= "ThrowableKey";
	
	/**
		Key used to look up the status code (if any) from the Map.
		The value returned is an Integer whose intValue() is the
		status code.  Corresponds to {@link #getStatusCode}.
	 */
	public static final String	STATUS_CODE_KEY		= "StatusCodeKey";
	
	/**
		If there is no explicit status code, an operation is considered
		successful if nothing was thrown.
		<p>
		Legal status codes include:
		<ul>
		<li>#STATUS_CODE_SUCCESS</li>
		<li>#STATUS_CODE_FAILURE</li>
		<li>#STATUS_CODE_WARNING</li>
		<li><i>any other status codes defined by sub-interfaces/subclasses</i></li>
		</ul>
		
		@return the status code
	 */
	public int			getStatusCode();
	
	/**
		If a Throwable was thrown, this implies some degree of failure
		from partial to total.
		
		@return any Throwable that was thrown
	 */
	public Throwable	getThrowable();
	
	/**
		Status code indicating success of the operation.
	 */
	public final int	STATUS_CODE_SUCCESS	= 2;
	
	/**
		Status code indicating failure of the operation.
	 */
	public final int	STATUS_CODE_FAILURE	= 0;
	
	/**
		Status code indicating success, with warning.
	 */
	public static final int STATUS_CODE_WARNING = 1; 
}








