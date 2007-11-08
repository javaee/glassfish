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

package com.sun.enterprise.appclient;

import com.sun.enterprise.deployment.annotation.AnnotationProcessorException;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.util.AnnotationDetector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import org.xml.sax.SAXParseException;

/**
 * Represents an app client that is in a stand-alone archive, not inside an
 * enterprise app archive and not a .class file.
 * @author tjquinn
 */
public class StandAloneAppClientInfo extends AppClientInfo {
    
    public StandAloneAppClientInfo(
            boolean isJWS, Logger logger, File archive, 
            Archivist archivist, String mainClassFromCommandLine) 
        throws IOException, ClassNotFoundException, 
               URISyntaxException, SAXParseException {
        super(isJWS, logger, archive, archivist, mainClassFromCommandLine);
    }

    protected AbstractArchive expand(File file) 
        throws IOException, Exception {
        InputJarArchive  appArchive = new InputJarArchive ();
        appArchive.open(file.getAbsolutePath());
        return appArchive;
    }

    protected boolean deleteAppClientDir() {
        return false;
    }

    protected void messageDescriptor(RootDeploymentDescriptor d, 
        Archivist archivist, AbstractArchive archive) 
            throws IOException, AnnotationProcessorException {
        ApplicationClientDescriptor appClient = (ApplicationClientDescriptor)d;
        appClient.getModuleDescriptor().setStandalone(true);
    }

    protected boolean classContainsAnnotation(
            String entry, AnnotationDetector detector, 
            AbstractArchive archive, ApplicationClientDescriptor descriptor) 
            throws FileNotFoundException, IOException {
        JarFile jar = null;
        try {
            jar = new JarFile(archive.getArchiveUri());
            JarEntry mainClassEntry = jar.getJarEntry(entry);
            return detector.containsAnnotation(jar, mainClassEntry);
        } catch (Throwable thr) {
            throw new RuntimeException(localStrings.getString(
                "appclient.errorCheckingAnnos"), thr);
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException ioe) {
                    throw new RuntimeException(localStrings.getString(
                        "appclient.errorClosingJar", archive.getArchiveUri()), ioe);
                }
            }
        }
    }
}
