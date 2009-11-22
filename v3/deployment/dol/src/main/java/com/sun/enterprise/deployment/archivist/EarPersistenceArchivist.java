/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.Archive;
import org.xml.sax.SAXParseException;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Service
public class EarPersistenceArchivist extends PersistenceArchivist {

    @Override
    public boolean supportsModuleType(XModuleType moduleType) {
        return XModuleType.EAR==moduleType;
    }


    /**
     * Reads persistence.xml from spec defined pu roots of an ear.
     * Spec defined pu roots are - (1)Non component jars in root of ear (2)jars in lib of ear
     */
    @Override
    public Object open(Archivist main, ReadableArchive earArchive, final RootDeploymentDescriptor descriptor) throws IOException, SAXParseException {

        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "EarArchivist",
                    "readPersistenceDeploymentDescriptors", "archive = {0}",
                    earArchive.getURI());
        }


        Map<String, ReadableArchive> probablePersitenceArchives = new HashMap<String,  ReadableArchive>();
        try {
            final Application app = Application.class.cast(descriptor);

            // TO DO: need to compute includeRoot, not hard-code it, in the next invocation.
            EARBasedPersistenceHelper.addLibraryAndTopLevelCandidates(earArchive, app, true /* includeRoot */,
                    probablePersitenceArchives);

            for(Map.Entry<String, ReadableArchive> pathToArchiveEntry : probablePersitenceArchives.entrySet()) {
                readPersistenceDeploymentDescriptor(main, pathToArchiveEntry.getValue(), pathToArchiveEntry.getKey(), descriptor);
            }
        } finally {
            for(Archive subArchive : probablePersitenceArchives.values()) {
                subArchive.close();
            }
        }
        return null;
    }

}
