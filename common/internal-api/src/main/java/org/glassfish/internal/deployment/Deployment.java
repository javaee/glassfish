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
package org.glassfish.internal.deployment;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.ActionReport;
import org.glassfish.api.event.EventTypes;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.data.ProgressTracker;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.TransactionFailure;

import java.io.IOException;
import java.io.File;
import java.util.LinkedList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Deployment facility
 *
 * @author Jerome Dochez
 */
@Contract
public interface Deployment {


    public final EventTypes<ExtendedDeploymentContext> DEPLOYMENT_START = EventTypes.create("Deployment_Start", ExtendedDeploymentContext.class);
    public final EventTypes<ExtendedDeploymentContext> DEPLOYMENT_FAIL = EventTypes.create("Deployment_Failed", ExtendedDeploymentContext.class);
    public final EventTypes<ApplicationInfo> DEPLOYMENT_SUCCESS = EventTypes.create("Deployment_Success", ApplicationInfo.class);

    public ArchiveHandler getArchiveHandler(ReadableArchive archive) throws IOException;

    public ExtendedDeploymentContext getContext(Logger logger, File source, OpsParams params)
            throws IOException;

    public ExtendedDeploymentContext getContext(Logger logger, ReadableArchive source, OpsParams params)
            throws IOException;


    public ModuleInfo prepareModule(
        LinkedList<EngineInfo> sortedEngineInfos, String moduleName,
        DeploymentContext context, ActionReport report,
        ProgressTracker tracker) throws Exception;

    public ApplicationInfo deploy(final ExtendedDeploymentContext context, final ActionReport report);
    public ApplicationInfo deploy(final Collection<Sniffer> sniffers, final ExtendedDeploymentContext context, final ActionReport report);

    public void undeploy(String appName, ExtendedDeploymentContext context, ActionReport report);

    public void registerAppInDomainXML(final ApplicationInfo
        applicationInfo, final DeploymentContext context)
        throws TransactionFailure;

    public void unregisterAppFromDomainXML(final String appName)
        throws TransactionFailure;
    

    public LinkedList<EngineInfo> setupContainerInfos(
            DeploymentContext context,
            ActionReport report) throws Exception;

    public boolean isRegistered(String appName);

    public ApplicationInfo get(String appName);


}
