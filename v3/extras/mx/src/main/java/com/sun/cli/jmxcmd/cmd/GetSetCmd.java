/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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

/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/GetSetCmd.java,v 1.2 2003/12/17 04:31:11 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/12/17 04:31:11 $
 */
 
package com.sun.cli.jmxcmd.cmd;


import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.*;

import com.sun.cli.jcmd.framework.CmdEnv;


/**
	Base class for GetCmd and SetCmd.
 */
public abstract class GetSetCmd extends JMXCmd
{
		protected
	GetSetCmd( final CmdEnv env )
	{
		// disallow instantiation
		super( env );
	}
	
		String
	getAttributes()
	{
		// guaranteed to be at least one
		return( getOperands()[ 0 ] );
	}
	
		protected String []
	getTargets()
	{
		final String []		operands	= getOperands();
		String []	targets	= null;
		
		if ( operands.length == 1 )
		{
			// one operand; that is the attribute list
			// so get attributes on current target
			targets	= getEnvTargets( );
		}
		else
		{
		
			// first operand is attributes, subsequent are the targets
			targets	= new String [ operands.length - 1 ];
			
			for( int i = 0; i < targets.length; ++i )
			{
				targets[ i ]	= operands[ i + 1 ];
			}
		}
		
		return( targets );
	}
}


















