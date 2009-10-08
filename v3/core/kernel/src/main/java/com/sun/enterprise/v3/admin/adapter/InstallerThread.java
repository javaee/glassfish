/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.v3.admin.adapter;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipFile;
import com.sun.enterprise.v3.server.ApplicationLoaderService;
import com.sun.pkg.client.Image;


import java.beans.PropertyVetoException;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;


/**
 * @author kedar
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
final class InstallerThread extends Thread {

    private final File ipsRoot;
    private final File warFile;
    private final String proxyHost;
    private final int proxyPort;
    private final Domain domain;
    private final ServerEnvironmentImpl env;
    private final String contextRoot;
    private final AdminConsoleAdapter adapter;
    private final Habitat habitat;
    private final Logger log;
    private final List<String> vss;


    /**
     * Constructor.
     */
    InstallerThread(File ipsRoot, File warFile, String proxyHost, int proxyPort, AdminConsoleAdapter adapter, Habitat habitat, Domain domain, ServerEnvironmentImpl env, String contextRoot, Logger log, List<String> vss) {

        this.ipsRoot = ipsRoot;
        this.warFile = warFile;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.adapter = adapter;
        this.habitat = habitat;
        this.domain = domain;
        this.env = env;
        this.contextRoot = contextRoot;
        this.vss = vss;  //defensive copying is not required here
        this.log = log;
    }

    /**
     *
     */
    @Override
    public void run() {
        try {
            // The following are the basic steps which are required to get the
            // Admin Console web application running.  Each step ensures that
            // it has not already been completed and adjusts the state message
            // accordingly.
            download();
            expand();
            install();
            load();
            cleanup();

            // From within this Thread mark the installation process complete
            adapter.setInstalling(false);
        } catch (Exception ex) {
            adapter.setInstalling(false);
            adapter.setStateMsg(AdapterState.APPLICATION_NOT_INSTALLED);
            log.log(Level.INFO, "Problem while attempting to install admin console!", ex);
        }
    }

    /**
     * <p> This uses the Update Center to download the Admin Console web
     * application.</p>
     */
    private void download() throws Exception {
	if (AdminConsoleAdapter.isDirectoryDeploy()) {
	    return;
	}
        File warFile = getWarFile();
        if (warFile.exists()) {
            // Already downloaded
            return;
        }

        // Not downloaded get it from IPS
        adapter.setStateMsg(AdapterState.DOWNLOADING);
        Proxy proxy = null;  //Proxy.NO_PROXY;
        if (proxyHost != null && !"".equals(proxyHost)) {
            SocketAddress address = new InetSocketAddress(proxyHost, proxyPort);
            proxy = new Proxy(Proxy.Type.HTTP, address);
        }

        // Download and install files from Update Center
        try {
            Image image = new Image(ipsRoot);
            String pkgs[] = {adapter.getIPSPackageName()};
            if (proxy != null) {
                image.setProxy(proxy);
            }
            image.installPackages(pkgs);
            //Verify that admingui.war exists, it should by this point.
            if (getWarFile().exists()) {
                log.log(Level.SEVERE, "Error in downloading Admin Console from UpdateCenter");
                // FIXME: should we thrown an exception here ?
            }
            adapter.setDownloadedVersion();
            adapter.setStateMsg(AdapterState.DOWNLOADED);
        } catch (Exception ex) {
// FIXME: Handle properly
            ex.printStackTrace();
        }
    }

    /**
     *
     */
    private void expand() throws Exception {
	if (AdminConsoleAdapter.isDirectoryDeploy()) {
	    return;
	}
        File warFile = getWarFile();
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Expanding the archive: "
                    + warFile.getAbsolutePath());
        }
        File expFolder = new File(warFile.getParentFile(), AdminConsoleAdapter.ADMIN_APP_NAME);
        if (expFolder.exists() && new File(expFolder, "WEB-INF").exists()) {
            // Already completed
            return;
        }

        // Set the adapter state
        adapter.setStateMsg(AdapterState.EXPANDING);

        // Not yet expanded, expand it...
        expFolder.mkdirs();
        ZipFile zip = new ZipFile(warFile, expFolder);
        List list = zip.explode(); //pre Java 5 code

        // Set the adapter state
        adapter.setStateMsg(AdapterState.EXPANDED);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Expanded the archive with :"
                    + list.size()
                    + " entries, at: "
                    + warFile.getParentFile().getAbsolutePath());
        }
    }

    /**
     *
     */
    private File getWarFile() {
        return warFile;
    }

    /**
     * <p> Install the admingui.war file.</p>
     */
    private void install() throws Exception {
        if (domain.getSystemApplicationReferencedFrom(env.getInstanceName(), AdminConsoleAdapter.ADMIN_APP_NAME) != null) {
            // Application is already installed
            adapter.setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
            // no need to change domain.xml application config, except to update the deployed version.
            adapter.updateDeployedVersion();
            return;
        }

        // Set the adapter state
        adapter.setStateMsg(AdapterState.INSTALLING);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Installing the Admin Console Application...");
        }

        //create the application entry in domain.xml
        ConfigCode code = new ConfigCode() {
            public Object run(ConfigBeanProxy... proxies) throws PropertyVetoException, TransactionFailure {
                SystemApplications sa = (SystemApplications) proxies[0];
                Application app = sa.createChild(Application.class);
                sa.getModules().add(app);
                app.setName(AdminConsoleAdapter.ADMIN_APP_NAME);
                app.setEnabled(Boolean.TRUE.toString());
                app.setObjectType("system-admin"); //TODO
                app.setDirectoryDeployed("true");
                app.setContextRoot(contextRoot);
                File warFile = getWarFile();
                try {
                    app.setLocation("${com.sun.aas.installRootURI}/lib/install/applications/" + AdminConsoleAdapter.ADMIN_APP_NAME);
                } catch (Exception me) {
                    // can't do anything
                    throw new RuntimeException(me);
                }
                Module singleModule = app.createChild(Module.class);
                app.getModule().add(singleModule);
                singleModule.setName(app.getName());
                Engine webe = singleModule.createChild(Engine.class);
                webe.setSniffer("web");
                Engine sece = singleModule.createChild(Engine.class);
                sece.setSniffer("security");
                singleModule.getEngines().add(webe);
                singleModule.getEngines().add(sece);
                Server s = (Server) proxies[1];
                List<ApplicationRef> arefs = s.getApplicationRef();
                ApplicationRef aref = s.createChild(ApplicationRef.class);
                aref.setRef(app.getName());
                aref.setEnabled(Boolean.TRUE.toString());
                aref.setVirtualServers(getVirtualServerList()); //TODO
                arefs.add(aref);
                return true;
            }
        };
        adapter.updateDeployedVersion();
        Server server = domain.getServerNamed(env.getInstanceName());
        ConfigSupport.apply(code, domain.getSystemApplications(), server);

        // Set the adapter state
        adapter.setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Admin Console Application Installed.");
        }
    }

    /**
     *
     */
    private String getVirtualServerList() {
        if (vss == null)
            return "";
        String s = Arrays.toString(vss.toArray(new String[0]));
        //standard JDK implemetation always returns this enclosed in [], remove them
        s = s.substring(1, s.length() - 1);
        return (s);
    }

    /**
     * <p> Load the Admin Console web application.</p>
     */
    private void load() {
        // hook for Jerome
        Application config = adapter.getConfig();
        if (config == null) {
            throw new IllegalStateException("Admin Console application has no system app entry!");
        }
        // Set adapter state
        adapter.setStateMsg(AdapterState.APPLICATION_LOADING);

        // Load the Admin Console Application
        String sn = env.getInstanceName();
// FIXME: An exception may not be thrown... check for errors!
        ApplicationRef ref = domain.getApplicationRefInServer(sn, AdminConsoleAdapter.ADMIN_APP_NAME);
        habitat.getComponent(ApplicationLoaderService.class).processApplication(config, ref, log);

        // Set adapter state
        adapter.setStateMsg(AdapterState.APPLICATION_LOADED);
    }

    /*
    * <p> Clean up the backup copy
    */
    private void cleanup() {
        File backup = new File(warFile.getParentFile(), AdminConsoleAdapter.ADMIN_APP_NAME + ".backup");
        if (backup.exists()) {
            adapter.setStateMsg(AdapterState.APPLICATION_BACKUP_CLEANING);
            FileUtils.whack(backup);
            adapter.setStateMsg(AdapterState.APPLICATION_BACKUP_CLEANED);
        }
    }

}
