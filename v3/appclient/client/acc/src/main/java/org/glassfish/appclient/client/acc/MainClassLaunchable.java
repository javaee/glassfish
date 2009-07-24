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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.util.XModuleType;
import java.io.IOException;
import java.net.URI;
import org.jvnet.hk2.component.Habitat;
import org.xml.sax.SAXParseException;

/**
 * Represents a Launchable main class which the caller specifies by the
 * main class itself, rather than a facade JAR or an original developer-provided
 * JAR file.
 *
 * @author tjquinn
 */
public class MainClassLaunchable implements Launchable {

    private final Class mainClass;
    private ApplicationClientDescriptor acDesc = null;
    private ClassLoader classLoader = null;
    private AppClientArchivist archivist = null;
    private final Habitat habitat;

    MainClassLaunchable(final Habitat habitat, final Class mainClass) {
        super();
        this.mainClass = mainClass;
        this.habitat = habitat;
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
            ArchivistFactory af = Util.getArchivistFactory();
            archivist = (AppClientArchivist) af.getArchivist(XModuleType.CAR);
            acDesc = (ApplicationClientDescriptor) archivist.getDefaultBundleDescriptor();
            Application.createApplication(habitat, null, acDesc.getModuleDescriptor());
            archivist.setDescriptor(acDesc);
            archivist.setClassLoader(classLoader);
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

    public String getAnchorDir() {
        return null;
    }
}
