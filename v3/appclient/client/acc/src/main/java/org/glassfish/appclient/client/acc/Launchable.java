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
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.module.bootstrap.BootException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
            // XXX either the user did not choose an existing class or this is not a facade
            throw new IllegalArgumentException("could not locate selected class");
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

            InputStream descriptorStream = archive.getEntry("META-INF/application-client.xml");
            if (descriptorStream == null) {
                return false;
            }
            XMLStreamReader reader = f.createXMLStreamReader(descriptorStream);
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

    /**
     * Represents a generated JAR created during deployment corresponding to
     * the developer's original app client JAR or EAR.  Even if the facade object
     * represents an EAR facade, it uses the caller-supplied main class name
     * and/or caller-supplied app client name to select one of the app client
     * facades listed in the group facade.  That is, once fully initialized,
     * a given Facade instance represents the single app client to be launched.
     */
    static class Facade implements Launchable {

        /** name of a manifest entry in an EAR facade listing the URIs of the individual app client facades in the group */
        public static final Attributes.Name GLASSFISH_APPCLIENT_GROUP = new Attributes.Name("GlassFish-AppClient-Group");

        /** name of a manifest entry in an app client facade indicating the app client's main class */
        public static final Attributes.Name GLASSFISH_APPCLIENT_MAIN_CLASS = new Attributes.Name("Glassfish-AppClient-Main-Class");

        /** name of a manifest entry in an app client facade listing the URI of the developer's original app client JAR */
        public static final Attributes.Name GLASSFISH_APPCLIENT = new Attributes.Name("GlassFish-AppClient");

        private final String mainClassNameToLaunch;
        private final URI[] classPathURIs;
        private final ReadableArchive clientRA;

        private URI facadeURI;

        private ApplicationClientDescriptor acDesc = null;

        /**
         * Creates a new facade for an app client archive.
         * @param mainAttrs main attributes from the facade's manifest
         * @param facadeRA the readable archive already opened for this facade
         * @throws java.io.IOException
         */
        Facade(final Attributes mainAttrs,
                final ReadableArchive facadeRA) throws IOException, URISyntaxException {
            this(facadeRA.getURI(),
                 mainAttrs,
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
         * @param facadeURI URI to the EAR facade
         * @param mainAttrs main attributes from the EAR facade's manifest
         * @param clientRA readable archive to the
         * @param mainClassNameToLaunch
         */
        Facade(final URI facadeURI,
                final Attributes mainAttrs,
                final ReadableArchive clientRA,
                final String mainClassNameToLaunch) throws IOException {
            this.facadeURI = facadeURI;
            this.mainClassNameToLaunch = mainClassNameToLaunch;
            this.clientRA = clientRA;
            this.classPathURIs = toURIs(mainAttrs.getValue(Name.CLASS_PATH));
        }

        URI getURI() {
            return facadeURI;
        }

        protected URI[] toURIs(final String uriList) {
            String[] uris = uriList.split(" ");
            URI[] result = new URI[uris.length];
            for (int i = 0; i < uris.length; i++) {
                result[i] = URI.create(uris[i]);
            }
            return result;
        }

        /**
         * Returns a Facade object for the specified app client group facade.
         * <p>
         * The caller-supplied information is used to select the first app client
         * facade in the app client group that matches either the main class or
         * the app client name.  If the caller-supplied values are both null then
         * the method returns the first app client facade in the group.  If the
         * caller passes at least one non-null selector (main class or app client
         * name) but no app client matches, the method returns null.
         *
         * @param groupFacadeURI URI to the app client group facade
         * @param callerSuppliedMainClassName main class name to find; null if
         * the caller does not require selection based on the main class name
         * @param callerSuppliedAppName (display) nane of the app client to find; null
         * if the caller does not require selection based on display name
         * @return a Facade object representing the selected app client facade;
         * null if at least one of callerSuppliedMainClasName and callerSuppliedAppName
         * is not null and no app client matched the selection criteria
         * @throws java.io.IOException
         * @throws com.sun.enterprise.module.bootstrap.BootException
         * @throws java.net.URISyntaxException
         * @throws javax.xml.stream.XMLStreamException
         */
        static Facade newFacade(final URI groupFacadeURI,
                final String callerSuppliedMainClassName,
                final String callerSuppliedAppName) throws IOException, BootException, URISyntaxException, XMLStreamException {

            ArchiveFactory af = ACCModulesManager.getComponent(ArchiveFactory.class);
            ReadableArchive facadeRA = af.openArchive(groupFacadeURI);
            Manifest mf = facadeRA.getManifest();

            final Attributes mainAttrs = mf.getMainAttributes();
            Facade result = null;
            if (mainAttrs.containsKey(GLASSFISH_APPCLIENT)) {
                /*
                 * The facade contains the GlassFish-AppClient manifest entry
                 * so it is an app client facade (as opposed to an app
                 * client group facade).  Create an app client facade object.
                 */
                result = new Facade(mainAttrs, facadeRA);
            } else {
                /*
                 * The facade does not contain GlassFish-AppClient so if it is
                 * a facade it must be an app client group facade.  Select
                 * which app client facade within the group, if any, matches
                 * the caller's selection criteria.
                 */
                facadeRA.close();
                final String facadeGroupURIs = mainAttrs.getValue(GLASSFISH_APPCLIENT_GROUP);
                if (facadeGroupURIs != null) {
                    result = selectFacadeFromGroup(
                            groupFacadeURI,
                            af,
                            facadeGroupURIs,
                            callerSuppliedMainClassName,
                            callerSuppliedAppName);
                    if (result != null) {
                        /*
                         * Add the selected app client facade from the group
                         * to the class path.
                         * We don't need to do this in the earlier case - when
                         * the facade is itself an app client facade - because
                         * the facade is already on the system class path because
                         * the app client was launched using a java command that
                         * specified the app client facade JAR file or had the
                         * app client facade in the class path (e.g., if the user
                         * specified -classpath appClientFacade.jar).
                         *
                         */
                        URL clientFacadeURL = new URL("file:" + result.getURI().getSchemeSpecificPart()); // strip off the jar:
                        ACCClassLoader.instance().appendURL(clientFacadeURL);
                    }
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
                final URI groupFacadeURI,
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
                URI clientFacadeURI = groupFacadeURI.resolve(uriText);
                ReadableArchive clientFacadeRA = af.openArchive(clientFacadeURI);
                Manifest facadeMF = clientFacadeRA.getManifest();
                Attributes facadeMainAttrs = facadeMF.getMainAttributes();
                clientFacadeRA.close();

                URI clientURI = groupFacadeURI.resolve(facadeMF.getMainAttributes().getValue(GLASSFISH_APPCLIENT));
                ReadableArchive clientRA = af.openArchive(clientURI);
                /*
                 * Look for an entry corresponding to the
                 * main class or app name the caller requested.  Treat as a
                 * special case if the user specifies no main class and no
                 * app name - use the first app client present.
                 */
                Facade facade = null;
                if (callerSpecifiedMainClassName == null &&
                    callerSpecifiedAppClientName == null) {
                    facade = new Facade(clientFacadeURI, facadeMainAttrs, clientRA,
                            facadeMainAttrs.getValue(GLASSFISH_APPCLIENT_MAIN_CLASS));
                } else if (Util.matchesMainClassName(clientRA, callerSpecifiedMainClassName)) {
                    facade = new Facade(clientFacadeURI, facadeMainAttrs, clientRA, callerSpecifiedMainClassName);
                } else if (Util.matchesName(clientRA, callerSpecifiedAppClientName)) {
                    facade = new Facade(clientFacadeURI, facadeMainAttrs, clientRA,
                            facadeMainAttrs.getValue(GLASSFISH_APPCLIENT_MAIN_CLASS));
                }
                if (facade != null) {
                    return facade;
                }
            }
            return null;
        }
    }

    /**
     * Represents a Launchable main class which the caller specifies by the
     * main class itself, rather than a facade JAR or an original developer-provided
     * JAR file.
     */
    static class MainClass implements Launchable {

        private final Class mainClass;

        private ApplicationClientDescriptor acDesc = null;

        MainClass(final Class mainClass) {
            this.mainClass = mainClass;
        }

        public Class getMainClass() throws ClassNotFoundException {
            return mainClass;
        }

        public ApplicationClientDescriptor getDescriptor(ClassLoader loader) throws IOException, SAXParseException {
            /*
             * There is no developer-provided descriptor possible so just
             * use a default one.
             */
            if (acDesc == null) {
                acDesc = (ApplicationClientDescriptor) org.glassfish.appclient.client.acc.Util
                        .getArchivistFactory().getArchivist(XModuleType.CAR)
                        .getDefaultBundleDescriptor();
            }
            return acDesc;
        }

    }
}
