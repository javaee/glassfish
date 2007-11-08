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



import com.sun.enterprise.diagnostics.collect.Collector;
import com.sun.enterprise.diagnostics.collect.DomainXMLHelper;
import com.sun.enterprise.diagnostics.report.html.HTMLReportWriter;
import com.sun.logging.LogDomains;

import java.util.zip.ZipFile;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * DiagnosticAgent provides the main entry point to Diagnostic Service Back End.
 * 
 * @author mu125243
 */
public class PEDiagnosticAgent implements DiagnosticAgent {
    
    private static Logger logger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    
    /** Creates a new instance of DiagnosticAgent */
    public PEDiagnosticAgent() {
    }
    
    

    /**
     * Generates Diagnostic Report
     * @param clioptions cli options specified by the user
     * @return absolute path of zip containing diagnostic report
     * @throw DiagnosticException 
     */
    public String generateReport(Map options) throws DiagnosticException {
        if(options != null) {
            BackendObjectFactory objectFactory = getBackendObjectFactory(options);
            ReportGenerator generator = objectFactory.createReportGenerator();
            return generator.generate().getName();
        }
        throw new DiagnosticException(" Null Input for report generation");
    }
    
    /**
     * Generates Diagnostic Report
     * @param clioptions cli options specified by the user
     * @instances list of instance names for which report generation is invoked
     * @targetType type of the target for which report is being generated
     * @return absolute path of zip containing diagnostic report
     * @throw DiagnosticException 
     */
    public String generateReport(Map clioptions, List<String> instances,
            String targetType) throws DiagnosticException {
        throw new DiagnosticException("Unsupported operation");
    }
            

    /**
     * @param repositoryDir absolute path of central repository
     * @return list of attributes being masked with ****
     * @throw DiagnosticException 
     */
    public List<String> getConfidentialProperties(String repositoryDir) 
    throws DiagnosticException {
       return new DomainXMLHelper(repositoryDir).getAttrs();
    }
    
    /**
     * Delete report specified by the fileName
     * @fileName absolute path of the report to be deleted
     * @throw DiagnosticException
     */
    public void deleteReport(String fileName) throws DiagnosticException {
        if (fileName != null) {
            File file = new File(fileName);
            if(file.exists()) {
                file.delete();
            }
        }
    }
    /**
     * Returns BavkendObjectFactory
     */
    protected BackendObjectFactory getBackendObjectFactory(Map input) {
        return new PEBackendObjectFactory(input);
    }
    
    private Logger getLogger() {
        return logger;
    }
}
