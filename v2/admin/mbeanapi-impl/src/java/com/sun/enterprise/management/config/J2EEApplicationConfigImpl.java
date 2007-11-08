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

import javax.management.ObjectName;
import java.util.Map;

import com.sun.enterprise.management.support.Delegate;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.base.XTypes;

/**
*/
public final class J2EEApplicationConfigImpl extends DeployedItemConfigBase
	// implements J2EEApplicationConfig
{
		public
	J2EEApplicationConfigImpl( final Delegate delegate )
	{
		super( delegate );
	}
	
    /**
	    A troublesome Attribute which may not exist, hence we need to supply
	    a default value. If it doesn't exist, the Delegate will return null.
	 */
	    public boolean
	getJavaWebStartEnabled()
	{
	    final Object value = delegateGetAttributeNoThrow( "JavaWebStartEnabled" );
	    return Boolean.parseBoolean( "" + value );
	}

    /*
     public Map getWebServiceEndpointConfigObjectNameMap() {
          return(getContaineeObjectNameMap(XTypes.WEB_SERVICE_ENDPOINT_CONFIG));
     }
     */

}









