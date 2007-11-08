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
package com.sun.enterprise.diagnostics.collect;

import com.sun.enterprise.diagnostics.*;
import com.sun.enterprise.diagnostics.util.DiagnosticServiceHelper;
import com.sun.logging.LogDomains;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Responsible for configuring various collectors based on diagnostic service
 * configuration and colleting diagnostic information for the entire report.
 *
 * @author Manisha Umbarje
 */
public abstract class Harvester implements Collector {

    protected ReportConfig config;
    protected ReportTarget target;
    private List<Collector> collectors;
    private ChecksumCollector checksumCollector;
   
    private SystemInfoCollector systemInfoCollector ;
    private FilesCollector installationLogCollector;
    protected static final Logger logger =
            LogDomains.getLogger(LogDomains.ADMIN_LOGGER);

    /**
     * Creates instance of Harvester
     * @param config combined representation of CLIOptions and ReportTarget
     */
    public Harvester(ReportConfig config) {
        if (config != null) {
            this.config = config;
            this.target = config.getTarget();
        }
    }

    /**
     * Invoke various collectors to capture diagnostic data
     */
    public Data capture() throws DiagnosticException {
        WritableDataImpl dataImpl = new WritableDataImpl();
        Iterator<Collector> list = collectors.iterator();
        while(list.hasNext()) {
            Data dataObj = capture(list.next());
            if(dataObj != null)
                dataImpl.addChild(dataObj);
        }
        if (!dataImpl.getChildren().hasNext()) {
            //no data has been collected by any of the collectors.
            DiagnosticException e = new DiagnosticException("No Diagnostic data has been collected");
            logger.log(Level.WARNING, "diagnostic-service.no_data_collected");
            throw e;
        }
        return dataImpl;
    }

    public void addCollector(Collector obj) {
        if(obj != null)
            collectors.add(obj);
    }

    public abstract void addRemoteCollectors() throws DiagnosticException ;

    /**
     * Initialize collectors
     */
    public void initialize() throws DiagnosticException {
        collectors = new ArrayList(8);
        addCustomerInputCollector();
        //addInstallationCheckSumCollector();
        initializeInstanceCollectors(config.getInstanceConfigurations());
        addRemoteCollectors();
        //addSystemInfoCollector();
    }

    /**
     * Initialize instance collectors
     */
    protected void initializeInstanceCollectors(
            Iterator<ServiceConfig> iterator) {
        while(iterator.hasNext()) {
            ServiceConfig serviceConfig = iterator.next();
            String repositoryDir = serviceConfig.getRepositoryDir() ;
            String reportDir = target.getIntermediateReportDir();

            if(!(serviceConfig.getInstanceName().equals(Constants.SERVER)))
                reportDir = reportDir+ File.separator +
                    serviceConfig.getInstanceName();

            if(serviceConfig.isCaptureChecksumEnabled()) {
                addInstallationCheckSumCollector();
                ChecksumCollector collector = new ChecksumCollector(
                        repositoryDir,
                        reportDir);
                addCollector(collector);
            }

            if(serviceConfig.isVerifyDomainEnabled()) {
                DomainXMLVerificationCollector domainXMLCollector
                         = new DomainXMLVerificationCollector(repositoryDir,
                         reportDir);
                 addCollector(domainXMLCollector);
            }

            addCollector(new ConfigCollector(repositoryDir, reportDir));
            //If minLogLevel != OFF, add collector
            if(!(serviceConfig.getMinLogLevel() == Level.OFF.intValue())) {
                Collector collectorObj = new LogCollector(
                        config.getCLIOptions().getStartDate(),
                        config.getCLIOptions().getEndDate(),
                        reportDir, serviceConfig );
                if (collectorObj != null)
                    addCollector(collectorObj);
            }

            if(serviceConfig.isCaptureAppDDEnabled()) {
                addCollector(new AppInfoCollector(repositoryDir, reportDir));
            }

            addFilesCollector(serviceConfig.isCaptureInstallLogEnabled(),
                    DataType.INSTALLATION_LOG);
            addSystemInfoCollector(serviceConfig.isCaptureSystemInfoEnabled());
        }
    }

    protected void addFilesCollector(boolean captureInstallationLog, String dataType) {
        if(captureInstallationLog) {
            if(installationLogCollector == null) {
                String installationRoot = DiagnosticServiceHelper.getInstallationRoot();
                List<String> files = new ArrayList(2);
                String[] logFiles =
                        new File(installationRoot).list(new FilenameFilter() {
                   public boolean accept(File folder, String name)  {
                       if( (name.contains(Constants.INSTALLATION_LOG_PREFIX)) ||
                               (name.contains(Constants.SJSAS_INSTALLATION_LOG_PREFIX)))
                           return true;
                       return false;
                   }
                });
                files.addAll(Arrays.asList(logFiles));

                installationLogCollector = new FilesCollector(installationRoot,
                    target.getIntermediateReportDir(),files,  dataType);
                addCollector(installationLogCollector);
            }
        }
    }

    protected void addCustomerInputCollector() {
        if (config.getCLIOptions().getCustomerInput() != null ||
                config.getCLIOptions().getCustomerInputFile() != null) {
            addCollector(new CustomerInputCollector(
                    config.getCLIOptions().getCustomerInputFile(),
                    config.getCLIOptions().getCustomerInput(),
                    target.getIntermediateReportDir(),
                    config.getExecutionContext().isLocal()));
        }
    }

    protected void addInstallationCheckSumCollector() {
        if(checksumCollector == null) {
            checksumCollector = new ChecksumCollector(
                            DiagnosticServiceHelper.getInstallationRoot(),
                            target.getIntermediateReportDir());
            addCollector(checksumCollector);
        }
    }

    protected void addMonitoringInfoCollectors(String path, List<String> instances) {
        String reportDir = target.getIntermediateReportDir();
        Iterator<String> iterator = instances.iterator();

        while (iterator.hasNext()) {
            String instanceName = iterator.next();
            addMonitoringInfoCollector(reportDir, path,instanceName);
        }
    }

    protected void addMonitoringInfoCollector(String reportDir,
            String path, String instanceName) {
        if(path == null)
            path = "";
        reportDir= reportDir + File.separator + path + File.separator +
                instanceName;
        addCollector(new MonitoringInfoCollector(path, instanceName, reportDir));
    }

    protected void addSystemInfoCollector(boolean captureSystemInfoEnabled){
        if(captureSystemInfoEnabled) {
            String reportDir = config.getTarget().getIntermediateReportDir();
            if(systemInfoCollector == null) {
                systemInfoCollector = new SystemInfoCollector(reportDir);
                //Add the collector only once.
                addCollector(systemInfoCollector);
            }
        }
    }

    /**
     * Invoke collector to capture information
     * @param obj collector
     */
    private Data capture(Collector obj) {
        try {
             return obj.capture();
        } catch (Throwable de) {
            logger.log(Level.WARNING,
                    "diagnostic-service.error_collecting_data" ,
                    new Object[]{de.getMessage()});
        }
        return null;
    }
}
