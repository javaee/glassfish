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

import java.util.List;
import java.util.zip.ZipFile;
import java.io.File;

import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.ReportGenerator;
import com.sun.enterprise.diagnostics.ReportConfig;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.ReportTarget;
import com.sun.enterprise.diagnostics.util.FileUtils;
import com.sun.enterprise.diagnostics.collect.Collector;
import com.sun.enterprise.ee.diagnostics.collect.DomainHarvester;
import com.sun.enterprise.diagnostics.report.html.HTMLReportWriter;

/**
 * Collects data, generates HTML report and archives it if mode is local
 * @author mu125243
 */
public class EEReportGenerator extends ReportGenerator {

    
    /** Creates a new instance of ReportGenerator */
    public EEReportGenerator(ReportConfig config, Collector harvester, 
            HTMLReportWriter writer) {
        super(config, harvester, writer);
     }
    
    protected void writeReportSummary(Data data)
    throws DiagnosticException {
         if(config.getExecutionContext().equals(EEExecutionContext.DAS_EC)) {
            String dirTobeArchived = config.getTarget().getArchiveDir();
            String extractToDir = dirTobeArchived;


            if(config.getTarget().getType().equals(EETargetType.DOMAIN) ||
                config.getTarget().getType().equals(EETargetType.DAS) ||    
                config.getTarget().getType().equals(EETargetType.CLUSTER) ) {
                extractToDir = extractToDir +  File.separator + config.getTarget().getName();
                FileUtils.extractJarFiles(dirTobeArchived, extractToDir);
                super.writeReportSummary(data);
            } else { // For a nodeagent and a instance, only extract the contents
                FileUtils.extractJarFiles(dirTobeArchived, extractToDir);
            }
        }else{
            super.writeReportSummary(data);
        }
    }
 }
