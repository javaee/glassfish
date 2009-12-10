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
package com.sun.enterprise.glassfish.web;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.web.loader.WebappClassLoader;
import org.jvnet.hk2.annotations.Service;
import org.apache.naming.resources.FileDirContext;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.*;

import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;
import com.sun.logging.LogDomains;

/**
 * Implementation of the ArchiveHandler for war files.
 *
 * @author Jerome Dochez
 */
@Service(name="war")
public class WarHandler extends AbstractArchiveHandler implements ArchiveHandler {
    private static XMLInputFactory xmlIf = null;

    static {
        xmlIf = XMLInputFactory.newInstance();
        xmlIf.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    private static final Logger logger = LogDomains.getLogger(WarHandler.class, LogDomains.WEB_LOGGER);
    private static final ResourceBundle rb = logger.getResourceBundle();

    public String getArchiveType() {
        return "war";               
    }

    public boolean handles(ReadableArchive archive) {
        if (DeploymentUtils.isEAR(archive)) {
            // I should not handle ear, so ear support must not be available
            // in this distribution
            throw new RuntimeException(
                "no container associated with application of type : ear");
        }

        return DeploymentUtils.isWebArchive(archive);
    }

    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        WebappClassLoader cloader = new WebappClassLoader(parent);
        try {
            FileDirContext r = new FileDirContext();
            File base = new File(context.getSource().getURI());
            r.setDocBase(base.getAbsolutePath());
            SunWebXmlParser sunWebXmlParser = new SunWebXmlParser(base.getAbsolutePath());

            cloader.setResources(r);
            cloader.addRepository("WEB-INF/classes/", new File(base, "WEB-INF/classes/"));
            if (context.getScratchDir("ejb") != null) {
                cloader.addRepository(context.getScratchDir("ejb").toURI().toURL().toString().concat("/"));
            }
            if (context.getScratchDir("jsp") != null) {
                cloader.setWorkDir(context.getScratchDir("jsp"));
            }

            if (context.getArchiveHandler().getClass(
                ).getAnnotation(Service.class).name().equals("ear")) {
                // add libarries referenced from manifest
                for (URL url : getManifestLibraries(context)) {
                    cloader.addRepository(url.toString());
                }
            }

            configureLoaderAttributes(cloader, sunWebXmlParser, base);
            configureLoaderProperties(cloader, sunWebXmlParser, base);
            
        } catch(MalformedURLException malex) {
            logger.log(Level.SEVERE, malex.getMessage());
            logger.log(Level.FINE, malex.getMessage(), malex);            
        } catch(XMLStreamException xse) {
            logger.log(Level.SEVERE, xse.getMessage());
            logger.log(Level.FINE, xse.getMessage(), xse);
        } catch(FileNotFoundException fnfe) {
            logger.log(Level.SEVERE, fnfe.getMessage());
            logger.log(Level.FINE, fnfe.getMessage(), fnfe);
        }

        cloader.start();

        return cloader;
    }

    protected void configureLoaderAttributes(WebappClassLoader cloader,
            SunWebXmlParser sunWebXmlParser, File base) {

        boolean delegate = sunWebXmlParser.isDelegate();
        cloader.setDelegate(delegate);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("WebModule[" + sunWebXmlParser.getBase() +
                        "]: Setting delegate to " + delegate);
        }

