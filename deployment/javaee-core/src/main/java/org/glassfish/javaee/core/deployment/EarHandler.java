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

package org.glassfish.javaee.core.deployment;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 16, 2009
 * Time: 3:33:40 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="ear")
public class EarHandler extends AbstractArchiveHandler implements ArchiveHandler {

    final private Logger logger = LogDomains.getLogger(EarHandler.class, LogDomains.DPL_LOGGER);
    
    @Inject
    Deployment deployment;
    
    public String getArchiveType() {
        return "ear";
    }

    public boolean handles(ReadableArchive archive) throws IOException {
        return DeploymentUtils.isEAR(archive);
    }

    @Override
    public void expand(ReadableArchive source, WritableArchive target) throws IOException {

        Enumeration<String> e = source.entries();
        while (e.hasMoreElements()) {
            String entryName = e.nextElement();
            if (!entryName.endsWith("xml")) {
                ReadableArchive subArchive = source.getSubArchive(entryName);
                try {
                    ArchiveHandler subHandler = deployment.getArchiveHandler(subArchive);
                    if (subHandler!=null) {
                        WritableArchive subTarget = target.createSubArchive(entryName);
                        subHandler.expand(subArchive, subTarget);
                        target.closeEntry(subTarget);
                        continue;
                    }
                } catch(IOException ioe) {
                    logger.log(Level.FINE, "Exception while processing " + entryName, ioe);

                }
            }
            // normal file, just copy.
            InputStream is = new BufferedInputStream(source.getEntry(entryName));
            OutputStream os = null;
            try {
                os = target.putNextEntry(entryName);
                FileUtils.copy(is, os, source.getEntrySize(entryName));
            } finally {
                if (os!=null) {
                    target.closeEntry();
                }
                is.close();
            }
        }

        // last is manifest is existing.
        Manifest m = source.getManifest();
        if (m!=null) {
            OutputStream os  = target.putNextEntry(JarFile.MANIFEST_NAME);
            m.write(os);
            target.closeEntry();
        }
    }

    public ClassLoader getClassLoader(ClassLoader parent, ReadableArchive archive) {
        EarClassLoader cl = new EarClassLoader(new URL[0], null);
        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement();
            if (archive.isDirectory(entryName) || !entryName.endsWith("xml")) {
                ReadableArchive sub = null;
                try {
                    sub = archive.getSubArchive(entryName);
                } catch (IOException e) {
                    logger.log(Level.FINE, "Sub archive " + entryName + " seems unreadable" ,e);
                }
                if (sub!=null) {
                    try {
                        ArchiveHandler handler = deployment.getArchiveHandler(sub);
                        if (handler!=null) {
                            // todo : this is a hack, once again, the handklet is assuming a file:// url
                            
                            ClassLoader subCl = handler.getClassLoader(cl, sub);
                            cl.addModuleClassLoader(subCl);
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Cannot find a class loader for submodule", e);
                    }

                }
            }
        }
        return cl;
    }
}
