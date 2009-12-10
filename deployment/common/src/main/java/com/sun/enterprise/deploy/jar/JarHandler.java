/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.deploy.jar;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentUtils;
import java.net.MalformedURLException;

import com.sun.enterprise.loader.ASURLClassLoader;
import com.sun.enterprise.deploy.shared.AbstractArchiveHandler;

import java.net.URL;
import java.lang.RuntimeException;

/**
 * ArchiveHandler implementation for jar files
 *
 * @author Jerome Dochez
 */
@Service(name="DEFAULT")
public class JarHandler extends AbstractArchiveHandler implements ArchiveHandler {
    public String getArchiveType() {
        return "jar";
    }

    public boolean handles(ReadableArchive archive) {
        if (DeploymentUtils.isEAR(archive)) {
            // I should not handle ear, so ear support must not be available
            // in this distribution
            throw new RuntimeException(
                "no container associated with application of type : ear");
        }
        // but I handle everything that looks like a jar...   
        return true;
    }

    public ClassLoader getClassLoader(ClassLoader parent, DeploymentContext context) {
        ASURLClassLoader cloader = new ASURLClassLoader(parent);
        try {              
            cloader.addURL(context.getSource().getURI().toURL());
            cloader.addURL(context.getScratchDir("ejb").toURI().toURL());
            if (context.getArchiveHandler().getClass(
                ).getAnnotation(Service.class).name().equals("ear")) {
                // add libarries referenced from manifest 
                for (URL url : getManifestLibraries(context)) {
                    cloader.addURL(url);
                }
            }
        } catch(MalformedURLException e) {
            return null;
        }
        return cloader;
    }
}
