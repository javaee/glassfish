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

package com.sun.enterprise.addons;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.appserv.addons.AddonFatalException;
import com.sun.appserv.addons.ConfigurationContext;

/**
 * This class maintains the registry, it is a warpper over Properties class.
 * @author binod@dev.java.net
 */
public class AddonInstanceRegistry extends AddonRegistry{

    
    protected AddonInstanceRegistry(File domainRoot, Logger logger) 
    throws AddonFatalException {
        super(domainRoot, logger);
    }
    
    /**
     * Retrieve the current status of the addon.
     * If the addon does not exist in the registry, then 
     * it should be configured.
     * If the addon status is changed to enabled/disabled
     * that is figured out by comparing with the system copy. 
     * of the registry.
     * If the addon is marked for unconfiguring, the addon 
     * status will be "unconfigure".
     * If the status is same in user copy and system copy
     * the status will be "unchanged"
     */
    protected status getStatus(String name){

        String systemConfFlag = 
        String.class.cast(systemRegistry.get(name+INSTANCEKEY+CONFIGUREKEY));

        if (systemConfFlag != null && systemConfFlag.equals("true")) {
            return status.CONFIGURE;
        }

        if (systemConfFlag != null && systemConfFlag.equals("false")) {
            return status.UNCONFIGURE;
        }

        return status.UNCHANGED;
    }


    /**
     * Change the status. It will make the status the same in 
     * user copy of the registry and system copy.
     */
    protected void setStatus(String name, status stat) {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, 
            "[No Op]Setting status of " + name + " as" + stat);
        }
    }
    
}
