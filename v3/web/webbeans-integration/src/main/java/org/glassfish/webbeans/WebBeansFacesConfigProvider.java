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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.web.WebModule;
import com.sun.faces.spi.FacesConfigResourceProvider;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.jvnet.hk2.component.Habitat;

/**
 * This provider returns the Web Beans faces-config.xml to the JSF runtime.
 * It will only return the configuraion file for Web Beans deployments.
 */  
public class WebBeansFacesConfigProvider implements FacesConfigResourceProvider {

    private static final String HABITAT_ATTRIBUTE =
            "org.glassfish.servlet.habitat";
    private InvocationManager invokeMgr;

    private static final String META_INF_FACES_CONFIG = "META-INF/faces-config.xml";

    private static final String WEB_BEAN_EXTENSION = "org.glassfish.webbeans";

    public Collection<URL> getResources(ServletContext context) {

        Habitat defaultHabitat = (Habitat)context.getAttribute(
                HABITAT_ATTRIBUTE);
        invokeMgr = defaultHabitat.getByContract(InvocationManager.class);
        ComponentInvocation inv = invokeMgr.getCurrentInvocation();
        WebModule webModule = (WebModule)inv.getContainer();
        WebBundleDescriptor wdesc = webModule.getWebBundleDescriptor();

        List<URL> list = new ArrayList<URL>(1);

        if (!wdesc.hasExtensionProperty(WEB_BEAN_EXTENSION)) {
            return list;
        }

        // Don't use Util.getCurrentLoader().  This config resource should
        // be available from the same classloader that loaded this instance.
        // Doing so allows us to be more OSGi friendly.
        ClassLoader loader = this.getClass().getClassLoader();
        list.add(loader.getResource(META_INF_FACES_CONFIG));
        return list;
    }

}
