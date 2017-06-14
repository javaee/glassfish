/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package override;

import java.io.*;
import java.net.*;
import java.util.Set;
import com.sun.enterprise.deployment.archivist.*;
import com.sun.enterprise.deployment.*;
import org.glassfish.deployment.common.Descriptor;

import org.glassfish.api.deployment.archive.ReadableArchive;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.internal.api.Globals;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

public class OverrideTest {

    private static ServiceLocator serviceLocator = null;

    private static final String EXPECTED_RESOURCE_JNDI_NAME = "jdbc/__default";
    private static final String EXPECTED_RESOURCE_DESCRIPTION = "override";
    private static final String EXPECTED_RESOURCE_SHARING_SCOPE = "Unshareable";
    private static final String EXPECTED_RESOURCE_AUTHORIZATION = "Container";

    public static void main(String args[]) {

        String fileName = args[0];
        String ext = getExtension(fileName);
        String archiveType = ext.substring(1);
        if ("jar".equals(archiveType)) {
          if (fileName.contains("car")) {
            archiveType = "car";
          } else {
            archiveType = "ejb";
          }
        }

        boolean expectException = false;
        if (args.length > 1) {
          if ("true".equals(args[1])) {
            expectException = true;
          }
        }

        String outputFileName = fileName + "1" + ext;
        String outputFileName2 = fileName + "2" + ext;

        prepareServiceLocator();

        ArchivistFactory archivistFactory = serviceLocator.getService(ArchivistFactory.class);
        ArchiveFactory archiveFactory = serviceLocator.getService(ArchiveFactory.class);
        Archivist archivist = null;

        ReadableArchive archive = null;
        
        // first read/parse and write out the original valid archive
        try {
            File archiveFile = new File(fileName);
            archive = archiveFactory.openArchive(
                archiveFile);
            ClassLoader classloader = new URLClassLoader(
                new URL[] { new File(archiveFile, "WEB-INF/classes").toURL() });

            archivist = archivistFactory.getArchivist(archiveType, classloader);
            archivist.setAnnotationProcessingRequested(true);
            JndiNameEnvironment nameEnv = (JndiNameEnvironment)archivist.open(archiveFile);

            Set<ResourceReferenceDescriptor> resRefDescs = nameEnv.getResourceReferenceDescriptors(); 
 
            for (ResourceReferenceDescriptor resRef : resRefDescs) {
                String refName = resRef.getName();
                String jndiName = resRef.getJndiName();
                String mappedName = resRef.getMappedName();
                String lookupName = resRef.getLookupName();
                String description = resRef.getDescription();
                String auth = resRef.getAuthorization();
                String scope = resRef.getSharingScope();
                log ("Resource ref [" + refName + "] with JNDI name: " + jndiName + ", description: " + description + ", authorization: " + auth + ", sharing scope: " + scope + ", mappedName: " + mappedName + ", lookupName: " + lookupName);
                if (refName.equals("myDS7") && 
                    !description.equals(EXPECTED_RESOURCE_DESCRIPTION)) {
                    log("Descriptor did not override the @Resource description attribute as expected");
                    fail();
                } else if (refName.equals("myDS5and6")) { 
                    Set<InjectionTarget> targets = resRef.getInjectionTargets();
                    for (InjectionTarget target : targets) {
                       log("Target class name: " + target.getClassName());
                       log("Target name: " + target.getTargetName());
                    }
                    if (targets.size() != 2) {
                        log("The additional injection target specified in the descriptor is not used as expected");
                        fail();
                    }
                } else if (refName.equals("myDS8") && 
                    !mappedName.equals(EXPECTED_RESOURCE_JNDI_NAME)) {
                    log("Descriptor did not override the @Resource mapped-name attribute as expected");
                    fail();
                } else if (refName.equals("myDS7") && 
                    !scope.equals(EXPECTED_RESOURCE_SHARING_SCOPE)) {
                    log("Descriptor did not override the @Resource sharing scope attribute as expected");
                    fail();
                } else if (refName.equals("myDS7") && 
                    !auth.equals(EXPECTED_RESOURCE_AUTHORIZATION)) {
                    log("Descriptor did not override the @Resource authorization attribute as expected");
                    fail();
                } else if (refName.equals("myDS7") && 
                    !lookupName.equals(EXPECTED_RESOURCE_JNDI_NAME)) {
                    log("Descriptor did not override the @Resource lookup name attribute as expected");
                    fail();
                }
            }
  
        } catch (Exception e) {
            e.printStackTrace();
            log("Input archive: [" + fileName + 
                "] is not valid");
            fail();
        } finally {
            try {
                if (archive != null) {
                    archive.close();
                }
            } catch(IOException ioe) {
            }
        }
    }

    private static void log(String message) {
        System.out.println("[dol.override.OverrideTest]:: " + message);
        System.out.flush();
    }

    private static void pass() {
        log("PASSED: devtests/deployment/dol/override");
        System.exit(0);
    }

    private static void fail() {
        log("FAILED: devtests/deployment/dol/override");
        System.exit(-1);
    }

    private static String getExtension(String file) {
        String ext = file.substring(file.lastIndexOf("."));
        return ext;
    }

    private static void prepareServiceLocator() {
        if ( (serviceLocator == null) ) {
            // Bootstrap a hk2 environment.
            ModulesRegistry registry = new StaticModulesRegistry(Thread.currentThread().getContextClassLoader());
            serviceLocator = registry.createServiceLocator("default");
            StartupContext startupContext = new StartupContext();

            ServiceLocatorUtilities.addOneConstant(serviceLocator, startupContext);
            ServiceLocatorUtilities.addOneConstant(serviceLocator, 
                new ProcessEnvironment(ProcessEnvironment.ProcessType.Other));

            Globals.setDefaultHabitat(serviceLocator);
        }
    }

}
