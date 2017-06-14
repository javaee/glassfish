/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package test;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.WebConnection;

/**
 * The protocol is as follows:
 * Client:
 *  (a_isbn_to_be_verified CRLF)*| EXIT
 *   and tokens can be separated by " \t\n\r\f".
 * Server:
 *  (a_previous_isbn (true|false) CRLF)*
 */
public class ISBNHttpUpgradeHandler implements HttpUpgradeHandler {
    @Inject
    private ISBNValidator isbnValidator;

    private boolean debug = false;

    private String appName = null;

    public ISBNHttpUpgradeHandler() {
    }

    @Override
    public void init(WebConnection wc) {
        System.out.println("ISBNHttpUpgradeHandler.init");
        try {
            InitialContext initialContext = new InitialContext();
            appName = (String)initialContext.lookup("java:app/AppName");
            if (debug) {
                System.out.println("--> appName: " + appName);
            }

            ServletInputStream input = wc.getInputStream();
            ReadListenerImpl readListener =
                new ReadListenerImpl(appName, input, wc, isbnValidator, debug);
            input.setReadListener(readListener);
            wc.getOutputStream().flush();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void destroy() {
        if (debug) {
            System.out.println("--> destroy");
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
