/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.*;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.ActionReport;
import org.glassfish.deployment.common.DeploymentProperties;

import java.util.*;
import java.util.jar.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URISyntaxException;
import java.net.URI;
import java.io.*;
import java.beans.PropertyVetoException;

/**
 * Very simple ModuleStartup that basically force an immediate shutdown.
 * When start() is invoked, the upgrade of the domain.xml has already been
 * performed.
 * 
 * @author Jerome Dochez
 */
@Service(name="upgrade")
public class UpgradeStartup implements ModuleStartup {

    @Inject
    CommandRunner runner;

    @Inject
    AppServerStartup appservStartup;

    @Inject
    Applications applications;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    CommandRunner commandRunner;

    // we need to refine, a better logger should be used.
    @Inject
    Logger logger;

    private final static String MODULE_TYPE = "moduleType";

    public void setStartupContext(StartupContext startupContext) {
        appservStartup.setStartupContext(startupContext);
    }

    // do nothing, just return, at the time the upgrade service has
    // run correctly.
    public void start() {
        // we need to disable all the applications before starting server 
        // so the applications will not get loaded before redeployment
        // store the list of previous enabled applications
        // so we can reset these applications back to enabled after 
        // redeployment
        List<ApplicationRef> enabledApps = new ArrayList<ApplicationRef>();
        List<String> enabledAppNames = new ArrayList<String>();
        for (ApplicationRef appRef : server.getApplicationRef()) {
            logger.log(Level.INFO, "app " + appRef.getRef() + " is " + appRef.getEnabled() + " resulting in " + Boolean.parseBoolean(appRef.getEnabled()));
            if (Boolean.parseBoolean(appRef.getEnabled())) {
                logger.log(Level.INFO, "Disabling application " + appRef.getRef());
                enabledApps.add(appRef);
                enabledAppNames.add(appRef.getRef());
            }
        }

        if (enabledApps.size()>0) {
            try  {
                ConfigSupport.apply(new ConfigCode() {
                    public Object run(ConfigBeanProxy... configBeanProxies) throws PropertyVetoException, TransactionFailure {
                        for (ConfigBeanProxy proxy : configBeanProxies) {
                            ApplicationRef appRef = (ApplicationRef) proxy;
                            appRef.setEnabled(Boolean.FALSE.toString());
                        }
                        return null;
                    }
                }, enabledApps.toArray(new ApplicationRef[enabledApps.size()]));
            } catch(TransactionFailure tf) {
                logger.log(Level.SEVERE, "Exception while disabling applications", tf);
                return;
            }
        }

        // start the application server
        appservStartup.start();

        // redeploy all existing applications in disable state
        for (Application app : applications.getApplications()) {
            // we don't need to redeploy lifecycle modules
            if (Boolean.valueOf(app.getDeployProperties().getProperty
                ("isLifecycle"))) {
                continue;
            }
            logger.log(Level.INFO, "Redeploy application " + app.getName() + " located at " + app.getLocation());    
            if (!redeployApp(app)) {
                return;
            };     
        }

        // re-enables all applications. 
        // we need to use the names in the enabledAppNames to find all 
        // the application refs that need to be re-enabled
        // as the previous application refs collected not longer exist
        // after redeployment
        if (enabledAppNames.size()>0) {
            for (ApplicationRef appRef : server.getApplicationRef()) {
                if (enabledAppNames.contains(appRef.getRef())) {
                    logger.log(Level.INFO, "Enabling application " + appRef.getRef());
                    try {
                        ConfigSupport.apply(new SingleConfigCode<ApplicationRef>() {
                            public Object run(ApplicationRef param) throws PropertyVetoException, TransactionFailure {
                                param.setEnabled(Boolean.TRUE.toString());
                                return null;
                            }
                        }, appRef);
                    } catch(TransactionFailure tf) {
                        logger.log(Level.SEVERE, "Exception while re-enabling application " + appRef.getRef(), tf);
                        return;
                    }
                }
            }
        }

        // stop-the server.
        Logger.getAnonymousLogger().info("Exiting after upgrade");
        try {
            Thread.sleep(3000);
            if (runner!=null) {
                runner.doCommand("stop-domain", new Properties(), new PlainTextActionReporter());
                return;
            }

        } catch (InterruptedException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Exception while attempting to shutdown after upgrade", e);
        }

    }

