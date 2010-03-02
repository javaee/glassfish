/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.deploy.jar;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.loader.util.ASClassLoaderUtil;
import java.net.MalformedURLException;

import com.sun.enterprise.loader.ASURLClassLoader;
import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;

import java.net.URL;
import java.lang.RuntimeException;
import java.io.*;
import java.util.logging.Level;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.*;

/**
 * ArchiveHandler implementation for jar files
 *
 * @author Jerome Dochez
 */
@Service(name="DEFAULT")
public class JarHandler extends AbstractArchiveHandler implements ArchiveHandler {
    private static XMLInputFactory xmlIf = null;

    static {
        xmlIf = XMLInputFactory.newInstance();
        xmlIf.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    public String getArchiveType() {
        return "jar";
    }

    public boolean handles(ReadableArchive archive) {
        if (DeploymentUtils.isEAR(archive)) {
            // I should not handle ear, so ear support must not be available
            // in this distribution
            throw new RuntimeException(
                "no container associated with application of type : ear");
        }
        // but I handle everything that looks like a jar...   
        return true;
    }

    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        ASURLClassLoader cloader = new ASURLClassLoader(parent);
        try {              
            String compatProp = context.getAppProps().getProperty(
                DeploymentProperties.COMPATIBILITY);
            // if user does not specify the compatibility property
            // let's see if it's defined in sun-ejb-jar.xml
            if (compatProp == null) {
                SunEjbJarXMLParser sunEjbJarXMLParser =
                    new SunEjbJarXMLParser(context.getSourceDir());
                compatProp = sunEjbJarXMLParser.getCompatibilityValue();
                if (compatProp != null) {
                    context.getAppProps().put(
                        DeploymentProperties.COMPATIBILITY, compatProp);
                }
            }

            // if the compatibility property is set to "v2", we should add
            // all the jars under the ejb module root to maintain backward
            // compatibility of v2 jar visibility
            if (compatProp != null && compatProp.equals("v2")) {
                List<URL> moduleRootLibraries = 
                    ASClassLoaderUtil.getURLsAsList(null, 
                    new File[] {context.getSourceDir()}, true);
                for (URL url : moduleRootLibraries) {
                    cloader.addURL(url);
                }
            }

            cloader.addURL(context.getSource().getURI().toURL());
            cloader.addURL(context.getScratchDir("ejb").toURI().toURL());
            if (context.getArchiveHandler().getClass(
                ).getAnnotation(Service.class).name().equals("ear")) {
                // add libarries referenced from manifest 
                for (URL url : getManifestLibraries(context)) {
                    cloader.addURL(url);
                }
            }
        } catch(Exception e) {
            _logger.log(Level.SEVERE, e.getMessage());
            return null;
        }
        return cloader;
    }

    private class SunEjbJarXMLParser {
        private XMLStreamReader parser = null;
        private String compatValue = null;

        SunEjbJarXMLParser(File baseDir) throws XMLStreamException, FileNotFoundException {
            InputStream input = null;
            File f = new File(baseDir, "META-INF/sun-ejb-jar.xml");
            if (f.exists()) {
                input = new FileInputStream(f);
                try {
                    read(input);
                } finally {
                    if (parser != null) {
                        try {
                            parser.close();
                        } catch(Exception ex) {
                            // ignore
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                        } catch(Exception ex) {
                            // ignore
                        }
                    }
                }
            }
        }

        private void read(InputStream input) throws XMLStreamException {
            parser = xmlIf.createXMLStreamReader(input);

            int event = 0;
            boolean done = false;
            skipRoot("sun-ejb-jar");

            while (!done && (event = parser.next()) != END_DOCUMENT) {

                if (event == START_ELEMENT) {
                    String name = parser.getLocalName();
                    if (DeploymentProperties.COMPATIBILITY.equals(name)) {
                        compatValue = parser.getElementText();
                       done = true;
                    } else {
                        skipSubTree(name);
                    }
                }
            }
        }

        private void skipRoot(String name) throws XMLStreamException { 
            while (true) {
                int event = parser.next();
                if (event == START_ELEMENT) {
                    if (!name.equals(parser.getLocalName())) {
                       throw new XMLStreamException();
                    }
                    return; 
                }
            }
        }

        private void skipSubTree(String name) throws XMLStreamException {
            while (true) {
                int event = parser.next();
                if (event == END_DOCUMENT) {
                    throw new XMLStreamException();
                } else if (event == END_ELEMENT && name.equals(parser.getLocalName())) {
                    return;
                }
            }
        }

        String getCompatibilityValue() {
            return compatValue;
        }
    }
}
