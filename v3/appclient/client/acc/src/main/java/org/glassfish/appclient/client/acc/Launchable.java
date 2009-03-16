/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.module.bootstrap.BootException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.appclient.client.acc.Launchable.Facade;
import org.xml.sax.SAXParseException;

/**
 * Something launchable by the ACC - an app client archive or a class.
 *
 * @author tjquinn
 */
interface Launchable {

    /**
     * Returns the main class for this Launchable.
     * @return the main class
     *
     * @throws java.lang.ClassNotFoundException
     */
    Class getMainClass() throws ClassNotFoundException;

    ApplicationClientDescriptor getDescriptor(ClassLoader loader) throws IOException, SAXParseException;


    static class Util {
        static Launchable newLaunchable(final URI uri,
                final String callerSuppliedMainClassName,
                final String callerSuppliedAppName) throws IOException, BootException, URISyntaxException, XMLStreamException {
            Launchable result = Facade.newFacade(uri, callerSuppliedMainClassName, callerSuppliedAppName);
            if (result != null) {
                return result;
            }
            // XXX add logic to handle raw ear and raw client cases
            throw new IllegalArgumentException("not yet supported");
        }

        static Launchable newLaunchable(final Class mainClass) {
            throw new UnsupportedOperationException("Don't yet support launching a class");
        }
        
        private static boolean matchesMainClassName(final ReadableArchive archive, final String callerSpecifiedMainClassName) throws IOException {
            return (callerSpecifiedMainClassName != null) &&
                            archive.exists(classNameToArchivePath(callerSpecifiedMainClassName));
        }

        private static boolean matchesName(final ReadableArchive archive, final String appClientName) throws IOException, XMLStreamException {
            XMLInputFactory f = XMLInputFactory.newInstance();


            XMLStreamReader reader = f.createXMLStreamReader(archive.getEntry("META-INF/application-client.xml"));
            String displayName = null;
            while (displayName == null && reader.hasNext()) {
                if ((reader.next() == XMLEvent.START_ELEMENT) &&
                    (reader.getName().getLocalPart().equals("display-name"))) {
                        displayName = reader.getElementText();
                        break;
                }
            }
            reader.close();
            return (appClientName != null && appClientName.equals(displayName));
        }

        private static String classNameToArchivePath(final String className) {
            return new StringBuilder(className.replace('.', '/'))
                    .append(".class").toString();
        }
    }

    static class Facade implements Launchable {

        public static final Attributes.Name GLASSFISH_APPCLIENT_GROUP = new Attributes.Name("GlassFish-AppClient-Group");
        public static final Attributes.Name GLASSFISH_APPCLIENT_MAIN_CLASS = new Attributes.Name("Glassfish-AppClient-Main-Class");
        public static final Attributes.Name GLASSFISH_APPCLIENT = new Attributes.Name("GlassFish-AppClient");

        private final String mainClassNameToLaunch;
        private final URI[] classPathURIs;
        private final ReadableArchive clientRA;

        private ApplicationClientDescriptor acDesc = null;

        /**
         * Creates a new facade for an app client archive.
         * @param mainAttrs
         * @throws java.io.IOException
         */
        Facade(final Attributes mainAttrs,
                final ReadableArchive facadeRA) throws IOException, URISyntaxException {
            this(mainAttrs,
                 openOriginalArchive(facadeRA, mainAttrs.getValue(GLASSFISH_APPCLIENT)),
                 mainAttrs.getValue(GLASSFISH_APPCLIENT_MAIN_CLASS));
        }

        private static ReadableArchive openOriginalArchive(
                final ReadableArchive facadeArchive,
                final String relativeURIToOriginalJar) throws IOException, URISyntaxException {

            URI uriToOriginal = facadeArchive.getURI().resolve(relativeURIToOriginalJar);

            final ArchiveFactory af = org.glassfish.appclient.client.acc.Util.getArchiveFactory();
            return af.openArchive(uriToOriginal);
        }

