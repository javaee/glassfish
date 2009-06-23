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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.appclient.client.acc.FacadeLaunchable;
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

    void validateDescriptor();

    URI getURI();

    static class LaunchableUtil {

        private static final LocalStringManager localStrings = new LocalStringManagerImpl(Launchable.LaunchableUtil.class);
        static Launchable newLaunchable(final URI uri,
                final String callerSuppliedMainClassName,
                final String callerSuppliedAppName) throws IOException, BootException, URISyntaxException, XMLStreamException, SAXParseException, UserError {
            /*
             * Make sure the requested URI exists and is readable.
             */
            ArchiveFactory af = ACCModulesManager.getComponent(ArchiveFactory.class);
            ReadableArchive ra = null;
            try {
                ra = af.openArchive(uri);
            } catch (IOException e) {
                final String msg = localStrings.getLocalString(
                        Launchable.class,
                        "appclient.cannotFindJarFile",
                        "Could not locate the requested client JAR file {0}; please try again with an existing, valid client JAR",
                        new Object[] {uri});
                throw new UserError(msg);
            }

            Launchable result = FacadeLaunchable.newFacade(ra, callerSuppliedMainClassName, callerSuppliedAppName);
            if (result != null) {
                ra.close();
            } else {
                /*
                 * If newFacade found a facade JAR but could not find a suitable
                 * client it will have thrown a UserError.  If we're here, then
                 * newFacade did not have a facade to work with.  So the caller-
                 * provided URI should refer to an undeployed EAR or an undeployed
                 * app client JAR.
                 */
                result = UndeployedLaunchable.newUndeployedLaunchable(ra,
                        callerSuppliedMainClassName, callerSuppliedAppName,
                        Thread.currentThread().getContextClassLoader());
            }
            if (result != null) {
                URL clientOrFacadeURL = new URL("file:" + result.getURI().getSchemeSpecificPart());
                ACCClassLoader.instance().appendURL(clientOrFacadeURL);
                return result;
            }
            final String msg = localStrings.getLocalString(Launchable.class,
                    "appclient.invalidArchive",
                    "The location {0} could not be opened as an archive; an app client or an enterprise app was expected",
                    new Object[] {uri});

            throw new UserError(msg);
        }

        static Launchable newLaunchable(final Class mainClass) {
            return new MainClass(mainClass);
        }
        
        static boolean matchesMainClassName(final ReadableArchive archive, final String callerSpecifiedMainClassName) throws IOException {
            return (callerSpecifiedMainClassName != null) &&
                            archive.exists(classNameToArchivePath(callerSpecifiedMainClassName));
        }

        static boolean matchesName(final ReadableArchive archive, final String appClientName) throws IOException, SAXParseException {

            ApplicationClientDescriptor acd = null;
            AppClientArchivist archivist = new AppClientArchivist();
            acd = archivist.open(archive);
            final String moduleID = acd.getModuleID();
            final String displayName = acd.getDisplayName();
            return (   (moduleID != null && moduleID.equals(appClientName))
                    || (displayName != null && displayName.equals(appClientName)));

//            XMLInputFactory f = XMLInputFactory.newInstance();
//
//            InputStream descriptorStream = archive.getEntry("META-INF/application-client.xml");
//            if (descriptorStream == null) {
//                return false;
//            }
//            XMLStreamReader reader = f.createXMLStreamReader(descriptorStream);
//            String displayName = null;
//            while (displayName == null && reader.hasNext()) {
//                if ((reader.next() == XMLEvent.START_ELEMENT) &&
//                    (reader.getName().getLocalPart().equals("display-name"))) {
//                        displayName = reader.getElementText();
//                        break;
//                }
//            }
//            reader.close();
//            return (appClientName != null && appClientName.equals(displayName));
        }

        private static String classNameToArchivePath(final String className) {
            return new StringBuilder(className.replace('.', '/'))
                    .append(".class").toString();
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

        private ClassLoader classLoader = null;

        private AppClientArchivist archivist = null;

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
                archivist = new AppClientArchivist();
                acDesc = (ApplicationClientDescriptor) archivist
                        .getDefaultBundleDescriptor();
                archivist.setDescriptor(acDesc);
                this.classLoader = loader;
            }
            return acDesc;
        }

        public void validateDescriptor() {
            archivist.validate(classLoader);
        }

        public URI getURI() {
            return null;
        }



    }
}
