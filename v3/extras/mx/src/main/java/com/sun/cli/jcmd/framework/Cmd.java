/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/Cmd.java,v 1.4 2004/07/12 19:42:54 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2004/07/12 19:42:54 $
 */
 

package com.sun.cli.jcmd.framework;

import com.sun.cli.jcmd.util.cmd.CmdInfo;

/**
	A command may be plugged into jmxcmd so long as it implements
	this interface and provides a constructor with the following 
	signature:
	
	cmd( CmdEnv env )
	
	Most commands will want to extend CmdBase or JMXCmd to inherit
	much functionality, rather than implementing this interface
	directly.
 */
public interface Cmd extends CmdOutput
{
	/**
		Execute the command.  The command should throw a CmdException
		with an appropriate error code if an error occurs.
		
		Alternately, it may allow exceptions to propogate out; in this
		case a generic error code will be supplied.
	 */
	public void	execute( ) throws Exception;
	
	/**
		Get a CmdHelp describing for this command.  If there is none,
		return null.
	 */
	public CmdHelp		getHelp( );
	
	/*
		Each Cmd should also implement:
		
		public static CmdInfo[]		getCmdInfos( String name );
	 */
};

