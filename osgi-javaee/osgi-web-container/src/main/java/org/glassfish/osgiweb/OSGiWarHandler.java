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


package org.glassfish.osgiweb;

import org.glassfish.internal.deployment.GenericHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.web.loader.WebappClassLoader;
import org.osgi.framework.Bundle;

import java.io.IOException;

import com.sun.enterprise.glassfish.web.WarHandler;

/**
 * An implementation of {@link org.glassfish.api.deployment.archive.ArchiveHandler}
 * specialized for OSGi-ed WAR files. It is not exported as a Service.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiWarHandler extends WarHandler
{
    public String getArchiveType()
    {
        return "OSGiBundle";
    }

    public boolean handles(ReadableArchive archive)
    {
        // We don't want this handler to participate in any automatic
        // discovery, so it returns false.
        return false;
    }

    public WebappClassLoader getClassLoader(ClassLoader parent, DeploymentContext context)
    {
        throw new RuntimeException("Assertion Failure: This method should not be called");
    }

    // Since we don't have a fixed file extension, we override
    // getDefaultApplicationName methods
    @Override
    public String getDefaultApplicationName(ReadableArchive archive)
    {
        String appName = archive.getName();
        int lastDot = appName.lastIndexOf('.');
        if (lastDot != -1) {
            appName = appName.substring(0, lastDot);
        }
        return appName;
    }

    @Override
    public String getDefaultApplicationName(ReadableArchive archive, DeploymentContext context)
    {
        return getDefaultApplicationName(archive);
    }
}
