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
package org.glassfish.admin.amx.base;

import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXProxy;

import java.util.Map;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.base.Singleton;
import org.glassfish.admin.amx.base.Utility;
import org.glassfish.api.amx.AMXMBeanMetadata;

/**
    @since GlassFish V3
 */
@AMXMBeanMetadata(leaf=true, singleton=true)
public interface RuntimeMgr extends AMXProxy, Utility, Singleton
{
    @ManagedOperation
    public void stopDomain();
    
    /** Map key is the name of the descriptor, value is the content of the descriptor */
    @ManagedOperation
    public Map<String,String> getDeploymentConfigurations(String appName);
    
    /**
        Stop the domain (command to pass to {@link #executeREST}.
        Note that this method might or might not throw an exception, depending on
        how quickly the server quits. Wrap the call in a try/catch always.
     */
    public static final String STOP_DOMAIN = "stop-domain";
    
    /**
        Execute a REST command.  Do not include a leading "/".
     */
    @ManagedOperation
    public String executeREST(final String command);
    
    /**
        Return the base URL for use with {@link #executeREST}.  Example:
        http://localhost:4848/__asadmin/
        
        Example only, the host and port are typically different.  A trailing "/" is 
        included; simply append the command string and call {@link #executeREST}.
     */
    @ManagedAttribute
    public String getRESTBaseURL();
    
    
    /** Key into Map returned by various methods including {@link #getConnectionDefinitionPropertiesAndDefaults} */
    public static final String PROPERTY_MAP_KEY = "PropertyMapKey";
    /** Key into Map returned by various methods including {@link #getConnectionDefinitionPropertiesAndDefaults} */
    public static final String REASON_FAILED_KEY = "ReasonFailedKey";
    
    /**
        Get properties of JDBC Data Source
        @see #PROPERTY_MAP_KEY
        @see #REASON_FAILED_KEY
     */
    public Map<String,Object>  getConnectionDefinitionPropertiesAndDefaults( final String datasourceClassName );
}