        String extraClassPath = sunWebXmlParser.getExtraClassPath();
        if (extraClassPath != null) {
            // Parse the extra classpath into its ':' and ';' separated
            // components. Ignore ':' as a separator if it is preceded by
            // '\'
            String[] pathElements = extraClassPath.split(";|((?<!\\\\):)");
            if (pathElements != null) {
                for (String path : pathElements) {
                    path = path.replace("\\:", ":");
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("WarHandler[" + sunWebXmlParser.getBase() +
                                    "]: Adding " + path +
                                    " to the classpath");
                    }

                    try {
                        URL url = new URL(path);
                        cloader.addRepository(path);
                    } catch (MalformedURLException mue1) {
                        // Not a URL, interpret as file
                        File file = new File(path);
                        // START GlassFish 904
                        if (!file.isAbsolute()) {
                            // Resolve relative extra class path to the
                            // context's docroot
                            file = new File(base.getPath(), path);
                        }
                        // END GlassFish 904

                        try {
                            URL url = file.toURI().toURL();
                            cloader.addRepository(url.toString());
                        } catch (MalformedURLException mue2) {
                            String msg = rb.getString(
                                "webcontainer.classpathError");
                            Object[] params = { path };
                            msg = MessageFormat.format(msg, params);
                            logger.log(Level.SEVERE, msg, mue2);
                        }
                    }
                }
            }
        }
    }

    protected void configureLoaderProperties(WebappClassLoader cloader,
            SunWebXmlParser sunWebXmlParser, File base) {

        cloader.setUseMyFaces(sunWebXmlParser.isUseBundledJSF());

        File libDir = new File(base, "WEB-INF/lib");
        if (libDir.exists()) {
            int baseFileLen = base.getPath().length();
            final boolean ignoreHiddenJarFiles = sunWebXmlParser.isIgnoreHiddenJarFiles();

            for (File file : libDir.listFiles(
                    new FileFilter() {
                        public boolean accept(File pathname) {
                            String fileName = pathname.getName();
                            return ((fileName.endsWith(".jar") ||
                                        fileName.endsWith(".zip")) &&
                                    (!ignoreHiddenJarFiles ||
                                    !fileName.startsWith(".")));
                        }
                    }))
            {
                try {
                    cloader.addJar(file.getPath().substring(baseFileLen),
                                   new JarFile(file), file);
                } catch (Exception e) {
                    // Catch and ignore any exception in case the JAR file
                    // is empty.
                }
            }
        }
    }

    protected class SunWebXmlParser {
        private String baseStr = null;
        private XMLStreamReader parser = null;

        //XXX need to compute the default delegate depending on the version of dtd
        /*
         * The DOL will *always* return a value: If 'delegate' has not been
         * configured in sun-web.xml, its default value will be returned,
         * which is FALSE in the case of sun-web-app_2_2-0.dtd and
         * sun-web-app_2_3-0.dtd, and TRUE in the case of
         * sun-web-app_2_4-0.dtd.
         */
        private boolean delegate = true;

        private boolean ignoreHiddenJarFiles = false;
        private boolean useBundledJSF = false;
        private String extraClassPath = null;

        SunWebXmlParser(String baseStr) throws XMLStreamException, FileNotFoundException {
            this.baseStr = baseStr;
            InputStream input = null;
            File f = new File(baseStr, "WEB-INF/sun-web.xml");
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
            boolean inClassLoader = false;
            skipRoot("sun-web-app");

            while (parser.hasNext() && (event = parser.next()) != END_DOCUMENT) {
                if (event == START_ELEMENT) {
                    String name = parser.getLocalName();
                    if ("class-loader".equals(name)) {
                        int count = parser.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            String attrName = parser.getAttributeName(i).getLocalPart();
                            if ("delegate".equals(attrName)) {
                                delegate = Boolean.valueOf(parser.getAttributeValue(i));
                            } else if ("extra-class-path".equals(attrName)) {
                                extraClassPath = parser.getAttributeValue(i);
                            } else if ("dynamic-reload-interval".equals(attrName)) {
                                if (parser.getAttributeValue(i) != null) {
                                    // Log warning if dynamic-reload-interval is specified
                                    // in sun-web.xml since it is not supported
                                    if (logger.isLoggable(Level.WARNING)) {
                                        logger.log(Level.WARNING, "webcontainer.dynamicReloadInterval");
                                    }
                                }
                            }
                        }
                        inClassLoader = true;
                    } else if (inClassLoader && "property".equals(name)) {
                        int count = parser.getAttributeCount();
                        String propName = null;
                        String value = null;
                        for (int i = 0; i < count; i++) {
                            String attrName = parser.getAttributeName(i).getLocalPart();
                            if ("name".equals(attrName)) {
                                propName = parser.getAttributeValue(i);
                            } else if ("value".equals(attrName)) {
                                value = parser.getAttributeValue(i);
                            }
                        }

                        if (propName == null || value == null) {
                            throw new IllegalArgumentException(
                                rb.getString("webcontainer.nullWebProperty"));
                        }

                        if ("ignoreHiddenJarFiles".equals(propName)) {
                            ignoreHiddenJarFiles = Boolean.valueOf(value);
                        } else {
                            Object[] params = { propName, value };
                            if (logger.isLoggable(Level.WARNING)) {
                                logger.log(Level.WARNING, "webcontainer.invalidProperty",
                                           params);
                            }
                        }
                    } else if ("property".equals(name)) {
                        int count = parser.getAttributeCount();
                        String propName = null;
                        String value = null;
                        for (int i = 0; i < count; i++) {
                            String attrName = parser.getAttributeName(i).getLocalPart();
                            if ("name".equals(attrName)) {
                                propName = parser.getAttributeValue(i);
                            } else if ("value".equals(attrName)) {
                                value = parser.getAttributeValue(i);
                            }
                        }

                        if (propName == null || value == null) {
                            throw new IllegalArgumentException(
                                rb.getString("webcontainer.nullWebProperty"));
                        }

                        if("useMyFaces".equalsIgnoreCase(propName)) {
                            useBundledJSF = Boolean.valueOf(value);
                        } else if("useBundledJsf".equalsIgnoreCase(propName)) {
                            useBundledJSF = Boolean.valueOf(value);
                        }
                    } else {
                        skipSubTree(name);
                    }
                } else if (inClassLoader && event == END_ELEMENT) {
                    if ("class-loader".equals(parser.getLocalName())) {
                        inClassLoader = false;
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

        String getBase() {
            return baseStr;
        }

        boolean isDelegate() {
            return delegate;
        }

        boolean isIgnoreHiddenJarFiles() {
            return ignoreHiddenJarFiles;
        }

        String getExtraClassPath() {
            return extraClassPath;
        }

        boolean isUseBundledJSF() {
            return useBundledJSF;
        }
    }
}
