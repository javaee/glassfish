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
 * $Id: EjbModule.java,v 1.5 2005/12/25 04:27:36 tcfujii Exp $
 */

package com.sun.enterprise.ddgenerator.sun;

import java.util.*;
import java.io.File;

import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.util.io.FileUtils;

import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.interfaces.*;

/**
 * Class description
 *
 * @author Sreenivas Munnangi
 */

public class EjbModule implements DDGenerator {

    private EjbBundleDescriptor ejbBundleDescriptor = null;
    private String applicationDirectory = null;

    public EjbModule () {
    }

    public EjbModule (EjbBundleDescriptor ejbBundleDescriptor, String applicationDirectory) {
	this.ejbBundleDescriptor = ejbBundleDescriptor;
	this.applicationDirectory = applicationDirectory;
    }

    public void setApplicationDirectory(String applicationDirectory) {
	this.applicationDirectory = applicationDirectory;
    }

    public void setDescriptor(com.sun.enterprise.deployment.Descriptor descriptor) {
	this.ejbBundleDescriptor = (com.sun.enterprise.deployment.EjbBundleDescriptor) descriptor;
    }

    public void generate() {
	if (ejbBundleDescriptor == null) return;

	java.util.Set ejbDescriptors = ejbBundleDescriptor.getEjbs();

	for (Iterator it = ejbDescriptors.iterator(); it.hasNext();) {
		EjbDescriptor ejbDescriptor = (EjbDescriptor) it.next();
		String jndiName = ejbDescriptor.getJndiName();
		if ((jndiName == null) || (jndiName.length() <1)) {
			String homeName = ejbDescriptor.getHomeClassName();
			ejbDescriptor.setJndiName(homeName);
		}
	}

    }

    public boolean hasSunDescriptor() {
	
	File file = getSunDescriptorFile();

	if (file == null) return false;

	if (file.exists()) {
		return true;
	} 

	return false;
    }

    public void backupSunDescriptor() {

	File file = getSunDescriptorFile();

	if (file == null) return;

	if (file.exists()) {
	    try {
		FileUtils.copy(file, new File(file.getAbsolutePath() + ".bak"));
	    } catch (java.io.IOException ioe) {
	    }
	}

    }


    private File getSunDescriptorFile () {
	
	File file = null;

	String archiveUri = ejbBundleDescriptor.getModuleDescriptor().getArchiveUri();

	String friendlyFileName = FileUtils.makeFriendlyFilename(archiveUri);

	String sunXmlFileName = (applicationDirectory + File.separator + 
				 friendlyFileName + File.separator + 
				 com.sun.enterprise.deployment.io.DescriptorConstants.S1AS_EJB_DD_ENTRY);

	file = new File(sunXmlFileName);

	return file;
    }

    public static void main (String args[]) {

        System.out.println("Ejb");

	String appDir = "/home/sreeni/TEMP/stateless-converterEjb_1";

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


	EjbArchivist ejbArchivist = new EjbArchivist();
	ejbArchivist.setXMLValidation(false);
	ejbArchivist.setClassLoader(null);

	com.sun.enterprise.deployment.Application application = null;
	try {
		application = (com.sun.enterprise.deployment.Application)
			ApplicationArchivist.openArchive(ejbArchivist, in, true);
	} catch (java.io.IOException ioe) {
	} catch (org.xml.sax.SAXParseException saxpe) {
	}

	com.sun.enterprise.ddgenerator.sun.Application app = 
		new com.sun.enterprise.ddgenerator.sun.Application(application, appDir); 

	app.generate();
    }

}
