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

package org.glassfish.webbeans;

import com.sun.enterprise.deployment.Application;
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

        WebBeansApplicationContainer wbApp = new WebBeansApplicationContainer();
        return wbApp; 
    }

}


