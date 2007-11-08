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
import com.sun.enterprise.diagnostics.collect.Harvester;
import com.sun.enterprise.diagnostics.report.html.HTMLReportWriter;
import com.sun.logging.LogDomains;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 *
 * @author mu125243
 */
public abstract class SingleInstanceObjectFactory implements BackendObjectFactory {
    
    protected Map input;
    protected CLIOptions options;
    protected ReportConfig config;
    protected ReportTarget target;
    protected ExecutionContext context;
    protected Logger logger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    /** Creates a new instance of BackendObjectFactory */
    public SingleInstanceObjectFactory(Map input) {
        this.input = input;
    }
 
   
    public ReportGenerator createReportGenerator() throws DiagnosticException {
        analyzeInput();
        Collector harvester = createHarvester();
        HTMLReportWriter reportWriter = createHTMLReportWriter();
        return createReportGenerator(config,harvester,reportWriter);
    }

    public abstract ReportGenerator createReportGenerator(ReportConfig config,
            Collector harvester,HTMLReportWriter reportWriter)
            throws DiagnosticException;

    public abstract Collector createHarvester() throws DiagnosticException;
    
    public abstract HTMLReportWriter createHTMLReportWriter() 
    throws DiagnosticException ;
    
    public abstract TargetResolver createTargetResolver(boolean local)
    throws DiagnosticException;
    
    protected ReportConfig getReportConfig() {
        return config;
    }
    
    protected  void analyzeInput()  throws DiagnosticException{
        if(input != null) {
            TargetResolver resolver = null;
            boolean local;
            List<ServiceConfig> serviceConfigs = null;
            
            options = new CLIOptions(input);
            local = isLocal();
            resolver = createTargetResolver(local);
            target = resolver.resolve();
            context = resolver.getExecutionContext();
            serviceConfigs = resolver.getServiceConfigs();
            logger = context.getLogger();
            config = new ReportConfig(options, target,context);
            config.addInstanceConfigs(serviceConfigs);
        }
    }
    
    protected boolean isLocal() {
        if(options.isLocal() && (options.getTargetDir() != null) )
            return true;
        return false;
    }
}
