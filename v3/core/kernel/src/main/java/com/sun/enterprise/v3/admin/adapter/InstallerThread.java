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

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemApplications;
import com.sun.enterprise.util.zip.ZipFile;
import com.sun.enterprise.v3.server.ApplicationLoaderService;
import com.sun.pkg.client.Image;

import java.beans.PropertyVetoException;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
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
 *
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
     *	Constructor.
     */
    InstallerThread(File ipsRoot, File warFile, String proxyHost, int proxyPort, AdminConsoleAdapter adapter, Habitat habitat, Domain domain, ServerEnvironmentImpl env, String contextRoot, Logger log, List<String>vss) {

        this.ipsRoot	= ipsRoot;
        this.warFile	= warFile;
        this.proxyHost	= proxyHost;
        this.proxyPort	= proxyPort;
        this.adapter	= adapter;
        this.habitat	= habitat;
        this.domain	= domain;
        this.env	= env;
        this.contextRoot= contextRoot;
        this.vss         = vss;  //defensive copying is not required here
	this.log	= log;
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

	    // From within this Thread mark the installation process complete
	    adapter.setInstalling(false);
        } catch (Exception ex) {
	    adapter.setInstalling(false);
	    adapter.setStateMsg(AdapterState.APPLICATION_NOT_INSTALLED);
	    log.log(Level.INFO, "Problem while attempting to install admin console!", ex);
        }
    }

    /**
     *	<p> This uses the Update Center to download the Admin Console web
     *	    application.</p>
     */
    private void download() throws Exception {
	File warFile = getWarFile();
	if (warFile.exists()) {
	    // Already downloaded
	    return;
	}

	// Not downloaded get it from IPS
// FIXME: set state to DOWNLOADING: adapter.setStateMsg(AdapterState.DOWNLOADING);
// FIXME: Use proxy information for UC
	Proxy proxy = Proxy.NO_PROXY;
	if (proxyHost != null && !"".equals(proxyHost)) {
	    SocketAddress address = new InetSocketAddress(proxyHost, proxyPort);
	    proxy = new Proxy(Proxy.Type.HTTP, address);
	}

	// Download and install files from Update Center
	try {
	    Image img = new Image(ipsRoot);
System.out.println("image.getRootDirectory() = " + img.getRootDirectory());
	    /*
	    img.refreshCatalogs();
	    Catalog catalog = img.getCatalog();
	    catalog.refresh();

	    System.out.println("!!!!!!!!!!! ========================  getInventory");
	    List<Image.FmriState> list2 = img.getInventory(null, false);
	    for (Image.FmriState fs : list2){
		Fmri fmri = fs.fmri;
		System.out.println("NAME = " + fmri.getName() + ";  VERSION = " + fmri.getVersion());
	    }
	    */
	    
	    String pkgs[] = { "glassfish-gui" };
	    img.installPackages(pkgs);

// FIXME: Verify that getWarFile() exists, it should by this point.
// FIXME: set state to DOWNLOADED: adapter.setStateMsg(AdapterState.DOWNLOADED);
	    // FIXME: Adjust this if needed.
	    //img.setAuthority("glassfish.org",  "http://eflat.sfbay.sun.com:10000",  "glassfish.org");
System.out.println("\nAfter installation ---------");
	} catch(Exception ex) {
	    ex.printStackTrace();
System.out.println("!!!!!!!  cannot create Image");
	}
    }

    /**
     *
     */
    private void expand() throws Exception {
// FIXME: adapter.setStateMsg(AdapterState.EXPANDING_WAR_FILE);  <-- add this
	File warFile = getWarFile();
	if (log.isLoggable(Level.FINE)) {
	    log.log(Level.FINE, "Expanding the archive: "
		    + warFile.getAbsolutePath());
	}
        File expFolder = new File(warFile.getParentFile(), AdminConsoleAdapter.ADMIN_APP_NAME);
	if (expFolder.exists()) {
	    // Already completed
	    return;
	}

	// Not yet expanded, expand it...
        expFolder.mkdirs();
        ZipFile zip = new ZipFile(warFile, expFolder);
        List list = zip.explode(); //pre Java 5 code
// FIXME: adapter.setStateMsg(AdapterState.WAR_FILE_EXANDED);  <-- add WAR_FILE_EXPLODED
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
     *	<p> Install the admingui.war file.</p>
     */
    private void install() throws Exception {
	if (domain.getSystemApplicationReferencedFrom(env.getInstanceName(), AdminConsoleAdapter.ADMIN_APP_NAME) != null) {
	    // Application is already installed
	    adapter.setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
	    return;
	}

// FIXME: Set state msg
	//adapter.setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
	if (log.isLoggable(Level.FINE)) {
	    log.log(Level.FINE, "Installing the Admin Console Application...");
	}
        //create the application entry in domain.xml
        ConfigCode code = new ConfigCode() {
            public Object run(ConfigBeanProxy ... proxies) throws PropertyVetoException, TransactionFailure {
                SystemApplications sa = (SystemApplications) proxies[0];
                Application app = ConfigSupport.createChildOf(sa, Application.class);
                sa.getModules().add(app);
                app.setName(AdminConsoleAdapter.ADMIN_APP_NAME);
                app.setEnabled(Boolean.TRUE.toString());
                app.setObjectType("system-admin"); //TODO
                app.setDirectoryDeployed("true");
                app.setContextRoot(contextRoot);
		File warFile = getWarFile();
                File expFolder = new File(warFile.getParentFile(), AdminConsoleAdapter.ADMIN_APP_NAME);
                try {
                    app.setLocation(expFolder.toURI().toString());
                } catch(Exception me) {
		    // can't do anything
		    throw new RuntimeException(me);
		}
                Engine webe = ConfigSupport.createChildOf(app, Engine.class);
                webe.setSniffer("web");
                Engine sece = ConfigSupport.createChildOf(app, Engine.class);
                sece.setSniffer("security");
                app.getEngine().add(webe);
                app.getEngine().add(sece);
                Server s = (Server)proxies[1];
                List<ApplicationRef> arefs = s.getApplicationRef();
                ApplicationRef aref = ConfigSupport.createChildOf(s, ApplicationRef.class);
                aref.setRef(app.getName());
                aref.setEnabled(Boolean.TRUE.toString());
                aref.setVirtualServers(getVirtualServerList()); //TODO
                arefs.add(aref);
                return true;
            }
        };
        Server server = domain.getServerNamed(env.getInstanceName());
        ConfigSupport.apply(code, domain.getSystemApplications(), server);

	// Set the state msg to APPLICATION_INSTALLED_BUT_NOT_LOADED
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
        s = s.substring(1, s.length()-1);
        return ( s );
    }
    /**
     *	<p> Load the Admin Console web application.</p>
     */
    private void load() {
	// hook for Jerome
	Application config = adapter.getConfig();
	if (config == null) {
	    throw new IllegalStateException("handleInstalledButNotLoadedState called with no system app entry");
	}
	String sn = env.getInstanceName();
	ApplicationRef ref = domain.getApplicationRefInServer(sn, AdminConsoleAdapter.ADMIN_APP_NAME);
	habitat.getComponent(ApplicationLoaderService.class).processApplication(config, ref, log);
	adapter.setStateMsg(AdapterState.APPLICATION_LOADED);
    }
}
