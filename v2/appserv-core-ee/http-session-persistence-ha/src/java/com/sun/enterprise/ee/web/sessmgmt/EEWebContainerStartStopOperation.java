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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * EEWebContainerStartStopOperation.java
 *
 * Created on February 17, 2004, 4:07 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;

import com.sun.enterprise.web.ConnectionShutdownUtil;
import com.sun.enterprise.web.EmbeddedWebContainer;
import com.sun.enterprise.web.WebContainerStartStopOperation;
import com.sun.enterprise.ee.web.initialization.ServerConfigReader;

/**
 *
 * @author  lwhite
 */
public class EEWebContainerStartStopOperation implements WebContainerStartStopOperation {
    
    /**
     * The embedded Catalina object.
     */
    protected EmbeddedWebContainer _embedded = null; 
    
    /** Creates a new instance of EEWebContainerStartStopOperation */
    public EEWebContainerStartStopOperation() {
    }
    
    public void init(EmbeddedWebContainer embeddedWebContainer) {
        _embedded = embeddedWebContainer;
    }

    /**
     * return a list of the classes implementing ShutdownCleanupCapable
     */    
    public ArrayList doPreStop() {
        return getShutdownCleanupCapablesList();
    }    

    /**
     * clear the JDBC connection pool
     * and close the connections from the shutdownCleanupCapablesList
     */     
    public void doPostStop(ArrayList shutdownCleanupCapablesList) {
        if(shutdownCleanupCapablesList != null) {
            doShutdownCleanup(shutdownCleanupCapablesList);
        }
        /*
        ConnectionShutdownUtil util = 
            new ConnectionShutdownUtil(_embedded);
        util.clearoutJDBCPool();
         */
    }    
    
    /**
     * get a list of the classes implementing ShutdownCleanupCapable
     */
    private ArrayList getShutdownCleanupCapablesList() { 
         System.out.println("SHUTDOWN-about to gather shutdownCleanupCapables");
         ConnectionShutdownUtil shutdownUtil = new ConnectionShutdownUtil(_embedded);
         ArrayList shutdownCleanupCapablesList = new ArrayList();
         try {
            shutdownCleanupCapablesList = shutdownUtil.runGetShutdownCapables();
         } catch (Exception ex) {};
         return shutdownCleanupCapablesList;
    }

    private void doShutdownCleanup(ArrayList shutdownCleanupCapablesList) {
         ConnectionShutdownUtil shutdownUtil = new ConnectionShutdownUtil(_embedded);        
        //close all the connections in our connection pool 
        shutdownUtil.clearoutJDBCPool();
        
        //finally get shutdownCleanupCapables to close other connections
        //must skip if HADB is not installed
        if(ServerConfigReader.isHADBInstalled()) {
            if(EEHADBHealthChecker.isOkToProceed() ) {
                System.out.println("SHUTDOWN-about to close connections");        
                shutdownUtil.runShutdownCleanupFromShutdownCleanupCapableList(shutdownCleanupCapablesList);
            } else {
                System.out.println("skipping SHUTDOWN to close connections: HADB non-operational");
            }
        }
    }    
    
}
