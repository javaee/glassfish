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

/*
 * $Id: Application.java,v 1.5 2005/12/25 04:27:36 tcfujii Exp $
 */

package com.sun.enterprise.ddgenerator.sun;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.interfaces.*;

/**
 * Class description
 *
 * @author Sreenivas Munnangi
 */

public class Application implements DDGenerator {

    private com.sun.enterprise.deployment.Application application = null;
    private String applicationDirectory = null;

    public Application() {
    }

    public Application(com.sun.enterprise.deployment.Application application,
		String applicationDirectory) {
	this.application = application;
	this.applicationDirectory = applicationDirectory;
    }

    public void setApplicationDirectory(String applicationDirectory) {
	this.applicationDirectory = applicationDirectory;
    }

    public void setDescriptor(com.sun.enterprise.deployment.Descriptor descriptor) {
	this.application = (com.sun.enterprise.deployment.Application) descriptor;
    }

    public void generate() {

	if (application == null) return;

	java.util.Set ejbBundleDescriptors = application.getEjbBundleDescriptors();

	// Determine if sun-ejb.xml needs to be generated

	boolean generate = false;

	for(Iterator it=ejbBundleDescriptors.iterator(); it.hasNext(); ) {
		EjbBundleDescriptor ejbBundleDescriptor = (EjbBundleDescriptor) it.next();
		com.sun.enterprise.ddgenerator.sun.EjbModule ejbModule =
			new com.sun.enterprise.ddgenerator.sun.EjbModule(ejbBundleDescriptor, 
				applicationDirectory);
		if (! ejbModule.hasSunDescriptor()) {
			generate = true;
		}
	}

	if (generate == false) return;

	// Backup existing sun descriptor

	for(Iterator it=ejbBundleDescriptors.iterator(); it.hasNext(); ) {
		EjbBundleDescriptor ejbBundleDescriptor = (EjbBundleDescriptor) it.next();
		com.sun.enterprise.ddgenerator.sun.EjbModule ejbModule =
			new com.sun.enterprise.ddgenerator.sun.EjbModule(ejbBundleDescriptor, 
				applicationDirectory);
		ejbModule.backupSunDescriptor();
	}

	// Generate sun descriptor

	for(Iterator it=ejbBundleDescriptors.iterator(); it.hasNext(); ) {
		EjbBundleDescriptor ejbBundleDescriptor = (EjbBundleDescriptor) it.next();
		com.sun.enterprise.ddgenerator.sun.EjbModule ejbModule =
			new com.sun.enterprise.ddgenerator.sun.EjbModule(ejbBundleDescriptor, 
				applicationDirectory);
		ejbModule.generate();
	}

	// update referencing descriptors

	java.util.Vector ejbRefDescriptorsV = application.getEjbReferenceDescriptors();

	if (ejbRefDescriptorsV == null) {
		save();
		return;
	}

	int ejbRefDescriptorsSize = ejbRefDescriptorsV.size();

	for (int i=0; i<ejbRefDescriptorsSize; i++) {
		EjbReferenceDescriptor ejbRefDescriptor = 
			(EjbReferenceDescriptor) ejbRefDescriptorsV.get(i);
		updateEjbRefDescriptorJndiName(ejbRefDescriptor, ejbBundleDescriptors);
	}

	// save application descriptor

	save();

    }

    private void updateEjbRefDescriptorJndiName(EjbReferenceDescriptor ejbRefDescriptor, 
		java.util.Set ejbBundleDescriptors) {

	for(Iterator it=ejbBundleDescriptors.iterator(); it.hasNext(); ) {

		EjbBundleDescriptor ejbBundleDescriptor = (EjbBundleDescriptor) it.next();
		
		java.util.Set ejbDescriptors = ejbBundleDescriptor.getEjbs();

		for (Iterator jt = ejbDescriptors.iterator(); jt.hasNext();) {

			EjbDescriptor ejbDescriptor = (EjbDescriptor) jt.next();
			if ((ejbDescriptor.getHomeClassName()).equals(ejbRefDescriptor.getHomeClassName())) {
				ejbRefDescriptor.setJndiName(ejbDescriptor.getJndiName());
				return;
			}
		}
	}

    }

    private void save() {

	FileArchive in = new FileArchive();

	try {
		in.open(applicationDirectory);
	} catch (java.io.IOException ioe) {
	}

	DescriptorArchivist archivist = new DescriptorArchivist();

	try {
		archivist.write(application, in);
	} catch (java.io.IOException ioe) {
	}

    }

    public static void main (String args[]) {

	String appDir = "/home/sreeni/TEMP/stateless-converter_1";

        System.out.println("Application");

        System.out.println("Press enter to continue ...");
	try {
		System.in.read();
	} catch (java.io.IOException ioe) {
	}

	FileArchive in = new FileArchive();

	try {
		in.open(appDir);
	} catch (java.io.IOException ioe) {
	}


	ApplicationArchivist archivist = new ApplicationArchivist();
	archivist.setXMLValidation(false);
	archivist.setClassLoader(null);
	com.sun.enterprise.deployment.Application application = null;
	try {
		application = (com.sun.enterprise.deployment.Application) archivist.open(in);
	} catch (java.io.IOException ioe) {
	} catch (org.xml.sax.SAXParseException saxpe) {
	}

	com.sun.enterprise.ddgenerator.sun.Application app = 
		new com.sun.enterprise.ddgenerator.sun.Application(application, appDir); 

	app.generate();
    }

}
