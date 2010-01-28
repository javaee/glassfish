/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.javaee.full.deployment;

import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.deployment.archive.CompositeHandler;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.javaee.core.deployment.ApplicationHolder;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PreDestroy;
import org.xml.sax.SAXParseException;
import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.config.serverbeans.DasConfig;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.io.*;
import java.util.logging.Level;
import java.net.URLClassLoader;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.*;

/*;
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 16, 2009
 * Time: 3:33:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="ear")
public class EarHandler extends AbstractArchiveHandler implements CompositeHandler {

    @Inject
    Deployment deployment;

    @Inject
    Habitat habitat;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    ServerEnvironment env;

    @Inject
    DasConfig dasConfig;

    private static final String EAR_LIB = "ear_lib";
    private static final String EMBEDDED_RAR = "embedded_rar";

    private static LocalStringsImpl strings = new LocalStringsImpl(EarHandler.class);;

    private static XMLInputFactory xmlIf = null;

    static {
        xmlIf = XMLInputFactory.newInstance();
        xmlIf.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }


    public String getArchiveType() {
        return "ear";
    }

    public boolean handles(ReadableArchive archive) throws IOException {
        return DeploymentUtils.isEAR(archive);
    }

    @Override
    public void expand(ReadableArchive source, WritableArchive target, DeploymentContext context) throws IOException {
        // expand the top level first so we could read application.xml
        super.expand(source, target, context);

        ReadableArchive source2 = null;
        try {
            source2 = archiveFactory.openArchive(target.getURI());

            ApplicationHolder holder = 
                getApplicationHolder(source2, context, false);

            // now start to expand the sub modules 
            for (ModuleDescriptor md : holder.app.getModules()) {
                String moduleUri = md.getArchiveUri();
                ReadableArchive subArchive = null;
                WritableArchive subTarget = null;
                try {
                    subArchive = source2.getSubArchive(moduleUri);
                    if (subArchive == null) {
                        _logger.log(Level.WARNING, 
                            "Exception while locating sub archive: " + 
                            moduleUri);
                        continue;
                    }
                    // optimize performance by retrieving the archive handler
                    // based on module type first
                    ArchiveHandler subHandler = getArchiveHandlerFromModuleType(md.getModuleType());
                    if (subHandler == null) {
                        subHandler = deployment.getArchiveHandler(subArchive);
                    }
                    context.getModuleArchiveHandlers().put(
                        moduleUri, subHandler);
                    if (subHandler!=null) {
                        subTarget = target.createSubArchive(
                            FileUtils.makeFriendlyFilenameExtension(moduleUri));
                        subHandler.expand(subArchive, subTarget, context);
// Keep the original submodule file because the app client deployer needs it.
/*
                        // delete the original module file
                        File origSubArchiveFile = new File(
                            target.getURI().getSchemeSpecificPart(), moduleUri);
                        origSubArchiveFile.delete();
*/
                    }
                } catch(IOException ioe) {
                    _logger.log(Level.FINE, "Exception while processing " + 
                        moduleUri, ioe);
                } finally {
                    if (subArchive != null) {
                        subArchive.close();
                    }
                    if (subTarget != null) {
                        subTarget.close();
                    }
                }
            }
        } finally {
            if (source2 != null) {
                source2.close();
            }
        }
    }

    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        final ReadableArchive archive  = context.getSource();

        ApplicationHolder holder = 
            getApplicationHolder(archive, context, true);

        // the ear classloader hierachy will be 
        // ear lib classloader -> embedded rar classloader -> 
        // ear classloader -> various module classloaders
        DelegatingClassLoader embeddedConnCl;
        EarClassLoader cl;
        // add the libraries packaged in the application library directory
        try {
            String compatProp = context.getAppProps().getProperty(
                DeploymentProperties.COMPATIBILITY);
            // if user does not specify the compatibility property
            // let's see if it's defined in sun-application.xml
            if (compatProp == null) {
                SunApplicationXmlParser sunApplicationXmlParser = 
                    new SunApplicationXmlParser(context.getSourceDir());
                compatProp = sunApplicationXmlParser.getCompatibilityValue();
                if (compatProp != null) {
                    context.getAppProps().put(
                        DeploymentProperties.COMPATIBILITY, compatProp);
                }
            }
            EarLibClassLoader earLibCl = new EarLibClassLoader(ASClassLoaderUtil.getAppLibDirLibraries(context.getSourceDir(), holder.app.getLibraryDirectory(), compatProp), parent);
            embeddedConnCl = new DelegatingClassLoader(earLibCl);
            cl = new EarClassLoader(embeddedConnCl);

            // add ear lib to module classloader list so we can 
            // clean it up later
            cl.addModuleClassLoader(EAR_LIB, earLibCl);
        } catch (Exception e) {
            _logger.log(Level.SEVERE, strings.get("errAddLibs") ,e);
            throw new RuntimeException(e);
        }



        for (ModuleDescriptor md : holder.app.getModules()) {
            ReadableArchive sub = null;
            String moduleUri = md.getArchiveUri();
            try {
                sub = archive.getSubArchive(moduleUri);
                if (sub instanceof InputJarArchive) {
                    throw new IllegalArgumentException(strings.get("wrongArchType", moduleUri));
                }
            } catch (IOException e) {
                _logger.log(Level.FINE, "Sub archive " + moduleUri + " seems unreadable" ,e);
            }
            if (sub!=null) {
                try {
                    ArchiveHandler handler = 
                        context.getModuleArchiveHandlers().get(moduleUri);
                    if (handler == null) {
                        handler = getArchiveHandlerFromModuleType(md.getModuleType());
                        if (handler == null) {
                            handler = deployment.getArchiveHandler(sub);
                        }
                        context.getModuleArchiveHandlers().put(
                            moduleUri, handler);
                    }

                    if (handler!=null) {
                        ActionReport subReport = 
                            context.getActionReport().addSubActionsReport();
                        // todo : this is a hack, once again, 
                        // the handler is assuming a file:// url
                        ExtendedDeploymentContext subContext = 
                            new DeploymentContextImpl(subReport, 
                            context.getLogger(), 
                            sub, 
                            context.getCommandParameters(
                                DeployCommandParameters.class), env) {

                            @Override
                            public File getScratchDir(String subDirName) {
                                String modulePortion = Util.getURIName(
                                    getSource().getURI());
                                return (new File(super.getScratchDir(
                                    subDirName), modulePortion));
                            }
                        };

                        // sub context will store the root archive handler also 
                        // so we can figure out the enclosing archive type
                        subContext.setArchiveHandler
                            (context.getArchiveHandler());

                        sub.setParentArchive(context.getSource());

                        ClassLoader subCl = handler.getClassLoader(cl, subContext);
                        if (md.getModuleType().equals(XModuleType.EJB)) {
                            // for ejb module, we just add the ejb urls 
                            // to EarClassLoader and use that to load 
                            // ejb module
                            URL[] moduleURLs =
                                ((URLClassLoader)subCl).getURLs();
                            for (URL moduleURL : moduleURLs) {
                                cl.addURL(moduleURL);
                            }
                            cl.addModuleClassLoader(moduleUri, cl);
                            PreDestroy.class.cast(subCl).preDestroy();
                        } else if (md.getModuleType().equals(XModuleType.RAR)) {
                            embeddedConnCl.addDelegate(
                                (DelegatingClassLoader.ClassFinder)subCl);
                            cl.addModuleClassLoader(moduleUri, subCl);
                        } else {
                            if (subCl instanceof URLClassLoader && 
                                context.getTransientAppMetaData(ExtendedDeploymentContext.IS_TEMP_CLASSLOADER, Boolean.class)) {
                                // for temp classloader, we add all the module
                                // urls to the top level EarClassLoader
                                URL[] moduleURLs = 
                                    ((URLClassLoader)subCl).getURLs();
                                for (URL moduleURL : moduleURLs) {
                                    cl.addURL(moduleURL);
                                }
                            }
                            cl.addModuleClassLoader(moduleUri, subCl);
                        }
                    }
                } catch (IOException e) {
                    _logger.log(Level.SEVERE, strings.get("noClassLoader", moduleUri), e);
                }
            }
        }
        return cl;
    }

    public boolean accept(ReadableArchive source, String entryName) {
        // I am hiding everything but the metadata.
        return entryName.startsWith("META-INF");

    }

    private ApplicationHolder getApplicationHolder(ReadableArchive source, 
        DeploymentContext context, boolean isDirectory) {
        ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
        if (holder==null || holder.app==null) {
            try {
                long start = System.currentTimeMillis();
                ApplicationArchivist archivist = habitat.getComponent(ApplicationArchivist.class);
                archivist.setAnnotationProcessingRequested(true);

                String xmlValidationLevel = dasConfig.getDeployXmlValidation();
                archivist.setXMLValidationLevel(xmlValidationLevel);
                if (xmlValidationLevel.equals("none")) {
                    archivist.setXMLValidation(false);
                }

                holder = new ApplicationHolder(archivist.createApplication(
                    source, isDirectory));
                _logger.fine("time to read application.xml " + (System.currentTimeMillis() - start));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SAXParseException e) {
                throw new RuntimeException(e);
            }
            context.addModuleMetaData(holder);
        }

        if (holder.app==null) {
            throw new RuntimeException(strings.get("errReadMetadata"));
        }
        return holder;
    }

    // get archive handler from module type
    // performance optimization so we don't need to retrieve archive handler
    // the normal way which might involve annotation scanning
    private ArchiveHandler getArchiveHandlerFromModuleType(XModuleType type) {
        if (type.equals(XModuleType.WAR)) {
            return habitat.getComponent(ArchiveHandler.class, "war");
        } else if (type.equals(XModuleType.RAR)) {
            return habitat.getComponent(ArchiveHandler.class, "connector");
        } else if (type.equals(XModuleType.EJB) || 
            type.equals(XModuleType.CAR)) {
            return habitat.getComponent(ArchiveHandler.class, "DEFAULT");
        } else {
            return null;
        }
    }

    private class SunApplicationXmlParser {
        private XMLStreamReader parser = null;
        private String compatValue = null;

        SunApplicationXmlParser(File baseDir) throws XMLStreamException, FileNotFoundException {
            InputStream input = null;
            File f = new File(baseDir, "META-INF/sun-application.xml");
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
            skipRoot("sun-application");

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
