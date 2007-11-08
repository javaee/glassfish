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

import java.io.*;
import java.util.*;
import com.sun.enterprise.util.*;
import com.sun.enterprise.deployment.*;
//import com.sun.enterprise.tools.deployment.ui.UIUtils;
//import com.sun.enterprise.tools.deployment.ui.utils.*;

/* Manage stand-alone objects within the deploytool. 
** @author Martin D. Flynn
*/

public class StandAloneManager 
    implements NotificationListener 
{

    /* ------------------------------------------------------------------------ 
    ** Constants
    */

    public static final String STANDALONE_PROPERTY = "StandAloneProperty"; // NOI18N
    public static final String STANDALONE_ADDED    = "StandAloneAdded"; // NOI18N
    public static final String STANDALONE_REMOVED  = "StandAloneRemoved"; // NOI18N
    public static final String LISTENER_ADDED      = "ListenerAdded"; // NOI18N
    public static final String ACTIVE_CHANGED      = "ActiveChanged"; // NOI18N

    /* ------------------------------------------------------------------------ 
    */

    private Vector standalones = new Vector();
    private Vector standaloneListeners = new Vector();
    private Descriptor activeStandAlone = null;

    private File preferencesDirectory = null;
    private File temp = null;

    /* ------------------------------------------------------------------------ 
    ** constructors 
    */

    public StandAloneManager(File preferencesDirectory, File temp) 
    {
	this.preferencesDirectory = preferencesDirectory;
	this.temp = temp;
    }

    /* ------------------------------------------------------------------------ 
    */
    
    /** Gets the temporary directory. */
    public File getTemp() {
	return temp;
    }
            
    /* return Vector containing open stand-alone objects */
    public Vector getStandAlones() 
    {
	return (Vector)this.standalones.clone();
    }

    /* return names of the open stand-alone objects */
    public Vector getStandAloneNames() 
    {
	Vector names = new Vector();
	Enumeration e = this.standalones.elements();
	for (;e.hasMoreElements();) {
	    names.addElement(((Descriptor)e.nextElement()).getName());   
	}
	return names;
    }
        
    public Descriptor getStandAloneWithJar(File jarFilename) 
    {
	String absPath = jarFilename.getAbsolutePath();
	Enumeration e = this.standalones.elements();
	for (;e.hasMoreElements();) {
	    Descriptor d = (Descriptor)e.nextElement();
	    if (d instanceof ConnectorDescriptor) {
		String uri = ((ConnectorDescriptor)d).getArchivist().
		    getArchiveUri();
	        if ((new File(uri)).getAbsolutePath().equals(absPath)) {
		    return d;
	        }
	    } else
	    if (d instanceof Application) {
	        if (((Application)d).getApplicationArchivist().
		    getApplicationFile().getAbsolutePath().equals(absPath)) {
		    return d;
	        }
	    } else {
		// XXX - unknown descriptor
	    }
	}
	return null;
    }
    
    /** Return the active stand-alone object */
    public Descriptor getActiveStandAlone() 
    {
	return this.activeStandAlone;
    }

    /* uniquify stand-alone name */
    protected String getUniqueStandAloneName(String trialName) 
    {
	return Descriptor.createUniqueNameAmongst(trialName, 
	    this.getStandAloneNames());
    }

    /* ------------------------------------------------------------------------ 
    */

    /* retore state from the last session, 
    ** return Hashtable of filenames that could not be reopened */
    public Hashtable restoreFromUserHome() 
	throws IOException 
    {
	Hashtable badFiles = new Hashtable();
	File propsFile = new File(preferencesDirectory, getConfigFileName());
	if (propsFile.exists()) {
	    FileInputStream fis = new FileInputStream(propsFile);
	    Properties props = new Properties();
	    props.load(fis);  
	    for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
		String name = (String)e.nextElement();
		String jarFilename = (String)props.getProperty(name);
		try {
		    this.openStandAlone(new File(jarFilename)); 
		} catch (Throwable t) {
		    badFiles.put(jarFilename, t);
		} 
	    }
            fis.close();
	}
	return badFiles;
    }
    
    /** Saves the list of open stand-alone objects to the working dir */
    public void saveToUserHome() 
	throws IOException 
    {
	File propsFile = new File(preferencesDirectory, getConfigFileName());	
	FileOutputStream fos = new FileOutputStream(propsFile);
	Properties props = new Properties();
	Enumeration e = this.standalones.elements();
	for (;e.hasMoreElements();) {
	    Descriptor next = (Descriptor)e.nextElement();
	    String loc = null;
	    if (next instanceof ConnectorDescriptor) {
		loc = ((ConnectorDescriptor)next).getArchivist().
		    getArchiveUri();
	    } else
	    if (next instanceof Application) {
		loc = ((Application)next).getApplicationArchivist().
		    getApplicationFile().toString();
	    }
	    if (loc != null) {
	        props.put(next.getName(), loc);
	    } else {
		System.err.println("Unsupported Stand-Alone descriptor type:");
	 	System.err.println("  " + next.getClass().getName());
	    }
	}
	props.store(fos, "J2EE Stand-Alone Descriptors"); // NOI18N
        fos.close();
    }

    /* ------------------------------------------------------------------------ 
    */

    public Descriptor openStandAlone(File name) 
	throws Exception 
    {

	/* ConnectorDescriptor */
	if (ConnectorArchivist.isConnector(name)) {
            //IASRI 4691307  Anissa
            //Open the rar file with ValidateXML set to false, and include S1AS descriptor.
	    ConnectorDescriptor d = ConnectorArchivist.open(name, false, true);
            // IASRI 4691307 end.
            
	    this.addStandAlone(d);
            
//	    ProjectImpl newProj = new ProjectImpl(d);
//	    newProj.load();
//	    Project.addProject(d, newProj);
	    return d;
	}

	/* Application */
	if (ApplicationArchivist.isApplication(name)) {
	    Application d = ApplicationArchivist.openAT(name);  //bug# 4774785;  4691307
	    this.addStandAlone(d);
//	    ProjectImpl newProj = new ProjectImpl(d);
//	    newProj.load();
//	    Project.addProject(d, newProj);
	    d.doneOpening();
	    return d;
	}

	/* not found */
	return null;

    }
    
    /** Close the given stand-alone object */
    public void closeStandAlone(Descriptor desc) 
    {
	this.standalones.removeElement(desc);
//	UIProject.removeProject(desc);
//	ProjectImpl proj = (ProjectImpl)Project.getProject(desc);
//	if (proj != null) {
//	    proj.remove();
//	    Project.removeProject(desc);
//	}
	this.setActiveStandAlone(null);
	this.notify(STANDALONE_REMOVED, STANDALONE_PROPERTY, desc);
    }

    /* save specified stand-alone object */
    public void saveStandAlone(Descriptor desc)
	throws IOException
    {
	if (desc instanceof ConnectorDescriptor) {
	    ConnectorDescriptor cd = (ConnectorDescriptor)desc;
	    ConnectorArchivist ca = (ConnectorArchivist)cd.getArchivist();
	    ca.save(new File(ca.getArchiveUri()), true);
	}
    }

    /* save specified stand-alone object */
    public void saveStandAloneAs(Descriptor desc, File newFile)
	throws IOException
    {
	if (desc instanceof ConnectorDescriptor) {
	    ConnectorDescriptor cd = (ConnectorDescriptor)desc;
	    ConnectorArchivist ca = (ConnectorArchivist)cd.getArchivist();
	    ca.save(newFile, true);
	}
    }

    /* -------------------------------------------------------------------------
    */
    
    /* Adds a stand-alone object to the manager */
    public void addStandAlone(Descriptor desc) 
    {
	String oldName = desc.getName();
	String newName = this.getUniqueStandAloneName(oldName);
	if (!oldName.equals(newName)) {
	    desc.setName(newName);
	}
	this.standalones.addElement(desc);
	desc.addNotificationListener(this);
	this.notify(STANDALONE_ADDED, STANDALONE_PROPERTY, desc);
	this.setActiveStandAlone(desc);	
    }

    /* Sets the active stand-alone object */
    public void setActiveStandAlone(Descriptor desc) 
    {
	if (desc != this.activeStandAlone) {
	    this.activeStandAlone = desc;
	    if (desc != null) {
		this.notify(ACTIVE_CHANGED, STANDALONE_PROPERTY, desc);
	    } else {
		this.notify(ACTIVE_CHANGED, null, null);
	    }
	}
    }

    /* return true if specified descriptor belongs to the stand-alone set */
    public boolean isStandAloneDescriptor(Descriptor desc)
    {
	Enumeration e = this.standalones.elements();
	for (;e.hasMoreElements();) {
	    if (desc == e.nextElement()) {
		return true;
	    }   
	}
	return false;
    }

    /* return true if stand-alone object descriptor is dirty (been changed) */
    public boolean isDirty(Descriptor desc)
    {
	if (desc instanceof ConnectorDescriptor) {
	    return ((ConnectorDescriptor)desc).isDirty();
	} else
	if (desc instanceof Application) {
	    return ((Application)desc).isDirty();
	}
	return false;
    }

    /* ------------------------------------------------------------------------ 
    */
    
    /*rRegister for state changes */
    public void addNotificationListener(NotificationListener nl) 
    {
	standaloneListeners.addElement(nl);
	this.notify(LISTENER_ADDED, null, null);
    }
    
    /* deregister for state changes */
    public void removeNotificationListener(NotificationListener nl) 
    {
	standaloneListeners.removeElement(nl);
    }

    /* forward notification events */
    public void notification(NotificationEvent ne) 
    {
	this.notify(ne.getType(), NotificationEvent.OBJECT_THAT_CHANGED, 
	    ne.getObjectThatChanged());
    }

    /** Convenience method for notifying listeners.*/
    public void notify(String type, String name, Object value) 
    {
	NotificationEvent ne = (name == null)?
	    new NotificationEvent(this, type) :
	    new NotificationEvent(this, type, name, value);

	/* make a copy of the listener list */
	Vector listenersClone = null;
	synchronized (standaloneListeners) {
	    listenersClone = (Vector)standaloneListeners.clone();
	}

	/* notify listeners */
	for (Enumeration e = listenersClone.elements(); e.hasMoreElements();) {
	    NotificationListener nl = (NotificationListener)e.nextElement();
	    nl.notification(ne);   
	}

    }

    /* -------------------------------------------------------------------------
    */

    public static final String CFG_CONNECTOR_FILE = "standalone"; // NOI18N
    private String configFileName = CFG_CONNECTOR_FILE;
    protected String getConfigFileName() 
    {
	return this.configFileName;
    }

    /* -------------------------------------------------------------------------
    */

    /** Formatted String. */
    public String toString() {
	return "Stand-Alone Manager"; // NOI18N
    }

}


