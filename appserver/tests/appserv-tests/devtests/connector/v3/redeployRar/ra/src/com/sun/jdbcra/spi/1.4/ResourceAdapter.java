/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jdbcra.spi;

import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.ActivationSpec;
import javax.resource.NotSupportedException;
import javax.transaction.xa.XAResource;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;

/**
 * <code>ResourceAdapter</code> implementation for Generic JDBC Connector.
 *
 * @version	1.0, 02/08/05
 * @author	Evani Sai Surya Kiran
 */
public class ResourceAdapter implements javax.resource.spi.ResourceAdapter {
public static final int VERSION = 2;
    public String raProp = null;

    /**
     * Empty method implementation for endpointActivation
     * which just throws <code>NotSupportedException</code>
     *
     * @param	mef	<code>MessageEndpointFactory</code>
     * @param	as	<code>ActivationSpec</code>
     * @throws	<code>NotSupportedException</code>
     */
    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }
    
    /**
     * Empty method implementation for endpointDeactivation
     *
     * @param	mef	<code>MessageEndpointFactory</code>
     * @param	as	<code>ActivationSpec</code>
     */
    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) {
        
    }
    
    /**
     * Empty method implementation for getXAResources
     * which just throws <code>NotSupportedException</code>
     *
     * @param	specs	<code>ActivationSpec</code> array
     * @throws	<code>NotSupportedException</code>
     */
    public XAResource[] getXAResources(ActivationSpec[] specs) throws NotSupportedException {
        throw new NotSupportedException("This method is not supported for this JDBC connector");
    }
    
    /**
     * Empty implementation of start method
     *
     * @param	ctx	<code>BootstrapContext</code>
     */
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
/*
NO NEED TO CHECK THIS AS THE TEST's PURPOSE IS TO CHECK THE VERSION ALONE

        System.out.println("Resource Adapter is starting with configuration :" + raProp);
        if (raProp == null || !raProp.equals("VALID")) {
	    throw new ResourceAdapterInternalException("Resource adapter cannot start. It is configured as : " + raProp);
	} 
*/
    }
    
    /**
     * Empty implementation of stop method
     */
    public void stop() {
    
    }

    public void setRAProperty(String s) {
        raProp = s;
    }

    public String getRAProperty() {
        return raProp;
    }
    
}
