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

import org.glassfish.internal.data.ApplicationInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
*/
public class OSGiApplicationInfo
{
    private ApplicationInfo appInfo;
    private boolean isDirectoryDeployment;
    private Bundle bundle;
    private final ClassLoader classLoader;
    private ServiceReference osgiDeployerRef;

    public OSGiApplicationInfo(ApplicationInfo appInfo, boolean directoryDeployment, Bundle bundle, ClassLoader classLoader) {
        this.appInfo = appInfo;
        isDirectoryDeployment = directoryDeployment;
        this.bundle = bundle;
        this.classLoader = classLoader;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }

    public boolean isDirectoryDeployment() {
        return isDirectoryDeployment;
    }

    public void setDirectoryDeployment(boolean directoryDeployment) {
        isDirectoryDeployment = directoryDeployment;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public ServiceReference getDeployer() {
        return osgiDeployerRef;
    }

    public void setDeployer(ServiceReference osgiDeployerRef) {
        this.osgiDeployerRef = osgiDeployerRef;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
