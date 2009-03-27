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
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import java.io.IOException;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.xml.sax.SAXParseException;

/**
 *
 * @author tjquinn
 */
public class UndeployedLaunchable implements Launchable {

    private static final LocalStringsImpl localStrings = new LocalStringsImpl(UndeployedLaunchable.class);

    private final String callerSuppliedMainClassName;

    private ApplicationClientDescriptor acDesc = null;

    private AppClientArchivist archivist = null;

    private final ReadableArchive clientRA;

    private ClassLoader classLoader = null;


    static UndeployedLaunchable newUndeployedLaunchable(
            final ReadableArchive ra,
            final String callerSuppliedMainClassName,
            final String callerSuppliedAppName,
            final ClassLoader classLoader) throws IOException, SAXParseException, UserError {

        ArchivistFactory af = Util.getArchivistFactory();

        Archivist archivist = af.getArchivist(ra, classLoader);

        if (archivist.getModuleType().equals(XModuleType.CAR)) {
            return new UndeployedLaunchable(ra, (AppClientArchivist) archivist, callerSuppliedMainClassName);
        } else if (archivist.getModuleType().equals(XModuleType.EAR)) {
            /*
             * Locate the app client submodule that matches the main class name
             * or the app client name.
             */

            Application app = (Application) archivist.open(ra);
            for (ModuleDescriptor<BundleDescriptor> md : app.getModules()) {
                if ( ! md.getModuleType().equals(XModuleType.CAR)) {
                    continue;
                }

                ApplicationClientDescriptor acd = (ApplicationClientDescriptor) md.getDescriptor();

                final String displayName = acd.getDisplayName();
                final String appName = acd.getModuleID();

                ArchiveFactory archiveFactory = Util.getArchiveFactory();
                ReadableArchive clientRA = archiveFactory.openArchive(ra.getURI().resolve(md.getArchiveUri()));

                /*
                 * Choose this nested app client if the caller-supplied name
                 * matches, or if the caller-supplied main class matches, or
                 * if neither was provided.  
                 */
                final boolean useThisClient =
                        (displayName != null && displayName.equals(callerSuppliedAppName))
                     || (appName != null && appName.equals(callerSuppliedAppName))
                     || (clientRA.exists(classToResource(callerSuppliedMainClassName))
                     || (callerSuppliedAppName == null && callerSuppliedMainClassName == null));

                if (useThisClient) {
                    return new UndeployedLaunchable(clientRA, acd, callerSuppliedMainClassName);
                }
                clientRA.close();
            }

            throw new UserError(localStrings.get("appclient.noMatchingClientInEAR",
                    ra.getURI(), callerSuppliedMainClassName, callerSuppliedAppName));
        } else {
            throw new UserError(
                    localStrings.get("appclient.unexpectedArchive", ra.getURI()));
        }
    }

    private static String classToResource(final String className) {
        return className.replace('.', '/') + ".class";
    }

    private UndeployedLaunchable(final ReadableArchive clientRA,
            final ApplicationClientDescriptor acd,
            final String callerSuppliedMainClass) {
        this.callerSuppliedMainClassName = callerSuppliedMainClass;
        this.clientRA = clientRA;
        this.acDesc = acd;
    }

    private UndeployedLaunchable(final ReadableArchive clientRA,
            final AppClientArchivist archivist,
            final String callerSuppliedMainClass) throws IOException, SAXParseException {
        this.clientRA = clientRA;
        this.archivist = archivist;
        this.callerSuppliedMainClassName = callerSuppliedMainClass;
    }

    public Class getMainClass() throws ClassNotFoundException {
        try {
            String mainClassName = mainClassNameToLaunch();
            return Class.forName(mainClassName, true, getClassLoader());
        } catch (Exception e) {
            throw new ClassNotFoundException("<mainclass>");
        }
    }

    private ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    private String mainClassNameToLaunch() throws IOException, SAXParseException {
        return (callerSuppliedMainClassName != null ? callerSuppliedMainClassName :
            getDescriptor(getClassLoader()).getMainClassName());
    }

    public ApplicationClientDescriptor getDescriptor(ClassLoader loader) throws IOException, SAXParseException {
        this.classLoader = loader;
        if (acDesc == null) {
            acDesc = getArchivist(loader).open(clientRA);
        }
        return acDesc;
    }

    private AppClientArchivist getArchivist(final ClassLoader classLoader) throws IOException {
        if (archivist == null) {
            ArchivistFactory af = Util.getArchivistFactory();
            archivist = (AppClientArchivist) af.getArchivist(clientRA, classLoader);
        }
        return archivist;
    }

    public void validateDescriptor() {
        try {
            getArchivist(classLoader).validate(classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
