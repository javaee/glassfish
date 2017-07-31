/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.cb;

import javax.servlet.*;
import javax.xml.registry.*; 
import java.util.ResourceBundle;
import java.io.*;

public final class ContextListener
    implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        String queryURL = URLHelper.getQueryURL();
        String publishURL = URLHelper.getPublishURL();
        String endpoint = URLHelper.getEndpointURL();

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeRegistry");

        String username = 
            registryBundle.getString("registry.username");
        String password = 
            registryBundle.getString("registry.password");
        String keyFile = registryBundle.getString("key.file");

        JAXRPublisher publisher = new JAXRPublisher();
        publisher.makeConnection(queryURL, publishURL);
        String key = publisher.executePublish(username, 
            password, endpoint);
        try {
            FileWriter out = new FileWriter(keyFile);
            out.write(key);
            out.flush();
            out.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }     
    }
      
    public void contextDestroyed(ServletContextEvent event) {
        String keyStr = null;

        String queryURL = URLHelper.getQueryURL();
        String publishURL = URLHelper.getPublishURL();

        ResourceBundle registryBundle =
            ResourceBundle.getBundle("com.sun.cb.CoffeeRegistry");

        String username = 
            registryBundle.getString("registry.username");
        String password = 
            registryBundle.getString("registry.password");
        String keyFile = registryBundle.getString("key.file");

        try {
            FileReader in = new FileReader(keyFile);
            char[] buf = new char[512];
            while (in.read(buf, 0, 512) >= 0) { }
            in.close();
            keyStr = new String(buf).trim();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        JAXRRemover remover = new JAXRRemover();
        remover.makeConnection(queryURL, publishURL);
        javax.xml.registry.infomodel.Key modelKey = null;
        modelKey = remover.createOrgKey(keyStr);
        remover.executeRemove(modelKey, username, password);
    }
}
