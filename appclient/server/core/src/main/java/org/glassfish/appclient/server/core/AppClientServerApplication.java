/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.deployment.Application;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import org.glassfish.api.deployment.DeployCommandParameters;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.logging.LogDomains;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.appclient.server.core.jws.JavaWebStartInfo;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

/**
 * Represents an app client module, either stand-alone or nested inside
 * an EAR, loaded on the server.
 * <p>
 * The primary purpose of this class is to implement Java Web Start support for
 * launches of this app client.  Other than in that sense, app clients do not
 * run in the server.  To support a client for Java Web Start launches, this
 * class figures out what static content (JAR files) and dynamic content (JNLP
 * documents) are needed by the client.  It then generates the required
 * dynamic content templates and submits them and the static content to a
 * Grizzly adapter which actually serves the data in response to requests.
 *
 * @author tjquinn
 */
@Service
@Scoped(PerLookup.class)
public class AppClientServerApplication implements 
        ApplicationContainer<ApplicationClientDescriptor> {

    @Inject
    private Habitat habitat;

    private DeploymentContext dc;

    private Logger logger;
    
    private AppClientDeployerHelper helper;

    private ApplicationClientDescriptor acDesc;
    private Application appDesc;
    
    private String deployedAppName;

    private JavaWebStartInfo jwsInfo = null;
    
    public void init (final DeploymentContext dc,
            final AppClientDeployerHelper helper) {
        this.dc = dc;
        this.helper = helper;
        this.logger = LogDomains.getLogger(AppClientServerApplication.class,
                LogDomains.ACC_LOGGER);

        acDesc = helper.appClientDesc();

        appDesc = acDesc.getApplication();

        deployedAppName = dc.getCommandParameters(DeployCommandParameters.class).name();
        
    }

    public String deployedAppName() {
        return deployedAppName;
    }

    public ApplicationClientDescriptor getDescriptor() {
        return acDesc;
    }

    public AppClientDeployerHelper helper() {
        return helper;
    }
    
    public boolean start(ApplicationContext startupContext) throws Exception {
        return start();
    }


    boolean start() {
        if (jwsInfo == null) {
            jwsInfo = newJavaWebStartInfo();
        }
        jwsInfo.start();

        return true;
    }

    private JavaWebStartInfo newJavaWebStartInfo() {
        final JavaWebStartInfo jwsInfo = habitat.getComponent(JavaWebStartInfo.class);
        jwsInfo.init(this);
        return jwsInfo;
    }

    public boolean stop(ApplicationContext stopContext) {
        return stop();
    }

    boolean stop() {
        if (jwsInfo != null) {
            jwsInfo.stop();
        }
        
        return true;
    }

    public boolean suspend() {
        if (jwsInfo != null) {
            jwsInfo.suspend();
        }
        return true;
    }

    public boolean resume() throws Exception {
        if (jwsInfo != null) {
            jwsInfo.resume();
        }
        return true;
    }

    public ClassLoader getClassLoader() {
        /*
         * This cannot be null or it prevents the framework from invoking unload
         * on the deployer for this app.
         */
        return new URLClassLoader(new URL[0]);
    }

    public DeploymentContext dc() {
        return dc;
    }

    public String registrationName() {
        return appDesc.getRegistrationName();
    }
        
    public String moduleExpression() {
        String moduleExpression;
        if (appDesc.isVirtual()) {
            moduleExpression = appDesc.getRegistrationName();
        } else {
            moduleExpression = appDesc.getRegistrationName() + "/" + acDesc.getModuleName();
        }
        return moduleExpression;
    }

}
