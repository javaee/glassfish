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

import com.sun.enterprise.ee.synchronization.cleaner.CleanerUtils;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.SynchronizationResponse;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Reporter class for synchronization audit. 
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class AuditReport {

    /**
     * Initializes the variables.
     *
     * @param   res  synchronization response
     * @param   missing  list of missing files in cache
     * @param   extra  list of extra files in cache
     */
    AuditReport(SynchronizationResponse res, List missing, List extra) {

        _response    = res;
        _missingList = missing;
        _extraList   = extra;
    }

    /**
     * Returns the audit status. 
     *
     * @return   true when audit fails
     */
    boolean isAuditFailed() {
        return _status;
    }

    void report() {
        StringBuffer buffer = new StringBuffer();
        String hdr = _localStrMgr.getString("reportHdr");
        buffer.append("\n" + hdr + "\n");

        // report missing files
        String missingFiles = _localStrMgr.getString("missingFiles");
        buffer.append(missingFiles + "\n");
        int missingCnt = 0;
        boolean addMissingDots = false;
        int mSize = _missingList.size();
        for (int i=0; i<mSize; i++) {
            String f = (String) _missingList.get(i);
            if (!CleanerUtils.isExcluded(f)) {
                if (missingCnt <= MAX_REPORT_CNT) {
                    buffer.append(f + "\n");
                } else {
                    addMissingDots = true;
                }
                missingCnt++;
            }
        }
        if (addMissingDots) {
            buffer.append("...\n");
        }
        String tMissingFiles = _localStrMgr.getString("totalMissingFiles");
        buffer.append("\t" + tMissingFiles + missingCnt + "\n");

        // report extra files
        String extraFiles = _localStrMgr.getString("extraFiles");
        buffer.append(extraFiles + "\n");
        int extraCnt = 0;
        boolean addExtraDots = false;
        int eSize = _extraList.size();
        for (int i=0; i<eSize; i++) {
            String f = (String) _extraList.get(i);
            if (!CleanerUtils.isExcluded(f)) {
                if (extraCnt <= MAX_REPORT_CNT) {
                    buffer.append(f + "\n");
                } else {
                    addExtraDots = true;
                }
                extraCnt++;
            }
        }
        if (addExtraDots) {
            buffer.append("...\n");
        }
        String tExtraFiles = _localStrMgr.getString("totalExtraFiles");
        buffer.append("\t" + tExtraFiles + extraCnt + "\n");

        List central = _response.getFileList();
        if (missingCnt > 0 || extraCnt > 0 || _logger.isLoggable(Level.FINE) ) {

            // add central repository content if log level is FINE
            if (_logger.isLoggable(Level.FINE)) {

                String centralFiles = _localStrMgr.getString("centralFiles");
                buffer.append(centralFiles + "\n");

                int cSize = central.size();
                for (int i=0; i<cSize; i++) {
                    String cFile = (String) central.get(i);
                    if (i < MAX_REPORT_CNT) {
                        buffer.append(cFile + "\n");
                    }
                }
                if (cSize > MAX_REPORT_CNT) {
                    buffer.append("...\n");
                }
                String tCentralFiles =
                    _localStrMgr.getString("totalCentralFiles");
                buffer.append("\t" + tCentralFiles + cSize + "\n");
            }
            
            // log audit report
            _logger.info( buffer.toString() );
        } else {

            // log audit report summary
            _logger.log(Level.INFO, 
                "synchronization.audit_report_success", 
                String.valueOf(central.size()));
        }

        // set the audit status
        if (missingCnt > 0 || extraCnt > 0) {
            _status = true;
        }
    }

    // ---- VARIABLE(S) - PRIVATE -------------------------------------
    private SynchronizationResponse _response = null;
    private List _missingList                 = null;
    private List _extraList                   = null;
    private boolean _status                   = false;

    private static final int MAX_REPORT_CNT   = 500;
    private static Logger _logger = Logger.getLogger(EELogDomains.
                SYNCHRONIZATION_LOGGER);
    private static final StringManager _localStrMgr = 
            StringManager.getManager(AuditReport.class);
}
