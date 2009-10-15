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

package org.glassfish.weld.jsf;

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
import org.glassfish.weld.WeldDeployer;
import org.jvnet.hk2.component.Habitat;

/**
 * This provider returns the Web Beans faces-config.xml to the JSF runtime.
 * It will only return the configuraion file for Web Beans deployments.
 */  
public class WeldFacesConfigProvider implements FacesConfigResourceProvider {

    private static final String HABITAT_ATTRIBUTE =
            "org.glassfish.servlet.habitat";
    private InvocationManager invokeMgr;

    private static final String SERVICES_FACES_CONFIG = "META-INF/services/faces-config.xml";

    public Collection<URL> getResources(ServletContext context) {

        Habitat defaultHabitat = (Habitat)context.getAttribute(
                HABITAT_ATTRIBUTE);
        invokeMgr = defaultHabitat.getByContract(InvocationManager.class);
        ComponentInvocation inv = invokeMgr.getCurrentInvocation();
        WebModule webModule = (WebModule)inv.getContainer();
        WebBundleDescriptor wdesc = webModule.getWebBundleDescriptor();

        List<URL> list = new ArrayList<URL>(1);

        if (!wdesc.hasExtensionProperty(WeldDeployer.WELD_EXTENSION)) {
            return list;
        }

        // Don't use Util.getCurrentLoader().  This config resource should
        // be available from the same classloader that loaded this instance.
        // Doing so allows us to be more OSGi friendly.
        ClassLoader loader = this.getClass().getClassLoader();
        list.add(loader.getResource(SERVICES_FACES_CONFIG));
        return list;
    }

}
