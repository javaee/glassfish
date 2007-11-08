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
 * HADBConfigurePersistence.java
 *
 * Created on May 25, 2004, 5:16 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.serverbeans.Config;
import java.util.*;

/**
 *
 * @author  bnevins
 */
public class HADBConfigurePersistence
{
	////////////////////////////////////////////////////////////////////////////
	//////  Public Methods
	////////////////////////////////////////////////////////////////////////////
	
	public HADBConfigurePersistence(HADBConfigurePersistenceInfo info) throws HADBSetupException
	{
		this.info = info;
		info.validate();
	}
	
	public Object[] commit() throws HADBSetupException
	{
		try
		{
			Config config = info.getConfigForCluster();
			AvailabilityService avail = config.getAvailabilityService();
			WebContainerAvailability webAvail = avail.getWebContainerAvailability();
			EjbContainerAvailability ejbAvail = avail.getEjbContainerAvailability();

			ejbAvail.setSfsbPersistenceType(	"ha",			BaseConfigMBean.OVERWRITE);
			ejbAvail.setSfsbHaPersistenceType(	"ha",			BaseConfigMBean.OVERWRITE);
			ejbAvail.setSfsbCheckpointEnabled(	"true",			BaseConfigMBean.OVERWRITE);
			ejbAvail.setAvailabilityEnabled(	"true",			BaseConfigMBean.OVERWRITE);
			
			if(info.store != null)
				webAvail.setHttpSessionStorePoolName(info.store, BaseConfigMBean.OVERWRITE);
			
			if(info.type != null)
				webAvail.setPersistenceType(info.type, BaseConfigMBean.OVERWRITE);
			
			if(info.frequency != null)
				webAvail.setPersistenceFrequency(info.frequency, BaseConfigMBean.OVERWRITE);
			
			if(info.scope != null)
				webAvail.setPersistenceScope(info.scope, BaseConfigMBean.OVERWRITE);

			if(info.props != null)
			{
				Set set = info.props.entrySet();
				
				for(Iterator it = set.iterator(); it.hasNext(); )
				{
					ElementProperty ep =	entryToProp((Map.Entry)it.next());
					ElementProperty old =	webAvail.getElementPropertyByName(ep.getName());
					
					// if one doesn't do this -- an Exception is thrown if it pre-exists
					if(old != null) 
						webAvail.removeElementProperty(old, BaseConfigMBean.OVERWRITE);
					
					webAvail.addElementProperty(ep, BaseConfigMBean.OVERWRITE);
				}
			}
			
			return info.prepMsgs();

		}
		catch(ConfigException ce)
		{
			throw new HADBSetupException("hadbmgmt-res.StaleWriteAvailability", ce);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	//////  Package Methods
	////////////////////////////////////////////////////////////////////////////
	
	////////////////////////////////////////////////////////////////////////////
	//////  Private Methods
	////////////////////////////////////////////////////////////////////////////

	private ElementProperty entryToProp(Map.Entry entry) throws StaleWriteConfigException
	{
		ElementProperty ep = new ElementProperty();
		ep.setName((String)entry.getKey(),		BaseConfigMBean.OVERWRITE);
		ep.setValue((String)entry.getValue(),	BaseConfigMBean.OVERWRITE);
		return ep;
	}

	
	////////////////////////////////////////////////////////////////////////////
	//////  Variables
	////////////////////////////////////////////////////////////////////////////

	private HADBConfigurePersistenceInfo info;
}
