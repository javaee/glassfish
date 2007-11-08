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

/*
 * HADBConfigurePersistenceInfo.java
 *
 * Created on May 25, 2004, 4:43 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

/*
import com.sun.enterprise.config.ConfigContext;
import java.util.*;
import javax.management.MBeanServer;

import java.util.*;
*/
import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.config.ConfigContext;
import javax.management.MBeanServer;

/**
 * @author  bnevins
 */
public class HADBConfigurePersistenceInfo extends HADBInfo
{
	////////////////////////////////////////////////////////////////////////////
	//////  Public Methods
	////////////////////////////////////////////////////////////////////////////
	
	public HADBConfigurePersistenceInfo(
		String clusterOrDbName, 
		Logger logger, 
		ConfigContext configCtx, 
		MBeanServer mbeanServer, 
		String type, 
		String frequency, 
		String scope, 
		String store, 
		Properties props) throws HADBSetupException
	{
		super(null, null, null, null, clusterOrDbName, logger, configCtx, mbeanServer);

		this.type		= type;
		this.frequency	= frequency;
		this.scope		= scope;
		this.store		= store;
		this.props		= props;
	}
	
	/**
	 * The superclass setup() will check on connecting to hadbm.
	 * This Info object is used exclusively for changes to domain.xml
	 * so that's unneccessary.
	 */	
	public void setup() throws HADBSetupException
	{
		verifyStandaloneCluster();
		setHadbRoot();
	}	

	////////////////////////////////////////////////////////////////////////////
	//////  Package-Private Methods
	////////////////////////////////////////////////////////////////////////////

	@Override
	void validate() throws HADBSetupException
	{
		if(type == null || type.length() <= 0)
			type = null;
		else
			validateOne(type,		LEGAL_TYPES_ARRAY,	"type",			LEGAL_TYPES);
		
		if(frequency == null || frequency.length() <= 0)
			frequency = null;
		else
			validateOne(frequency,	LEGAL_FREQS_ARRAY,	"frequency",	LEGAL_FREQS);
		
		if(scope == null || scope.length() <= 0)
			scope = null;
		else
			validateOne(scope,		LEGAL_SCOPES_ARRAY,	"scope",		LEGAL_SCOPES);
		
		if(store == null || store.length() <= 0)
			store = null;
		
		if(props == null || props.size() <= 0)
			props = null;
		
		// It's an error for the command to do nothing at all!
		if(type == null && frequency == null && scope == null && store == null && props == null)
			throw new HADBSetupException("hadbmgmt-res.NothingToDo");
	}

	////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////
	//////  Private Methods
	////////////////////////////////////////////////////////////////////////////
	
	private final void validateOne(String val, String[] valids, String argName, String validsMessage) throws HADBSetupException
	{
		for(int i = 0; i < valids.length; i++)
			if(val.equals(valids[i]))
				return;
		
		throw new HADBSetupException("hadbmgmt-res.BadArgument", 
			new Object[] {argName, validsMessage});
	}
	
	////////////////////////////////////////////////////////////////////////////
	//////  Variables
	////////////////////////////////////////////////////////////////////////////
	
	String		type; 
	String		frequency;
	String		scope;
	String		store;
	Properties	props;

	private static final String[]	LEGAL_TYPES_ARRAY	= {"memory", "file", "ha", "jdbc-oracle", "jdbc-pointbase", "custom" };
	private static final String		LEGAL_TYPES			= "memory | file | ha | jdbc-oracle | jdbc-pointbase | custom";
	private static final String[]	LEGAL_FREQS_ARRAY	= {"web-method", "time-based", "on-demand" };
	private static final String		LEGAL_FREQS			= "web-method | time-based | on-demand";
	private static final String[]	LEGAL_SCOPES_ARRAY	= {"session", "modified-session", "modified-attribute" };
	private static final String		LEGAL_SCOPES		= "session | modified-session | modified-attribute";
}
