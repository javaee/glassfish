/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 */

package org.glassfish.extras.osgicontainer;

import org.glassfish.internal.deployment.GenericDeployer;
import org.glassfish.internal.deployment.GenericApplicationContainer;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PreDestroy;
import com.sun.enterprise.module.*;

import java.util.Collection;

/**
 * OSGi deployer, takes care of loading and cleaning modules from the OSGi runtime.
 *
 * @author Jerome Dochez
 */
@Service
public class OSGiDeployer implements Deployer<OSGiContainer, OSGiDeployedBundle> {

    @Inject
    ModulesRegistry registry;

    @Inject
    OSGiArchiveHandler archiveHandler;

    public OSGiDeployedBundle load(OSGiContainer container, DeploymentContext context) {

        Collection<Module> modules = registry.getModules(context.getAppProps().getProperty("module-name"));
        final Module module = (modules.size()>0?modules.iterator().next():null);
        if (module==null) {
            throw new RuntimeException("Cannot install OSGi bundle in repository, is this an OSGi bundle ?"); 
        }
        // I am ensure I have a RefCountingClassLoader...
        final RefCountingClassLoader loader = archiveHandler.getClassLoader(null, module);
        // and release the one I got from the context.
        if (context.getFinalClassLoader() instanceof PreDestroy) {
            ((PreDestroy) context.getFinalClassLoader()).preDestroy();
        }

        return new OSGiDeployedBundle(module, loader);
    }

    public void unload(OSGiDeployedBundle appContainer, DeploymentContext context) {
        appContainer.cl.preDestroy();
        context.addModuleMetaData(appContainer.m);
    }



    public void clean(DeploymentContext context) {
        OpsParams params = context.getCommandParameters(OpsParams.class);
        if (params!=null) {
            if (params.origin== OpsParams.Origin.undeploy) {
                Module m = context.getModuleMetaData(Module.class);
                if (m!=null && m.getState()!= ModuleState.NEW) {
                    m.uninstall();
                }
            }
        }

    }

    public MetaData getMetaData() {
        return null;
    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    public boolean prepare(DeploymentContext context) {
        return true; 
    }
}
