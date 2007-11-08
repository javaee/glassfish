/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */


package com.sun.persistence.deployment.impl.reflection;

import com.sun.enterprise.deployment.annotation.*;
import com.sun.enterprise.deployment.annotation.factory.Factory;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.api.deployment.*;
import com.sun.persistence.deployment.impl.LogHelperDeployment;
import com.sun.persistence.deployment.impl.LogicalModelDefaultValueSupplier;
import com.sun.persistence.deployment.impl.RelationalModelDefaultValueSupplier;
import com.sun.persistence.deployment.impl.reflection.annotation.AnnotationHandlerRepository;
import com.sun.persistence.deployment.impl.reflection.annotation.DeploymentUnitContextImpl;
import com.sun.persistence.spi.deployment.Archive;
import com.sun.persistence.utility.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * This class reads the persistence-orm.xml and constructs the descriptor. It
 * takes input as jar file, jar directory, inputstream and annotations.
 *
 * @author Sanjeeb Sahoo
 * @version 1.0
 */
public class DescriptorBuilderImpl implements DescriptorBuilder {

    private final static I18NHelper i18NHelper = I18NHelper.getInstance(
            LogHelperDeployment.class);

    private final static String META_INF = "META-INF"; // NOI18N

    private final static String PERSISTENCE_ORM_XML = "persistence-orm.xml"; // NOI18N

    public DeploymentUnit readXML(final Archive archive) throws IOException,
            DeploymentException {
        LogHelperDeployment.getLogger().info(
                i18NHelper.msg("MSG_ReadingThePersistenceDescriptor")); // NOI18N
        final String xmlFilePath = META_INF + File.separator + PERSISTENCE_ORM_XML;
        InputStream is = archive.getEntry(xmlFilePath);
        PersistenceJarDescriptor pjar;
        if (is != null) {
            try {
                pjar = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<PersistenceJarDescriptor> () {
                        public PersistenceJarDescriptor run () 
                            throws IOException {
                            return new XMLReader().read(
                                archive.getEntry(xmlFilePath));
                        }});
            } catch (PrivilegedActionException e) {
                // e.getException() should be an instance of IOException,
                throw (IOException) e.getException();
            }
        } else {
            pjar = new ObjectFactory().createPersistenceJarDescriptor();
        }
        return new DeploymentUnitImpl(pjar, archive.getClassLoader());
    }

    /**
     * {@inheritDoc}
     */
    public DeploymentUnit readXMLAndAnnotations(
            Archive archive,
            boolean populateDefaults)
            throws IOException, DeploymentException {
        DeploymentUnit du = readXML(archive);
        readAnnotations(archive, du);
        if (populateDefaults) {
            populateLogicalModelDefaultValues(du);
            populateRelationalModelDefaultValues(du);
        }
        return du;
    }

    /**
     * {@inheritDoc}
     */
    public DeploymentUnit readAnnotations(
            final Archive archive,
            DeploymentUnit du)
            throws DeploymentException, IOException {
        LogHelperDeployment.getLogger().info(
                i18NHelper.msg("MSG_ReadingTheAnnotations")); // NOI18N
        AnnotationProcessor ap = Factory.getAnnotationProcessor();
        for (AnnotationHandler ah : AnnotationHandlerRepository.getAnnotationHandlers()) {
            ap.pushAnnotationHandler(ah);
        }
        ProcessingContext ctx = ap.createContext();
        Scanner scanner = new Scanner() {
            public ClassLoader getClassLoader() {
                return archive.getClassLoader();
            }

            public Set<Class> getElements() {
                Set<Class> result = new HashSet<Class>();
                ClassLoader cl = archive.getClassLoader();
                Enumeration<String> entries = null;
                try {
                    entries = archive.getEntries();
                } catch (IOException e) {
                    LogHelperDeployment.getLogger().log(Logger.SEVERE,
                            i18NHelper.msg("EXC_ArchiveGetEntries", archive), // NOI18N
                            e);
                }
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    int idx = entry.lastIndexOf(".class"); // NOI18N
                    if (idx != -1) {
                        String dottedClassName = entry.substring(0, idx)
                                .replace('/', '.');
                        try {
                            result.add(
                                    Class.forName(dottedClassName, false, cl));
                        } catch (ClassNotFoundException e) {
                            LogHelperDeployment.getLogger().log(Logger.SEVERE,
                                    i18NHelper.msg("EXC_ClassLoading", // NOI18N
                                            dottedClassName),
                                    e);
                        }
                    }
                }
                return result;
            }
        };
        ctx.setProcessingInput(scanner);
        if (du == null) {
            new DeploymentUnitImpl(
                    new ObjectFactory().createPersistenceJarDescriptor(),
                    archive.getClassLoader());
        }
        ctx.pushHandler(new DeploymentUnitContextImpl(du));
        try {
            ap.process(ctx);
        } catch (AnnotationProcessorException e) {
            throw new DeploymentException(e);
        }
        return du;
    }

    /**
     * {@inheritDoc}
     */
    public void populateLogicalModelDefaultValues(DeploymentUnit du)
            throws DeploymentException {
        new LogicalModelDefaultValueSupplier(du).populateEntityModelDefaultValues();
    }

    /**
     * {@inheritDoc}
     */
    public void populateRelationalModelDefaultValues(DeploymentUnit du)
            throws DeploymentException {
        new RelationalModelDefaultValueSupplier(du).populateORMDefaultValues();
    }

    /**
     * Get a class loader as per the rules defined in spec.
     *
     * @param f either explodedDir or a jar file
     * @return the ClassLoader for this du.
     * @throws MalformedURLException
     */
    private ClassLoader getClassLoader(File f) throws MalformedURLException {
        return new URLClassLoader(new URL[]{f.toURL()});
    }
}
