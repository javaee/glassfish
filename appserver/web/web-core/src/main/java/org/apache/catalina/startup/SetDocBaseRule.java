/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.startup;

import org.apache.catalina.Context;
import org.apache.catalina.Deployer;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardHost;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.Rule;
import org.xml.sax.Attributes;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Locale;

/**
 * <p>Rule that modifies the docBase of the host, setting it appropriately,
 * before adding the Context to the parent Host.</p>
 * 
 * @author Remy Maucherat
 */
public class SetDocBaseRule extends Rule {


    // -------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this Rule.
     *
     * @param digester Digester we are associated with
     */
    public SetDocBaseRule(Digester digester) {
        super(digester);
    }


    // -------------------------------------------------- Instance Variables


    // ------------------------------------------------------ Public Methods


    /**
     * Handle the beginning of an XML element.
     *
     * @param attributes The attributes of this element
     *
     * @exception Exception if a processing error occurs
     */
    public void begin(Attributes attributes) throws Exception {

        Context child = (Context) digester.peek(0);
        Deployer parent = (Deployer) digester.peek(1);
        Host host = null;
        if (!(parent instanceof StandardHost)) {
            Method method = parent.getClass().getMethod("getHost",(Class[])null);
            host = (Host) method.invoke(parent,(Object[])null);
        } else {
            host = (Host) parent;
        }
        String appBase = host.getAppBase();

        boolean unpackWARs = true;
        if (host instanceof StandardHost) {
            unpackWARs = ((StandardHost) host).isUnpackWARs();
        }
        if (!unpackWARs 
            && !("true".equals(attributes.getValue("unpackWAR")))) {
            return;
        }
        if ("false".equals(attributes.getValue("unpackWAR"))) {
            return;
        }

        File canonicalAppBase = new File(appBase);
        if (canonicalAppBase.isAbsolute()) {
            canonicalAppBase = canonicalAppBase.getCanonicalFile();
        } else {
            canonicalAppBase = 
                new File(System.getProperty("catalina.base"), appBase)
                .getCanonicalFile();
        }

        String docBase = child.getDocBase();
        if (docBase == null) {
            // Trying to guess the docBase according to the path
            String path = child.getPath();
            if (path == null) {
                return;
            }
            if (path.equals("")) {
                docBase = "ROOT";
            } else {
                if (path.startsWith("/")) {
                    docBase = path.substring(1);
                } else {
                    docBase = path;
                }
            }
        }

        File file = new File(docBase);
        if (!file.isAbsolute()) {
            docBase = (new File(canonicalAppBase, docBase)).getPath();
        } else {
            docBase = file.getCanonicalPath();
        }

        if (docBase.toLowerCase(Locale.ENGLISH).endsWith(".war")) {
            URL war = new URL("jar:" + (new File(docBase)).toURL() + "!/");
            String contextPath = child.getPath();
            if (contextPath.equals("")) {
                contextPath = "ROOT";
            }
            docBase = ExpandWar.expand(host, war, contextPath);
            file = new File(docBase);
            docBase = file.getCanonicalPath();
        } else {
            File docDir = new File(docBase);
            if (!docDir.exists()) {
                File warFile = new File(docBase + ".war");
                if (warFile.exists()) {
                    URL war = new URL("jar:" + warFile.toURL() + "!/");
                    docBase = ExpandWar.expand(host, war, child.getPath());
                    file = new File(docBase);
                    docBase = file.getCanonicalPath();
                }
            }
        }

        if (docBase.startsWith(canonicalAppBase.getPath())) {
            docBase = docBase.substring
                (canonicalAppBase.getPath().length() + 1);
        }
        docBase = docBase.replace(File.separatorChar, '/');

        child.setDocBase(docBase);


    }


}