    public void stop() {
        appservStartup.stop();
    }

    private boolean redeployApp(Application app) {
        // we don't need to redeploy any v3 type application
        if (app.getModule().size() > 0 ) {
            logger.log(Level.INFO, "Skip redeploying v3 type application " + 
                app.getName());
            return true;
        }
        ApplicationRef ref = null;
        for (ApplicationRef appRef : server.getApplicationRef()) {
            if (appRef.getRef().equals(app.getName())) {
                ref = appRef;
                break;
            }
        }

        // populate the params and properties from application element first
        DeployCommandParameters deployParams = app.getDeployParameters(ref);

        // for archive deployment, let's repackage the archive and redeploy
        // that way
        // we cannot just directory redeploy the archive deployed apps in
        // v2->v3 upgrade as the repository layout was different in v2 
        // we should not have to repackage for any upgrade from v3 
        if (! Boolean.valueOf(app.getDirectoryDeployed())) {
            File repackagedFile = null;
            try {
                repackagedFile = repackageArchive(app);
                logger.log(Level.INFO, "Repackaged application " + app.getName()
                    + " at " + repackagedFile.getPath()); 
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Repackaging of application " + app.getName() + " failed: " + ioe.getMessage(), ioe);
                return false;
            }
            if (repackagedFile == null) {
                logger.log(Level.SEVERE, "Repackaging of application " + app.getName() + " failed.");
                return false;
            }
            deployParams.path = repackagedFile;
        }

        deployParams.properties = app.getDeployProperties();
        // remove the marker properties so they don't get carried over 
        // through redeployment
        deployParams.properties.remove(MODULE_TYPE);
        // add the compatibility property so the applications are 
        // upgraded/redeployed in a backward compatible way
        deployParams.properties.setProperty(
            DeploymentProperties.COMPATIBILITY, "v2");
      
        // now override the ones needed for the upgrade
        deployParams.force = true;
        deployParams.dropandcreatetables = false;
        deployParams.createtables = false;
        deployParams.enabled = false;

        ActionReport report = new PlainTextActionReporter();

        commandRunner.doCommand("deploy", deployParams, report, null, null);

        // should we delete the temp file after we are done
        // it seems it might be useful to keep it around for debugging purpose

        if (report.getActionExitCode().equals(ActionReport.ExitCode.FAILURE)) {
            logger.log(Level.SEVERE, "Redeployment of application " + app.getName() + " failed: " + report.getMessage() + " Please reploy " + app.getName() + " manually.", report.getFailureCause());
            return false;
        }
        return true;
    }

