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

import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;
import com.sun.enterprise.ee.synchronization.inventory.InventoryMgr;
import com.sun.enterprise.ee.synchronization.TimestampRemoveCommand;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Manager class for synchronization audit. 
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class AuditMgr {

    /**
     * Initializes the variables.
     *
     * @param    req  synchronization request
     */
    public AuditMgr(SynchronizationRequest[] requests) {

        _requests  = requests;
    }

    /**
     * Audits a server synchronization.
     * 
     * @param  dasProperties   DAS properties
     */
    public void auditServer(DASPropertyReader dasProperties) 
            throws AuditException {

        try {
            // if audit is not turned on, abort
            if (!isAuditON()) {
                return;
            }

            // get repository content list from DAS
            MBeanServerConnectionInfo connInfo = 
                new MBeanServerConnectionInfo(dasProperties);
            AuditGetCommand aCommand = new AuditGetCommand(_requests, connInfo);
            aCommand.execute();
            SynchronizationResponse response = 
                (SynchronizationResponse) aCommand.getResult();

            List auditList = response.getFileList();

            // audit response
            RequestAuditor ra = new RequestAuditor(response);
            ra.audit();

            // generate audit report
            AuditReport aReport = 
                new AuditReport(response,ra.getMissingList(),ra.getExtraList());
            aReport.report();

            // remove timestamps if audit fails
            if (aReport.isAuditFailed()) {
                removeTS();
            }

        } catch (Exception e) {
            String msg = _localStrMgr.getString("serverAuditError");
            throw new AuditException(msg, e);
        }
    }

    /**
     * Removes the timestamp files. This will fascilitate a clean 
     * synchronization in the next attempt.
     */
    private void removeTS() {
        for (int i=0; i<_requests.length; i++) {
            try {
                TimestampRemoveCommand trc = 
                    new TimestampRemoveCommand(_requests[i], null);
                trc.execute();
            } catch (Exception e) { }
        }
    }

    /**
     * Returns the synchronization request this is executing.
     * 
     * @return  synchronization request for this object
     */
    public SynchronizationRequest[] getRequests() {
        return _requests;
    }

    /**
     * Returns true if audit is turned on by the system property.
     * Set -Dcom.sun.appserv.synchronization.audit=true to trun 
     * synchronization audit on.
     *
     * @return   true if synchronization audit is on
     */
    private boolean isAuditON() {
        return Boolean.getBoolean(AUDIT_KEY);
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------------
    private static final String AUDIT_KEY =
                    "com.sun.appserv.synchronization.audit";
    private SynchronizationRequest[] _requests = null;

    private static Logger _logger = Logger.getLogger(EELogDomains.
                SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
            StringManager.getManager(AuditMgr.class);
}
