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

package com.sun.enterprise.ee.diagnostics;

import com.sun.enterprise.diagnostics.ExecutionContext;
import com.sun.logging.ee.EELogDomains;
import java.util.logging.Logger;

/**
 *
 * @author mu125243
 */
public class EEExecutionContext extends ExecutionContext {
   
    private static Logger NODEAGENT_LOGGER = 
            Logger.getLogger(EELogDomains.NODE_AGENT_LOGGER,
                "com.sun.logging.ee.enterprise.system.nodeagent.LogStrings");
    public static final EEExecutionContext DAS_EC = 
            new EEExecutionContext(false, "das", ADMIN_LOGGER);
    //Any data collection in nodeagent is equivalent to local execution context
    public static final EEExecutionContext NODEAGENT_EC = 
            new EEExecutionContext(true,  "nodeagent" , NODEAGENT_LOGGER);
    public static final EEExecutionContext LOCAL_EC = 
            new EEExecutionContext(true, "", ADMIN_LOGGER);
    
    private String context;
    
    
    
    /** Creates a new instance of ExecutionContext */
    public EEExecutionContext(boolean local, String context, Logger logger) {
        //remote mode
        super(local, logger);
        this.context = context;
    }
    
    /**
     * Context in which report generation is being processed.
     * Context is not usful in PE environment
     */
    public String getContext() {
        return context;
    }
    
    public String toString() {
        return getContext() + "," + super.toString();
    }
    
    public boolean equals(EEExecutionContext ec) {
        if(ec != null) {
            if((ec.isLocal() == local) && (ec.getContext().equals(context)))
                return true;
            return false;
        }
        return false;
    }
}