    private File repackageArchive(Application app) throws IOException {
        URI uri = null;
        try {
            uri = new URI(app.getLocation());
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        if (uri == null) {
            return null;
        }
        
        Properties appProperties = app.getDeployProperties();
        String moduleType = appProperties.getProperty(MODULE_TYPE);
        String suffix = getSuffixFromType(moduleType);
        if (suffix == null) {
            suffix = ".jar";
        }
        File repositoryDir = new File(uri);

        // get temporary file directory of the system and set targetDir to it
        File tmp = File.createTempFile("upgrade", null);
        String targetParentDir = tmp.getParent();
        tmp.delete();

        if (moduleType.equals(ServerTags.J2EE_APPLICATION)) {
            return repackageApplication(repositoryDir, targetParentDir, suffix);
        } else {
            return repackageStandaloneModule(repositoryDir, targetParentDir, suffix);
        }
    }

    private File repackageApplication(File appDir,
        String targetParentDir, String suffix) throws IOException {
        String appName = appDir.getName();

        ReadableArchive source = archiveFactory.openArchive(appDir);

        File tempEar = new File(targetParentDir, appName + suffix);

        if (tempEar.exists()) {
            tempEar.delete();
        }

        WritableArchive target = archiveFactory.createArchive(tempEar);

        Collection<String> directoryEntries = source.getDirectories();
        List<String> subModuleEntries = new ArrayList<String>();
        List<String> entriesToExclude = new ArrayList<String>();
 
        // first put all the sub module jars to the target archive
        for (String directoryEntry : directoryEntries) {
            if (directoryEntry.endsWith("_jar") || 
                directoryEntry.endsWith("_war") || 
                directoryEntry.endsWith("_rar")) {
                subModuleEntries.add(directoryEntry); 
                File moduleJar = processModule(new File(
                    appDir, directoryEntry), targetParentDir, null);
                OutputStream os = null;
                InputStream is = new BufferedInputStream(
                    new FileInputStream(moduleJar));
                try {
                    os = target.putNextEntry(moduleJar.getName());
                    FileUtils.copy(is, os, moduleJar.length());
                } finally {
                    if (os!=null) {
                        target.closeEntry();
                    }
                    is.close();
                }
            }
        }

        // now find all the entries we should exclude to copy to the target
        // basically all sub module entries should be excluded
        for (String subModuleEntry : subModuleEntries) {
            Enumeration<String> ee = source.entries(subModuleEntry);
            while (ee.hasMoreElements()) {
                String eeEntryName = ee.nextElement();
                entriesToExclude.add(eeEntryName);
            }
        }

        // now copy the rest of the entries
        Enumeration<String> e = source.entries();
        while (e.hasMoreElements()) {
            String entryName = e.nextElement();
            if (! entriesToExclude.contains(entryName)) {
                InputStream is = new BufferedInputStream(source.getEntry(entryName));
                OutputStream os = null;
                try {
                    os = target.putNextEntry(entryName);
                    FileUtils.copy(is, os, source.getEntrySize(entryName));
                } finally {
                    if (os!=null) {
                        target.closeEntry();
                    }
                    is.close();
                }
            }
        }

        source.close();
        target.close();
      
        return tempEar;
    }

    private File repackageStandaloneModule(File moduleDirName, 
        String targetParentDir, String suffix) throws IOException {
        return processModule(moduleDirName, targetParentDir, suffix);
    }

    // repackage a module and return it as a jar file
    private File processModule(File moduleDir, String targetParentDir, 
        String suffix) throws IOException {
 
        String moduleName = moduleDir.getName();

        // sub module in ear case 
        if (moduleName.endsWith("_jar") || moduleName.endsWith("_war") || moduleName.endsWith("_rar")) {
            suffix = "." +  moduleName.substring(moduleName.length() - 3);
            moduleName = moduleName.substring(0, moduleName.lastIndexOf('_'));
        }

        ReadableArchive source = archiveFactory.openArchive(moduleDir);

        File tempJar = new File(targetParentDir, moduleName + suffix);

        if (tempJar.exists()) {
            tempJar.delete();
        }

        WritableArchive target = archiveFactory.createArchive(tempJar);

        Enumeration<String> e = source.entries();
        while (e.hasMoreElements()) {
            String entryName = e.nextElement();
            InputStream is = new BufferedInputStream(source.getEntry(entryName));
            OutputStream os = null;
            try {
                os = target.putNextEntry(entryName);
                FileUtils.copy(is, os, source.getEntrySize(entryName));
            } finally {
                if (os!=null) {
                    target.closeEntry();
                }
                is.close();
            }
        }

        // last is manifest if existing.
        Manifest m = source.getManifest();
        if (m!=null) {
            OutputStream os  = target.putNextEntry(JarFile.MANIFEST_NAME);
            m.write(os);
            target.closeEntry();
        }

        source.close();
        target.close();

        return tempJar;
    }

    private String getSuffixFromType(String moduleType) {
        if (moduleType == null) {
            return null;
        }
        if (moduleType.equals(ServerTags.CONNECTOR_MODULE)) {
            return ".rar"; 
        }
        if (moduleType.equals(ServerTags.EJB_MODULE)) {
            return ".jar"; 
        }
        if (moduleType.equals(ServerTags.WEB_MODULE)) {
            return ".war"; 
        }
        if (moduleType.equals(ServerTags.APPCLIENT_MODULE)) {
            return ".jar"; 
        }
        if (moduleType.equals(ServerTags.J2EE_APPLICATION)) {
            return ".ear"; 
        }
        return null;
    }
}
