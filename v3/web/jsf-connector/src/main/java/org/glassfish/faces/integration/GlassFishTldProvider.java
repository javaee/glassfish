/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.faces.integration;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.api.web.TldProvider;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

/**
 * Implementation of TldProvider for JSF.
 * @author Shing Wai Chan
 */

@Service(name="jsfTld")
@Scoped(Singleton.class)
public class GlassFishTldProvider implements TldProvider, PostConstruct {
    private List<URL> tldList = new ArrayList();
 
    /**
     * Get a list of URL that corresponding to Tld entries
     */
    public URL[] getTldURLs() {
        return tldList.toArray(new URL[tldList.size()]);
    }

    public void postConstruct() {
        List<URL> jsfImplURLs = getJSFImplURLs();
        for (URL jsfImplURL : jsfImplURLs) {
            try {
                JarFile jarFile = new JarFile(new File(jsfImplURL.toURI()));
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry)entries.nextElement();
                    String name = entry.getName();
                    if (!name.startsWith("META-INF/") ||
                            !name.endsWith(".tld")) {
                        continue;
                    } else {
                        tldList.add(new URL("jar:" +
                                jsfImplURL.toString() + "!/" + name));
                    }
                }
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private List<URL> getJSFImplURLs() {
        List<URL> jsfImplURLs = new ArrayList<URL>();
        ClassLoader loader = getClass().getClassLoader();
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader)loader).getURLs();
                if (urls != null && urls.length > 0) {
                    for (URL url : urls) {
                        File file = null;
                        try {
                            file = new File(url.toURI());
                        } catch(Exception ex) {
                            // ignore
                        }
                        if (file == null && file.isDirectory()) {
                            continue;
                        } else {
                            String name = file.getName();
                            if (name != null &&
                                    name.startsWith("jsf-impl") &&
                                    name.endsWith(".jar")) {
                                boolean isModule = false;
                                File parentFile = file.getParentFile();
                                if (parentFile != null) {
                                    isModule = "modules".equals(parentFile.getName());
                                    if (!isModule) {
                                        parentFile = parentFile.getParentFile();
                                        isModule = "modules".equals(parentFile.getName());
                                    }
                                }
                             
                                if (isModule) {
                                    jsfImplURLs.add(url);
                                }
                            }
                        }
                    }
                }
            }

            loader = loader.getParent();
        }

        return jsfImplURLs;
    }
}