        /**
         * Creates a new facade, primarily for an app client that had been
         * nested inside an EAR.
         * @param mainAttrs
         * @param clientRA
         * @param mainClassNameToLaunch
         */
        Facade(final Attributes mainAttrs,
                final ReadableArchive clientRA,
                final String mainClassNameToLaunch) throws IOException {
            this.mainClassNameToLaunch = mainClassNameToLaunch;
            this.clientRA = clientRA;
            this.classPathURIs = toURIs(mainAttrs.getValue(Name.CLASS_PATH));
        }

        protected URI[] toURIs(final String uriList) {
            String[] uris = uriList.split(" ");
            URI[] result = new URI[uris.length];
            for (int i = 0; i < uris.length; i++) {
                result[i] = URI.create(uris[i]);
            }
            return result;
        }

        static Facade newFacade(final URI uri,
                final String callerSuppliedMainClassName,
                final String callerSuppliedAppName) throws IOException, BootException, URISyntaxException, XMLStreamException {

            ArchiveFactory af = ACCModulesManager.getComponent(ArchiveFactory.class);
            ReadableArchive facadeRA = af.openArchive(uri);
            Manifest mf = facadeRA.getManifest();

            final Attributes mainAttrs = mf.getMainAttributes();
            Facade result = null;
            if (mainAttrs.containsKey(GLASSFISH_APPCLIENT)) {
                result = new Facade(mainAttrs, facadeRA);
            } else {
                facadeRA.close();
                final String facadeGroupURIs = mainAttrs.getValue(GLASSFISH_APPCLIENT_GROUP);
                if (facadeGroupURIs != null) {
                    result = selectFacadeFromGroup(
                            af,
                            facadeGroupURIs,
                            callerSuppliedMainClassName,
                            callerSuppliedAppName);
                } else {
                    return null;
                }
            }
            return result;
        }

        public Class getMainClass() throws ClassNotFoundException {
            return Class.forName(mainClassNameToLaunch, true, Thread.currentThread().getContextClassLoader());
        }

        public ApplicationClientDescriptor getDescriptor(final ClassLoader loader) throws IOException, SAXParseException {
            if (acDesc == null) {
//                ArchivistFactory f = org.glassfish.appclient.client.acc.Util.getArchivistFactory();
//                AppClientArchivist archivist = (AppClientArchivist) f.getArchivist(clientRA, loader);
                AppClientArchivist archivist = new AppClientArchivist();
                archivist.open(clientRA);
                acDesc = archivist.getDescriptor();
            }
            return acDesc;
        }

        public URI[] getClassPathURIs() {
            return classPathURIs;
        }

        private static Facade selectFacadeFromGroup(
                final ArchiveFactory af,
                final String groupURIs,
                final String callerSpecifiedMainClassName,
                final String callerSpecifiedAppClientName) throws IOException, BootException, URISyntaxException, XMLStreamException {

            String[] archiveURIs = groupURIs.split(" ");

            /*
             * Search the app clients in the group in order, checking each for
             * a match on either the caller-specified main class or the caller-specified
             * client name.
             */
            for (String uriText : archiveURIs) {
                URI facadeURI = URI.create(uriText);
                ReadableArchive facadeRA = af.openArchive(facadeURI);
                Manifest facadeMF = facadeRA.getManifest();
                Attributes facadeMainAttrs = facadeMF.getMainAttributes();
                facadeRA.close();

                URI clientURI = URI.create(facadeMF.getMainAttributes().getValue(GLASSFISH_APPCLIENT));
                ReadableArchive clientRA = af.openArchive(clientURI);
                /*
                 * Look for an entry corresponding to the
                 * main class the caller requested.
                 */
                Facade facade = null;
                if (Util.matchesMainClassName(clientRA, callerSpecifiedMainClassName)) {
                    facade = new Facade(facadeMainAttrs, clientRA, callerSpecifiedMainClassName);
                } else if (Util.matchesName(clientRA, callerSpecifiedAppClientName)) {
                    facade = new Facade(facadeMainAttrs, clientRA,
                            facadeMainAttrs.getValue(GLASSFISH_APPCLIENT_MAIN_CLASS));
                }
                return facade;
            }
            return null;
        }
    }
}
