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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/DisallowedOptionDependency.java,v 1.4 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:18 $
 */
package com.sun.cli.jcmd.util.cmd;


/**
	A dependency denoting a disallowed option (one that may not ocurr in conjunction
	with the option having this dependency).
 */
public final class DisallowedOptionDependency extends OptionDependency
{
	/**
		Create a new instance specifying that these OptionInfos are
		not allowed in conjunction with an option carrying this dependency.
	 */
		public
	DisallowedOptionDependency( OptionInfo[] refs )
	{
		super( refs );
	}
	
	/**
	 */
		public
	DisallowedOptionDependency( OptionInfo ref )
	{
		this( new OptionInfo[] { ref } );
	}
	
		public
	DisallowedOptionDependency( OptionInfo ref1, OptionInfo ref2 )
	{
		this( new OptionInfo[] { ref1, ref2 } );
	}
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3  )
	{
		this( new OptionInfo[] { ref1, ref2, ref3 } );
	}
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4  )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4 } );
	}
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4,  
		OptionInfo		ref5 )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4, ref5 } );
	}
	
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4,  
		OptionInfo		ref5 ,
		OptionInfo		ref6 )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4, ref5, ref6 } );
	}
	
	
		public
	DisallowedOptionDependency(
		OptionInfo		ref1,
		OptionInfo		ref2,
		OptionInfo		ref3, 
		OptionInfo		ref4,  
		OptionInfo		ref5 ,
		OptionInfo		ref6,
		OptionInfo		ref7 )
	{
		this( new OptionInfo[] { ref1, ref2, ref3, ref4, ref5, ref6, ref7} );
	}
}

