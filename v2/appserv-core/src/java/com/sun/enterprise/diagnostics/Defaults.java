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
package com.sun.enterprise.diagnostics;

import java.util.logging.Level;
import java.io.File;

/**
 * Holds all defaults for service config
 * @author mu125243
 */
public interface Defaults {
    
    public static final int MAX_NO_OF_ENTRIES = 500;
    public static final int MIN_LOG_LEVEL = Level.INFO.intValue();
    public static final String LOG_FILE = "${com.sun.aas.instanceRoot}" +
            File.separator + "logs" + File.separator + 
            "server.log" + File.separator;
    public static final String LOGS = "logs";
    public static final String DEST_LOG_FILE = File.separator + LOGS +
            File.separator + "server.log";
    public static final String TEMP_REPORT = "reports_temp";
    public static final String TEMP_REPORT_FOLDER = File.separator + 
            TEMP_REPORT;
    public static final String DIAGNOSTIC_REPORT  = "diagnostic-reports";
    public static final String REPORT_FOLDER = File.separator +  
            DIAGNOSTIC_REPORT;
    public static final String CHECKSUM_FILE_NAME= File.separator + "checksum";
    public static final String DOMAIN_XML_VERIFICATION_OUTPUT= File.separator +
            "domain-xml-verification";
    public static final String CUSTOMER_INPUT = File.separator + "customer-input";
    public static final String MONITORING_INFO_FILE = "MonitoringInformation";
    public static final String SYSTEM_INFO_FILE = File.separator +
            "SystemInformation";
    public static final String HADB_INFO_FILE = File.separator +
            "HADBInformation";
    public static final String REPORT_NAME = File.separator + "ReportSummary.html";
}
