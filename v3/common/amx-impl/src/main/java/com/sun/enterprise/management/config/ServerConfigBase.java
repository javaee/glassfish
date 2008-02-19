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
package com.sun.enterprise.management.config;

import java.util.Map;
import java.util.Set;

import com.sun.enterprise.management.config.AMXConfigImplBase;

import com.sun.enterprise.management.support.Delegate;
import com.sun.enterprise.management.support.AMXAttributeNameMapper;

import org.glassfish.admin.amx.util.Issues;

/**
 */
public class ServerConfigBase extends AMXConfigImplBase
{
		public
	ServerConfigBase(
		final String 		j2eeType,
		final Delegate		delegate )
	{
		super( delegate);
		
	}

		protected void
	addCustomMappings( final AMXAttributeNameMapper mapper )
	{
	    super.addCustomMappings( mapper );
	    
		// these require custom mappings due to different names...
		mapper.matchName( "ReferencedConfigName", "ConfigRef");
		mapper.matchName( "ReferencedNodeAgentName", "NodeAgentRef" );
	}


	private static final String	TEMPLATE_PREFIX	= "${";
		public boolean
	isTemplateString( final String s )
	{
		// at least one character must be between the {} 
		return( s != null &&
			s.startsWith( TEMPLATE_PREFIX ) &&
			s.indexOf( "}" ) >= TEMPLATE_PREFIX.length() + 1 );
	}
	
		public String
	resolveTemplateString( final String template )
	{
		String	result	= template;
		
        Issues.getAMXIssues().notDone( "ServerConfigBase.resolveTemplateString(): support for ${...} template resolution" );
        
		if ( isTemplateString( template ) )
		{
            /*
			final String			myName		= getName();
			final OldDomainMBean	oldDomain	= getOldConfigProxies().getOldDomainMBean();
			
   			try
   			{
   				result	= oldDomain.resolveTokens( template, myName);
   			}
   			catch( Exception e )
   			{
   				getMBeanLogger().warning( "Can't resolve: " + template + ", " + e);
   				e.printStackTrace();
   				throw new IllegalArgumentException( template );
   			}
            */
		}
		return( result );
	}
	
}








