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

package dol.validation;

import java.io.*;
import com.sun.enterprise.deployment.archivist.*;
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

public class Validator {

    private static ServiceLocator serviceLocator = null;

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
            archivist = archivistFactory.getArchivist(archiveType);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(fileName);
	    archivist.setXMLValidation(true);
	    archivist.setXMLValidationLevel("full");
            archivist.setRuntimeXMLValidation(true);
            archivist.setRuntimeXMLValidationLevel("full");
            log("Reading/parsing the orginal archive: " + 
                fileName);
            try {
              Descriptor descriptor = archivist.open(archiveFile);
            } catch (Exception ex) {
              if (expectException) {
                log("Expected exception");
              } else {
                throw ex;
              }
            }
            log("Writing out the archive to: " + 
                outputFileName);
            archivist.write(archive, outputFileName);
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

        // Read/parse the resulted archive, it should be valid too
        // then write to another archive
        try {
            File archiveFile = new File(outputFileName);
            archive = archiveFactory.openArchive(
                archiveFile);
            archivist = archivistFactory.getArchivist(archiveType);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            archivist.setRuntimeXMLValidation(true);
            archivist.setRuntimeXMLValidationLevel("full");
            log("Reading/parsing the output archive" + 
                outputFileName);
            Descriptor descriptor = archivist.open(archiveFile);
        } catch (Exception e) {
            e.printStackTrace();
            log("The input archive: [" + outputFileName + 
                "] is not valid");
            fail();
        }
        try {
            log("Writing out the archive to: " +
                outputFileName2);
            archivist.write(archive, outputFileName2);
        } catch (Exception e) {
            e.printStackTrace();
            log("The output archive: [" + outputFileName2 + 
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

        // Read/parse the resulted archive, it should be valid too
        try {

            File archiveFile = new File(outputFileName2);
            archive = archiveFactory.openArchive(
                archiveFile);
            archivist = archivistFactory.getArchivist(archiveType);
            archivist.setHandleRuntimeInfo(true);
            archivist.setArchiveUri(outputFileName2);
            archivist.setXMLValidation(true);
            archivist.setXMLValidationLevel("full");
            archivist.setRuntimeXMLValidation(true);
            archivist.setRuntimeXMLValidationLevel("full");
            log("Reading/parsing the output archive" +
                outputFileName2);
            Descriptor descriptor = archivist.open(archiveFile);
        } catch (Exception e) {
            e.printStackTrace();
            log("The output archive: [" + outputFileName2 +
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
        System.out.println("[dol.validation.Validator]:: " + message);
        System.out.flush();
    }

    private static void pass() {
        log("PASSED: devtests/deployment/dol/validation");
        System.exit(0);
    }

    private static void fail() {
        log("FAILED: devtests/deployment/dol/validation");
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
