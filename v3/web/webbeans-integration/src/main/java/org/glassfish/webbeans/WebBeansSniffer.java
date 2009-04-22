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

    /**
     * Returns true if the archive contains beans.xml as defined by packaging rules of WebBeans 
     * TODO : Enhance this to handle ears
     */
    @Override
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        boolean isWebBeansArchive = false;

        // scan for beans.xml in expected locations. If at least one is found, this is
        // a Web Beans archive
        //
        // Case 1: Standalone War check.  Perform the following checks:  
        //     - Check for beans.xml under WEB-INF
        //     - Check jar files under WEB-INF/lib for beans.xml under META-INF 
        //
        if (DeploymentUtils.isWebArchive(location)) {
            isWebBeansArchive = isEntryPresent(location, WEB_INF_BEANS_XML);

            if (!isWebBeansArchive) {

                // Check jars under WEB_INF/lib

                if (isEntryPresent(location, WEB_INF_LIB)) {
                    Enumeration<String> entries = location.entries(WEB_INF_LIB);
                    while (entries.hasMoreElements() && !isWebBeansArchive) {
                        String entryName = entries.nextElement();
                        // if the jar is not under a subdirectory under WEB-INF/lib
                        if (entryName.endsWith(JAR_SUFFIX) && 
                            entryName.indexOf(SEPERATOR_CHAR, WEB_INF_LIB.length() + 1 ) == -1 ) { 
                            try {
                                ReadableArchive jarInLib = location.getSubArchive(entryName);
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
        return isWebBeansArchive;
    }

    private boolean isEntryPresent(ReadableArchive location, String entry) {
        boolean entryPresent = false;
        try {
            entryPresent = location.exists(entry);
        } catch (IOException e) {
            // ignore
        }
        return entryPresent;
    }

    public String[] getContainersNames() {
        return containers;
    }
}

