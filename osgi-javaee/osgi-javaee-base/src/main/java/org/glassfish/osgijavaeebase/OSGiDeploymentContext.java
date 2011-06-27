/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.osgijavaeebase;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.osgijavaeebase.OSGiArchiveHandler;
import org.osgi.framework.Bundle;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class OSGiDeploymentContext extends DeploymentContextImpl
{
    private static final Logger logger =
            Logger.getLogger(OSGiDeploymentContext.class.getPackage().getName());

    protected ClassLoader shareableTempClassLoader;
    protected ClassLoader finalClassLoader;
    protected Bundle bundle;
    public OSGiDeploymentContext(ActionReport actionReport,
                                     Logger logger,
                                     ReadableArchive source,
                                     OpsParams params,
                                     ServerEnvironment env,
                                     Bundle bundle) throws Exception
    {
        super(actionReport, logger, source, params, env);
        this.bundle = bundle;
        setupClassLoader();

        // We always this handler instead of going through discovery process
        // which has issues.
        setArchiveHandler(new OSGiArchiveHandler());
    }

    protected abstract void setupClassLoader() throws Exception;

    @Override
    public void createDeploymentClassLoader(ClassLoaderHierarchy clh,
                                   ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException
    {
        // do nothing as we override getClassLoader methods.
    }

    @Override
    public void createApplicationClassLoader(ClassLoaderHierarchy clh,
                                   ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException
    {
        // do nothing as we override getClassLoader methods.
    }


    @Override
    public ClassLoader getClassLoader()
    {
        if (getPhase() != Phase.PREPARE) {
            // we return the final class loader
            return finalClassLoader;
        }
        return shareableTempClassLoader;
    }

    @Override
    public ClassLoader getFinalClassLoader()
    {
        return finalClassLoader;
    }

    @Override
    public synchronized ClassLoader getClassLoader(boolean sharable)
    {
        throw new RuntimeException("Assertion Failure: " +
                "This method should not be called");
    }

}
