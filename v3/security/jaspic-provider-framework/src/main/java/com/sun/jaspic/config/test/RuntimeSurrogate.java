/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jaspic.config.test;

import com.sun.jaspic.config.factory.GFAuthConfigFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigFactory.RegistrationContext;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import com.sun.jaspic.config.servlet.ServletAuthConfigProvider;

/**
 *
 * @author Ron Monzillo
 */
public class RuntimeSurrogate {

    private static final String CONFIG_FILE_NAME_KEY = "config.file.name";
    HashMap<String, String> providerProperties;
    AuthConfigFactory factory;
    AuthConfigProvider provider;

    public RuntimeSurrogate(HashMap<String, String> properties)  {
        this.providerProperties = properties;
        AuthConfigFactory f = new GFAuthConfigFactory();
        AuthConfigFactory.setFactory(f);
        this.factory = AuthConfigFactory.getFactory();
        this.provider = new ServletAuthConfigProvider(properties, factory);


        String[] regIDS = factory.getRegistrationIDs(provider);
        for (String i : regIDS) {
            try {
                RegistrationContext r = factory.getRegistrationContext(i);
                System.out.println(contextToString(r));
                AuthConfigProvider p = factory.getConfigProvider
                        (r.getMessageLayer(), r.getAppContext(), null);
                ServerAuthConfig c = p.getServerAuthConfig
                        (r.getMessageLayer(), r.getAppContext(),
                        new CallbackHandler() {
                            public void handle(Callback[] clbcks)
                                    throws IOException, UnsupportedCallbackException {
                                throw new UnsupportedOperationException("Not supported yet.");
                            }
                        });
                ServerAuthContext s = c.getAuthContext("0", new Subject() , new HashMap());
            } catch (AuthException ex) {
                Logger.getLogger(RuntimeSurrogate.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public final String contextToString(RegistrationContext r) {
        String rvalue = r.getDescription() + "\n\t" + r.getAppContext() + "\n\t" +
                r.getMessageLayer() + "\n\t" + r.isPersistent() + "\n";
        return rvalue;
    }

    public static void main(String[] args) {
        System.out.println("Security Manager is " +
                (System.getSecurityManager() == null ? "OFF" : "ON"));
        System.out.println("user.dir: " + System.getProperty("user.dir"));
        HashMap<String, String> properties = new HashMap<String, String>();
        for (String s : args) {
            StringTokenizer tokenizer = new StringTokenizer(s, "=");
            if (tokenizer.countTokens() == 2) {
                String key = tokenizer.nextToken();
                String value = tokenizer.nextToken();
                System.out.println("key: " + key + " value: " + value);
                properties.put(key, value);
            }
        }
        RuntimeSurrogate rS = new RuntimeSurrogate(properties);
    }
}
