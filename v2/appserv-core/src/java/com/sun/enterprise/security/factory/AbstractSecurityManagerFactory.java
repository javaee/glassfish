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
 * AbstractSecurityManagerFactory.java
 *
 * Created on June 9, 2003, 1:44 PM
 */

package com.sun.enterprise.security.factory;

import java.util.Map;
import java.util.HashMap;

import com.sun.enterprise.SecurityManager;
import com.sun.enterprise.security.factory.SecurityManagerFactory;
import com.sun.enterprise.deployment.Descriptor;
import java.util.logging.*; 
import com.sun.logging.LogDomains;
/**
 * This class is the parent for Web/Ejb SecurityManager. It keeps a pool of SM
 * objects.
 * @author  Harpreet Singh
 */
public abstract class AbstractSecurityManagerFactory 
    implements SecurityManagerFactory {
    
    protected static final Logger _logger = 
	Logger.getLogger(LogDomains.SECURITY_LOGGER);
  
    protected Map _securityManagerPool = new HashMap();
    
    public abstract SecurityManager getSecurityManager(String contextId);
    
    public abstract SecurityManager createSecurityManager(Descriptor descriptor);

    /**
     * Does the SM pool has this SM already
     * @param String the context Id of the SecurityManager
     * @return true, if SM present, false otherwise
     */
    protected boolean _poolHas(String contextId){
       return _securityManagerPool.containsKey(contextId);
    }
    
    protected void _poolPut(String contextId, SecurityManager smf){
        synchronized(_securityManagerPool){
            _securityManagerPool.put(contextId, smf);   
        }
    }
    protected SecurityManager _poolGet(String contextId){
        return (SecurityManager)_securityManagerPool.get(contextId);
    }
    
   public void removeSecurityManager(String contextId){
        synchronized (_securityManagerPool){
            _securityManagerPool.remove(contextId);
        }
    }   
}
