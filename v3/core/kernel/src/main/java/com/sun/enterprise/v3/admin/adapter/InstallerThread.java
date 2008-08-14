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
import com.sun.pkg.client.Image;

import java.beans.PropertyVetoException;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.List;

import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;


/**
 *
 * @author kedar
 * @author Ken Paulsen
 */
final class InstallerThread extends Thread {

    private final List<URL> urls;
    private final File toFile;
    private final String proxyHost;
    private final int proxyPort;
    private final ProgressObject progress;
    private final Domain domain;
    private final ServerEnvironmentImpl env;
    private final String contextRoot;


    /**
     *	Constructor.
     */
    InstallerThread(List<URL> urls, File toFile, String proxyHost, int proxyPort, ProgressObject progress, Domain domain, ServerEnvironmentImpl env, String contextRoot) {

        this.urls        = urls;
        this.toFile      = toFile;
        //this.toFile.getParentFile().mkdirs();
        this.proxyHost   = proxyHost;
        this.proxyPort   = proxyPort;
        this.progress    = progress;
        this.domain      = domain;
        this.env         = env;
        this.contextRoot = contextRoot;
    }
    
    /**
     *
     */
    @Override
    public void run() {
        try {
	    File warFile = getWarFile();
	    if (!warFile.exists()) {
		// Not downloaded get it from IPS
		download();
	    }
            expand();
            install();
            synchronized (progress) {
                progress.finish();
                progress.setAdapterState(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
            }
        } catch(Exception e) {
            synchronized (progress) {
                progress.finish();
                progress.setMessage(e.getMessage());
                progress.setAdapterState(AdapterState.APPLICATION_NOT_INSTALLED);
            }
        }
    }

    /**
     *
     */
    private void syncMessage(String s) {
        synchronized(progress) {
            progress.setMessage(s);
        }
    }

    /**
     *	<p> This now uses the Update Center for the download.</p>
     */
    private void download() throws Exception {
// FIXME: Use proxy information for UC
	Proxy proxy = Proxy.NO_PROXY;
	if (proxyHost != null && !"".equals(proxyHost)) {
	    SocketAddress address = new InetSocketAddress(proxyHost, proxyPort);
	    proxy = new Proxy(Proxy.Type.HTTP, address);
	}

	// Download and install files from Update Center
	try {
System.out.println("toFile: " + toFile);
	    Image img = new Image(toFile);
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
	    // FIXME: Adjust this if needed.
	    //img.setAuthority("glassfish.org",  "http://eflat.sfbay.sun.com:10000",  "glassfish.org");
System.out.println("\nAfter installation ---------");
	} catch(Exception ex) {
	    ex.printStackTrace();
System.out.println("!!!!!!!  cannot create Image");
	}

	/*
	// Open URL Connection to remote server
	URLConnection uc = null;
	URL theUrl = null;
	for (URL url : urls) {
	    uc = url.openConnection(proxy);
	    if (uc instanceof HttpURLConnection) {
		HttpURLConnection http = (HttpURLConnection)uc;
		if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    theUrl = url;
		    break;
		}
	    }
	}
	*/
	/*
	// Read data from remote server
	syncMessage("Starting download from: " + theUrl); //synchronized
	BufferedInputStream bis  = new BufferedInputStream (uc.getInputStream());
	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream (toFile));
	try {
	    byte[] bytes = new byte[8192];
	    int read;
	    while ((read = bis.read(bytes)) != -1) {
		bos.write(bytes, 0, read);
	    }
	} finally {
	    if (bis != null) {
		try {
		    bis.close();
		} catch(IOException io) {}
	    }            
	    if (bos != null) {
		try {
		    bos.close();
		} catch(IOException io) {}
	    }
	}
	syncMessage("Finished downloading: " + uc.getContentLength() + " bytes from: " + theUrl);
	*/
    }

    private void expand() throws Exception {
	File warFile = getWarFile();
        syncMessage("Expanding the archive: " + warFile.getAbsolutePath());
        File expFolder = new File(warFile.getParentFile(), AdminConsoleAdapter.ADMIN_APP_NAME);
        expFolder.mkdirs();
        ZipFile zip = new ZipFile(warFile, expFolder);
        List list = zip.explode(); //pre Java 5 code
        syncMessage("Expanded the archive with :" + list.size() + " entries, at: " + warFile.getParentFile().getAbsolutePath());
    }

    /**
     *
     */
    private File getWarFile() {
	String warFileName = AdminConsoleAdapter.ADMIN_APP_WAR;
	return new File(toFile, "glassfish/lib/install/applications/" + warFileName);
    }
    
    /**
     *	<p> Install the admingui.war file.</p>
     */
    private void install() throws Exception {
        syncMessage("Installing the application ...");
        //create the application entry in domain.xml
        ConfigCode code = new ConfigCode () {
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
                //aref.setVirtualServers("__asadmin"); //TODO
                arefs.add(aref);
                return ( true );
            }
        };
        Server server = domain.getServerNamed(env.getInstanceName());
        ConfigSupport.apply(code, domain.getSystemApplications(), server);
        syncMessage("Installed the application ...");
    }
}
