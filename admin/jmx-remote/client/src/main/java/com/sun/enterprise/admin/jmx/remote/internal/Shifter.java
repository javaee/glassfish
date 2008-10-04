/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/* Shifter.java
 * $Id: Shifter.java,v 1.3 2005/12/25 04:26:34 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:34 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.jmx.remote.internal;


/**
 *
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
public final class Shifter {
	
	private Object[] args;
	public Shifter(Object[] in) {
		if (in == null)
			throw new IllegalArgumentException("null array");
		this.args = new Object[in.length];
		System.arraycopy(in, 0, args, 0, in.length);
	}
	
	public void shiftRight(Object addition) {
		if (addition == null)
			throw new IllegalArgumentException ("Null argument");
		final Object[] tmp = new Object[args.length + 1];
		tmp[0] = addition;
		for (int i = 0 ; i < args.length ; i++) {
			tmp[i + 1] = args[i];
		}
		args = tmp;
	}
	
	public Object shiftLeft() {
		if (args.length == 0)
			throw new IllegalStateException("Can't Shift left, no elements");
		final Object ret = args[0];
		final Object[] tmp = new Object[args.length - 1];
		for (int i = 0 ; i < tmp.length ; i++) {
			tmp[i] = args[i + 1];
		}
		args = tmp;
		return ( ret );
	}
	
	public Object[] state() {
		return ( args );
	}
}
