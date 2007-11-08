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
package com.sun.enterprise.tools.deployment.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.util.NotificationListener;
import com.sun.enterprise.util.NotificationEvent;
//import com.sun.enterprise.tools.deployment.ui.utils.*;

/** Objects of this type manage applications within a tool. 
* @author Danny Coward
*/

public class ApplicationManager 
    implements NotificationListener 
{

    /** Notification type for change of active application.*/
    public static String ACTIVE_CHANGED = "active changed"; // NOI18N

    /** Notification type for an application change.*/
    public static String APPLICATION_CHANGED = "application changed"; // NOI18N

    /** Notification type that the list of apps changed in this manager.*/
    public static String APPLICATION_LIST_CHANGED = "application list changed"; // NOI18N

    /** Notification type that a listener was added.*/
    public static String LISTENER_ADDED = "listener added"; // NOI18N

    /** Notification type for addition of an application.*/
    public static String APPLICATION_ADDED = "application added"; // NOI18N

    /** Notification type that an application was removed.*/
    public static String APPLICATION_REMOVED = "application removed"; // NOI18N

    /** Notification property to store the application that changed.*/
    public static String APPLICATION_PROPERTY = "application property"; // NOI18N

//  private static Hashtable projects = new Hashtable();

    private Application activeApplication;
    private Vector applications = new Vector();
    private Vector applicationListeners = new Vector();

    private File preferencesDirectory;
    private File temp;
    
    /** Construct a new application manager storing its preferences in the 
    *** given directory.
    **/
    public ApplicationManager(File preferencesDirectory, File temp) {
	this.preferencesDirectory = preferencesDirectory;
	this.temp = temp;
    }
    
    /** Gets the temporary directory. */
    public File getTemp() {
	return temp;
    }
    
    /** retore state from the last session, returning a Hashtable of
    ** filenames to exceptions for each application that could
    * not be reopened.
    */
    public Hashtable restoreFromUserHome() throws IOException {
	Hashtable badApplicationJarFilenamesToExceptions = new Hashtable();
	File appsFile = new File(preferencesDirectory, getConfigAppFileName());
	if (appsFile.exists()) {
	    FileInputStream fis = new FileInputStream(appsFile);
	    Properties applications = new Properties();
	    applications.load(fis);  
	    for (Enumeration e = applications.propertyNames(); 
		e.hasMoreElements();) {
		String appName = (String) e.nextElement();
		String appJarFilename = (String) 
		    applications.getProperty(appName);
		Application openedApp=null;
		try {
		    this.openApplication(new File(appJarFilename)); 
		} catch (Throwable t) {
		    badApplicationJarFilenamesToExceptions.put(
			appJarFilename, t);
		} 
	    }
            if (fis != null) {
                fis.close();
            }
	}
	return badApplicationJarFilenamesToExceptions;
    }
    
    /** Saves the list of open applications to the working dir under 
    *** '.jpedeploytool/.' */
    public void saveToUserHome() throws IOException {
	File appsFile = new File(preferencesDirectory, getConfigAppFileName());	
	FileOutputStream fos = new FileOutputStream(appsFile);
	Properties applications = new Properties();
	for (Enumeration e = this.getApplications().elements(); 
	    e.hasMoreElements();) {
	    Application nextApplication = (Application) e.nextElement();
	    applications.put(nextApplication.getName(), 
		nextApplication.getApplicationArchivist().
		    getApplicationFile().toString());
	}
	applications.store(fos, "J2EE Applications"); // NOI18N
        if (fos != null) {
            fos.close();
        }
    }
    
    /** Register for state changes. */
    public void addNotificationListener(NotificationListener nl) {
	applicationListeners.addElement(nl);
	this.notify(LISTENER_ADDED, null, null);
    }
    
    /** Deregister for state changes. */
    public void removeNotificationListener(NotificationListener nl) {
	applicationListeners.removeElement(nl);
    }
    
    /** Sets the active application. */
    public void setActiveApplication(Application application) {
	if (application != activeApplication) {
	    this.activeApplication = application;
	    if (application != null) {
		this.notify(ACTIVE_CHANGED, APPLICATION_PROPERTY, application);
	    } else {
		this.notify(ACTIVE_CHANGED, null, null);
	    }
	}
    }
    
    /** Return the active application. */
    public Application getActiveApplication() {
	return activeApplication;
    }
    
    /** The Vector open application objects. */
    public Vector getApplications() {
	Vector clone = (Vector) applications.clone();
	return clone;
    }
    
    public Application getApplicationWithJar(File jarFilename) {
	for (Enumeration e = this.getApplications().elements(); 
	    e.hasMoreElements();) {
	    Application application = (Application) e.nextElement();
	    if (application.getApplicationArchivist().
		getApplicationFile().getAbsolutePath().equals(
		    jarFilename.getAbsolutePath())) {
		return application;
	    }
	}
	return null;
    }
    
    /* The names of the open applications. */
    public Vector getApplicationNames() {
	Vector names = new Vector();
	for (Enumeration e = applications.elements();e.hasMoreElements();) {
	    names.addElement(((Application) e.nextElement()).getName());   
	}
	return names;
    }
    
    protected String getUniqueApplicationName(String trialName) {
	return Application.createUniqueNameAmongst(trialName, 
	    this.getApplicationNames());
    }
    
    /** Create a new application with the given name and jar file and 
    ** makes it the active one.. */
    public Application newApplication(String name, String jarFile) {
	Application newApplication = new Application(
	    this.getUniqueApplicationName(name), new File(jarFile));

//	Project newProj = new ProjectImpl(newApplication);
//	Project.addProject(newApplication, newProj);

	try {
	    this.saveApplication(newApplication);
	} catch (IOException ioe) {
	    Log.print(this, ioe);
	}
	this.addApplication(newApplication);
	return newApplication;
    }
    
    /** Save the given applicastion to its JAR file. */
    public void saveApplication(Application application) 
	throws IOException {
	ApplicationArchivist archivist = application.getApplicationArchivist();
	archivist.save(archivist.getApplicationFile(), true);

//	ProjectImpl proj = (ProjectImpl)Project.getProject(application);
//	if (proj != null) {
//	    proj.save();
//	}
    }

    /** Save the given application to the supllied file.
    */
    public void saveAsApplication(Application application, File newJar) 
	throws IOException {
	ApplicationArchivist archivist = application.getApplicationArchivist();
	archivist.save(newJar, true);
//	ProjectImpl proj = (ProjectImpl)Project.getProject(application);
//	if (proj != null) {
//	    proj.save();
//	}
    }

    /** Open the JAR file application. */
    public Application openApplication(File name) throws Exception {
	Application openedApplication = ApplicationArchivist.openAT(name);   //4691307; 4774785
        // fix bug# 4766725
        for (java.util.Iterator itr = openedApplication.getWebBundleDescriptors().iterator(); itr.hasNext();) {
                WebBundleDescriptor webBundleDescriptor = (WebBundleDescriptor) itr.next();
                for(Enumeration e = webBundleDescriptor.getWebComponentDescriptors(); e.hasMoreElements();){
                        WebComponentDescriptorImpl comp = (WebComponentDescriptorImpl) e.nextElement();
                        if (comp.getName().equals(""))
                            comp.setName(comp.getCanonicalName());
                }
        }
        
        // start IASRI4691307
        // Initialize Sun Specific Descriptors HERE
        //
        com.sun.enterprise.tools.deployment.ui.sunone.SunOneUtils.createSunOneXml(openedApplication);
        com.sun.enterprise.tools.deployment.Util.addMappingsSkeleton(openedApplication);
        // end IASRI4691307
	this.addApplication(openedApplication);
//	ProjectImpl newProj = new ProjectImpl(openedApplication);
//	newProj.load();
//	Project.addProject(openedApplication, newProj);
	openedApplication.doneOpening();
	return openedApplication;
    }
    
    /** Close the given application. */
    public void closeApplication(Application application) {
	applications.removeElement(application);

//	UIProject.removeProject(application);
//	ProjectImpl proj = (ProjectImpl)Project.getProject(application);
//	if ( proj != null ) {
//	    proj.remove();
//	    Project.removeProject(application);
//	}

	this.setActiveApplication(null);
	this.notify(APPLICATION_REMOVED, APPLICATION_PROPERTY, application);
    }
    
    /** Adds an application to the manager.*/
    public void addApplication(Application application) {
	String newName = this.getUniqueApplicationName(application.getName());
	application.setName(newName);
	applications.addElement(application);
	application.addNotificationListener(this);
	this.notify(APPLICATION_ADDED, APPLICATION_PROPERTY, application);
	this.setActiveApplication(application);	
    }

    /* -------------------------------------------------------------------------
    */

    private static String CFG_APP_FILE = "applications"; // NOI18N
    private String configFileName = CFG_APP_FILE;
    protected String getConfigAppFileName() {
	return this.configFileName;
    }
    public void setConfigAppFileName(String cfgAppFile) {
	this.configFileName = (cfgAppFile != null)? cfgAppFile : CFG_APP_FILE;
    }

    /* -------------------------------------------------------------------------
    ** NotificationListener interface
    */

    /** I am recieving a notification event.*/
    public void notification(NotificationEvent ne) {
	this.notify(ne.getType(), NotificationEvent.OBJECT_THAT_CHANGED, 
	    ne.getObjectThatChanged());
    }

    /** Convenience method for notifying listeners.*/
    public void notify(String type, String name, Object value) {
	NotificationEvent ne = null;
	if (name == null) {
	    ne = new NotificationEvent(this, type);
	} else {
	    ne = new NotificationEvent(this, type, name, value);
	}    
	Vector listenersClone = null;
	synchronized (applicationListeners) {
	    listenersClone = (Vector) applicationListeners.clone();
	}
	for (Enumeration e = listenersClone.elements(); e.hasMoreElements();) {
	    NotificationListener nl = (NotificationListener) e.nextElement();
	    //System.out.println("notifying " + nl);
	    nl.notification(ne);   
	    //System.out.println("Done");
	}
	//System.out.println("Notify: " + (System.currentTimeMillis() - time));
    }

    /* -------------------------------------------------------------------------
    */

    /** Formatted String. */
    public String toString() {
	return "Application Manager"; // NOI18N
    }

}

