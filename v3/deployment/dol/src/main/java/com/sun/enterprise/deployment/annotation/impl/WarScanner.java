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
package com.sun.enterprise.deployment.annotation.impl;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebFragmentDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.AppListenerDescriptor;
import com.sun.enterprise.deployment.web.ServletFilter;
import org.glassfish.apf.impl.AnnotationUtils;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;


/**
 * Implementation of the Scanner interface for war.
 *
 * @author Shing Wai Chan
 */
@Service(name="war")
@Scoped(PerLookup.class)
public class WarScanner extends ModuleScanner<WebBundleDescriptor> {


    /**
     * This scanner will scan the archiveFile for annotation processing.
     * @param archiveFile
     * @param webBundleDesc
     * @param classLoader
     */
    public void process(File archiveFile, WebBundleDescriptor webBundleDesc,
            ClassLoader classLoader) throws IOException {
        if (AnnotationUtils.getLogger().isLoggable(Level.FINE)) {
            AnnotationUtils.getLogger().fine("archiveFile is " + archiveFile);
            AnnotationUtils.getLogger().fine("webBundle is " + webBundleDesc);
            AnnotationUtils.getLogger().fine("classLoader is " + classLoader);
        }
        this.archiveFile = archiveFile;
        this.classLoader = classLoader;

        if (!archiveFile.isDirectory()) {
            // on client side
            return;
        }

        File webinf = new File(archiveFile, "WEB-INF");
        
        if (webBundleDesc instanceof WebFragmentDescriptor) {
            WebFragmentDescriptor webFragmentDesc = (WebFragmentDescriptor)webBundleDesc;
            File lib = new File(webinf, "lib");
            if (lib.exists()) {
                File jarFile = new File(lib, webFragmentDesc.getJarName());
                if (jarFile.exists() && (!jarFile.isDirectory())) {
                    addScanJar(jarFile);
                }
            }
        } else {
            File classes = new File(webinf, "classes");
            if (classes.exists()) {
                addScanDirectory(classes);   
            }
        }
    }
}
 
