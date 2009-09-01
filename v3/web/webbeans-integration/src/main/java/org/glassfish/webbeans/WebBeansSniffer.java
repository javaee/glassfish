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


import com.sun.enterprise.deployment.deploy.shared.Util;

import org.glassfish.internal.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Enumeration;


/**
 * Implementation of the Sniffer for WebBeans.
 *
 */
@Service(name="webbeans")
@Scoped(Singleton.class)
public class WebBeansSniffer extends GenericSniffer implements Sniffer {

    private static final String[] containers = { "org.glassfish.webbeans.WebBeansContainer" };

    public WebBeansSniffer() {
        // We do not haGenericSniffer(String containerName, String appStigma, String urlPattern
        super("webbeans", null /* appStigma */, null /* urlPattern */);
    }

    private static char SEPERATOR_CHAR = '/';
    private static final String WEB_INF = "WEB-INF";
    private static final String WEB_INF_LIB = WEB_INF + SEPERATOR_CHAR + "lib";
    private static final String WEB_INF_CLASSSES = WEB_INF + SEPERATOR_CHAR + "classes";
    private static final String WEB_INF_BEANS_XML = "WEB-INF" + SEPERATOR_CHAR + "beans.xml";
    private static final String META_INF_BEANS_XML = "META-INF" + SEPERATOR_CHAR + "beans.xml";
    private static final String JAR_SUFFIX = ".jar";
    private static final String EXPANDED_JAR_SUFFIX = "_jar";

    /**
     * Returns true if the archive contains beans.xml as defined by packaging rules of WebBeans 
     * TODO : Enhance this to handle ears
     */
    @Override
    public boolean handles(ReadableArchive archive, ClassLoader loader) {
        boolean isWebBeansArchive = false;

        // scan for beans.xml in expected locations. If at least one is found, this is
        // a Web Beans archive
        //
        // War check.  Perform the following checks:  
        //     - Check for beans.xml under WEB-INF
        //     - Check jar files under WEB-INF/lib for beans.xml under META-INF 
        //
        if (DeploymentUtils.isWebArchive(archive)) {
            isWebBeansArchive = isEntryPresent(archive, WEB_INF_BEANS_XML);

            if (!isWebBeansArchive) {

                // Check jars under WEB_INF/lib

                if (isEntryPresent(archive, WEB_INF_LIB)) {
                    Enumeration<String> entries = archive.entries(WEB_INF_LIB);
                    while (entries.hasMoreElements() && !isWebBeansArchive) {
                        String entryName = entries.nextElement();
                        // if the jar is not under a subdirectory under WEB-INF/lib
                        if (entryName.endsWith(JAR_SUFFIX) && 
                            entryName.indexOf(SEPERATOR_CHAR, WEB_INF_LIB.length() + 1 ) == -1 ) { 
                            try {
                                ReadableArchive jarInLib = archive.getSubArchive(entryName);
                                isWebBeansArchive = isEntryPresent(jarInLib, META_INF_BEANS_XML);
                                jarInLib.close();
                            } catch (IOException e) {
                                // Something went wrong while reading the jar. Do not attempt to scan it
                            } 
                        } 
                    } 
                } 
            } 
        } 

        if (!isWebBeansArchive && archive.getName().endsWith(EXPANDED_JAR_SUFFIX)) {
            isWebBeansArchive = isEntryPresent(archive, META_INF_BEANS_XML);
        }

        return isWebBeansArchive;
    }

    private boolean isEntryPresent(ReadableArchive archive, String entry) {
        boolean entryPresent = false;
        try {
            entryPresent = archive.exists(entry);
        } catch (IOException e) {
            // ignore
        }
        return entryPresent;
    }

    public String[] getContainersNames() {
        return containers;
    }
}

