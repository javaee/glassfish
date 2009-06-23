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

/*
 * VerifierUtils.java
 *
 * Created on September 9, 2002, 12:08 PM
 */

package com.sun.enterprise.tools.verifier;

import javax.enterprise.deploy.shared.ModuleType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import com.sun.enterprise.deployment.deploy.shared.AbstractArchive;
import com.sun.enterprise.deployment.deploy.shared.JarArchiveFactory;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.util.shared.ArchivistUtils;

/**
 * @author dochez
 */
public class VerifierUtils {

    public static void copyArchiveToDir(File source, File dest)
            throws IOException {
        AbstractArchive in = null;
        try {
            in =
                    (new JarArchiveFactory()).openArchive(
                            source.getAbsolutePath());
            copyArchiveToDir(in, dest);
        } finally {
            if (in != null)
                in.close();
        }
    }

    public static void copyArchiveToDir(AbstractArchive source, File dest)
            throws IOException {
        for (Enumeration elements = source.entries();
             elements.hasMoreElements();) {
            String elementName = (String) elements.nextElement();
            InputStream is = source.getEntry(elementName);
            OutputStream fos = null;
            try {
                if (elementName.indexOf('/') != -1) {
                    String directory = elementName.substring(0,
                            elementName.lastIndexOf('/'));
                    File dir = new File(dest, directory);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                }
                File elementFile = new File(dest, elementName);
                fos = new BufferedOutputStream(
                        new FileOutputStream(elementFile));
                ArchivistUtils.copy(is, fos);
            } finally {
                try {
                    if(is != null)
                        is.close();
                    if(fos != null)
                        fos.close();
                } catch (Exception e) {}
            }
        }
    }

}
