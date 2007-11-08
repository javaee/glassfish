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
package com.sun.enterprise.ee.synchronization.audit;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Responsible for auditing synchronization request.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class RequestAuditor {

    /**
     * Constructor.
     *
     * @param  response  synchronization response
     */
    RequestAuditor(SynchronizationResponse response) {
        _response = response;
    }

    /**
     * Compares the given response for DAS against local repository.
     */
    void audit() {
        SynchronizationRequest[] requests = _response.getReply();
        List local = getLocalInventory();
        List central = _response.getFileList();

        //_missingList = central;
        _missingList = InventoryMgr.getInventoryDiff(central, local);
        _extraList = InventoryMgr.getInventoryDiff(local, central);
    }

    List getMissingList() {
        return _missingList;
    }

    List getExtraList() {
        return _extraList;
    }

    private List getLocalInventory() {

        SynchronizationRequest[] requests = _response.getReply();
        List localList = new ArrayList();

        for (int i=0; i<requests.length; i++) {
            File f = requests[i].getFile();

            String tDir = requests[i].getTargetDirectory();
            if (f.isDirectory()) {
                String dir = requests[i].getBaseDirectory() 
                           + File.separator + tDir;

                _logger.fine("Inspecting Directory: " + dir);

                File root = new File(dir);

                if (root.isDirectory()) {
                    InventoryMgr mgr = new InventoryMgr(root);
                    //List l = mgr.getAuditInventory();
                    List l = mgr.getInventory();
                    if (l != null) {
                        int length = l.size();
                        for (int j=0; j<length; j++) {
                            localList.add( tDir+File.separator+l.get(j) );
                        }
                    }
                }
            } else if (f.isFile()) {
                localList.add( tDir+File.separator+f.getName() );
            }
        }

        return localList;
    }

    
    // ---- VARIABLE(S) - PRIVATE -------------------------------------
    private SynchronizationResponse _response = null;
    private List _missingList                 = null;
    private List _extraList                   = null;

    private static Logger _logger = Logger.getLogger(EELogDomains.
                SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
            StringManager.getManager(RequestAuditor.class);
}
