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
 */

package org.glassfish.webbeans;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.AppListenerDescriptorImpl;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.web.WebComponentInvocation;
import com.sun.enterprise.web.WebModule;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.SimpleDeployer;
import org.jvnet.hk2.annotations.Service;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;

@Service
public class WebBeansDeployer extends SimpleDeployer<WebBeansContainer, WebBeansApplicationContainer> {

    private static final String WEB_BEAN_EXTENSION = "org.glassfish.webbeans";
    private static final String WEB_BEAN_LISTENER = "org.jboss.webbeans.servlet.WebBeansListener"; 

    @Override
    public MetaData getMetaData() {
        return new MetaData(true, null, new Class[] {Application.class});
    }

    protected void generateArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    protected void cleanArtifacts(DeploymentContext dc) throws DeploymentException {

    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    @Override
    public boolean prepare(DeploymentContext context) {
        return true;
    }

    @Override
    public WebBeansApplicationContainer load(WebBeansContainer container, DeploymentContext context) {
        ReadableArchive archive = context.getSource();
        BeanStore applicationBeanStore = new ConcurrentHashMapBeanStore();
        WebBeansBootstrap bootstrap = new WebBeansBootstrap();
        bootstrap.setEnvironment(Environments.SERVLET);
        bootstrap.getServices().add(WebBeanDiscovery.class, new WebBeanDiscoveryImpl(archive) {});
        bootstrap.setApplicationContext(applicationBeanStore);
        bootstrap.initialize();
        bootstrap.boot();

        WebBundleDescriptor wDesc = context.getModuleMetaData(WebBundleDescriptor.class);
        wDesc.setExtensionProperty(WEB_BEAN_EXTENSION, "true");

        // Add the Web Beans Listener if it does not already exist..

        wDesc.addAppListenerDescriptor(new AppListenerDescriptorImpl(WEB_BEAN_LISTENER));

        WebBeansApplicationContainer wbApp = new WebBeansApplicationContainer();
        return wbApp; 
    }

}


