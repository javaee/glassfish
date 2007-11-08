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
package com.sun.enterprise.admin.wsmgmt.repository.impl.cache;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Represents a J2EE Application with ejb and web modules that have
 * web services. 
 *
 * @author Nazrul Islam
 * @since  J2SE 5.0
 */
public class J2eeApplication {

    /**
     * Constructor. 
     * 
     * @param  name  name of the application
     * @param  ejb   name of ejb bundles with web services
     * @param  web   name of web bundles with web services
     */
    J2eeApplication(String name, List ejb, List web) {
        _name        = name;
        _ejbBundles  = ejb;
        _webBundles  = web;
    }

    /**
     * Constructor.
     *
     * @param  key  name of the application
     * @param  val  string representation of the ejb and web bundles
     */
    J2eeApplication(String key, String val) {
        _name = key;
        _ejbBundles = new ArrayList();
        _webBundles = new ArrayList();

        StringTokenizer st = new StringTokenizer(val, DELIM);
        while (st.hasMoreTokens()) {
            String m = st.nextToken();
            if (m.startsWith(EJB_KEY)) {
                int ejbKeyLength = EJB_KEY.length();
                String ejbModule = m.substring(ejbKeyLength);   
                addEjbBundle(ejbModule);

            } else if (m.startsWith(WEB_KEY)) {
                int webKeyLength = WEB_KEY.length();
                String webModule = m.substring(webKeyLength);   
                addWebBundle(webModule);
            }
        }
    }

    /**
     * Returns the name of the application.
     * 
     * @return  name of the application
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns a string representation of the ejb and web bundles.
     *
     * @return  a string representation of the ejb and web bundles
     */
    String getPersistentValue() {
        StringBuffer sb = new StringBuffer();

        for (Iterator iter=_ejbBundles.iterator(); iter.hasNext();) {
            String ejb = (String) iter.next();
            sb.append(EJB_KEY);
            sb.append(ejb);
            sb.append(DELIM);
        }

        for (Iterator iter=_webBundles.iterator(); iter.hasNext();) {
            String web = (String) iter.next();
            sb.append(WEB_KEY);
            sb.append(web);
            sb.append(DELIM);
        }

        String persistentValue = null;
        int length = sb.length();
        if (length > 0) {
            String val = sb.toString();
            persistentValue = val.substring(0, length-1);
        }
        return persistentValue;
    }

    /**
     * Returns the name of the ejb bundles that have web services. 
     *
     * @return  list of ejb bundles with web services
     */
    public List getEjbBundles() {
        return _ejbBundles;
    }

    /**
     * Adds an ejb bundle to the list.
     * 
     * @param  name  name of the ejb bundle
     */
    void addEjbBundle(String name) {
        _ejbBundles.add(name);
    }

    /**
     * Removes an ejb bundle from the list.
     * 
     * @param  name  name of the ejb bundle
     * @return  true if the bundle was removed
     */
    boolean removeEjbBundle(String name) {
        return _ejbBundles.remove(name);
    }

    /**
     * Returns the name of the web bundles that have web services. 
     *
     * @return  list of web bundles with web services 
     */
    public List getWebBundles() {
        return _webBundles;
    }

    /**
     * Adds a web bundle to the list.
     * 
     * @param  name  name of the web bundle
     */
    void addWebBundle(String name) {
        _webBundles.add(name);
    }

    /**
     * Removes a web bundle from the list.
     * 
     * @param  name  name of the web bundle
     * @return true if bundle was removed
     */
    boolean removeWebBundle(String name) {
        return _webBundles.remove(name);
    }

    // ---- VARIABLES - PRIVATE --------------------------------------
    String _name                 = null;
    List _ejbBundles             = null;
    List _webBundles             = null;
    static final String DELIM    = ",";
    static final String EJB_KEY  = "EJB_";
    static final String WEB_KEY  = "WEB_";
}
