/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.persistence.jpa;


import org.glassfish.api.container.CompositeSniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.javaee.core.deployment.ApplicationHolder;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarFile;

import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;


/**
 * Implementation of the Sniffer for JPA.
 *
 * @author Mitesh Meswani
 */
@Service(name="jpaCompositeSniffer")
@Scoped(Singleton.class)
public class JPACompositeSniffer extends JPASniffer implements CompositeSniffer  {

    /**
     * Scans for puroots in non component jars present in root of ear.
\     */
    public boolean handles(DeploymentContext context) {
        List<File> listofJars = getListOfJars(context);
        for (File jar : listofJars) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(jar);
                if (jarFile.getEntry("META-INF/persistence.xml") != null) {
                    return true;
                }
            } catch (IOException ioe) {
                // log warning
            } finally {
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets list of non component jars
\     */
    private List<File> getListOfJars(DeploymentContext context) {
          List<File> topLevelJars = new ArrayList<File>();

          ReadableArchive source = context.getSource();

          // when we get here, the archive should already be expanded
          // and the top level application.xml should already be parsed into
          // Application object
          ApplicationHolder holder = context.getModuleMetaData(ApplicationHolder.class);
          if (holder != null && holder.app != null) {
              File file = new File(source.getURI());
              for (File f : file.listFiles()) {
                  // if it's a top level jar
                  if (!f.isDirectory() && f.getName().endsWith(".jar")) {
                      // let's make sure if it's not one of the component jars
                      if (!isComponentJar(f, holder.app.getModules()) ) {
                          topLevelJars.add(f);
                      }
                  }
              }
          }
          return topLevelJars;
      }

    /**
     * Checks whether given <code>jar</code> file is a component jar within the given set of <code>moduleDescriptors</code>
     * @param jar given jar file
     * @param moduleDescriptors Given set of module descriptors
     * @return true if the file is a component jar false otherwise
     */
    private static boolean isComponentJar(File jar, Set<ModuleDescriptor<BundleDescriptor>> moduleDescriptors) {
        boolean isComponentJar = false;
        for (ModuleDescriptor md : moduleDescriptors) {
            String archiveUri = md.getArchiveUri();
            if (jar.getName().equals(archiveUri)) {
                isComponentJar = true;
                break;
            }
        }
        return isComponentJar;
    }

}