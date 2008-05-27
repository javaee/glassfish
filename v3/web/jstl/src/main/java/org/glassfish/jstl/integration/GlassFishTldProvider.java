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

package org.glassfish.jstl.integration;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URI;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.glassfish.api.web.TldProvider;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import com.sun.enterprise.util.web.JarURLPattern;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;

/**
 * Implementation of TldProvider for JSF.
 * @author Shing Wai Chan
 * @author Sahoo
 */

@Service(name="jstlTld")
@Scoped(Singleton.class)
public class GlassFishTldProvider implements TldProvider, PostConstruct {
    @Inject
    ModulesRegistry registry;

    private Map<URL, List<String>> tldMap = new HashMap<URL, List<String>>();

    /**
     * Get a Map with key URL and value as a list of tld entries.
     */
    public Map<URL, List<String>> getTldMap() {
        return (Map<URL, List<String>>)((HashMap)tldMap).clone();
    }
 
    public void postConstruct() {
        URL[] urls = null;
        Module m = registry.find(getClass());
        if (m!=null) {
            URI[] uris = m.getModuleDefinition().getLocations();
            List<URL> urlList = new ArrayList<URL>(uris.length);
            for (int i = 0; i< uris.length; ++i) {
                try {
                    urlList.add(uris[i].toURL());
                } catch (MalformedURLException e) {
                    // TODO(Sahoo): Use logger
                    System.out.println("Ignoring " + uris[i] + " because of " + e);
                }
            }
            urls = urlList.toArray(new URL[urlList.size()]);
        } else {
            ClassLoader classLoader = getClass().getClassLoader();
            if (classLoader instanceof URLClassLoader) {
                urls = ((URLClassLoader)classLoader).getURLs();
            } else {
                // TODO(Sahoo): Use logger
                System.out.println("ClassLoader [" + classLoader +
                        "] is not of type URLClassLoader");
            }
        }

        if (urls != null) {
            Pattern pattern = Pattern.compile("META-INF/.*\\.tld");
            for (URL url : urls) {
                List entries =  JarURLPattern.getJarEntries(url, pattern);
                if (entries != null && entries.size() > 0) {
                    tldMap.put(url, entries);
                }
            }
        }
    }
}
