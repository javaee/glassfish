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

/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.modeler;

import java.util.Map;
import java.util.Hashtable;
import com.sun.mfwk.agent.appserv.util.Constants;

/**
 * Context used during processing.
 */
public class ModelerContext {

    /**
     * Constructor.
     * 
     * @param  serverName  server name
     * @param  domainName  domain name
     */
    public ModelerContext(String serverName, String domainName) {

        _tokens        = new Hashtable();
        _serverName    = serverName;
        _domainName    = domainName;
        _moduleName    = Constants.DEF_MODULE_NAME;

        // add server name token
        addToken(Constants.SERVER_NAME_PROP, serverName);

        // add domain name token
        addToken(Constants.DOMAIN_NAME_PROP, domainName);
    }

    /**
     * Sets the module name.
     *
     * @param moduleName name of the module : com.sun.cmm.as
     */
    public void setModuleName(String moduleName) {
        _moduleName = moduleName;
    }

    /**
     * Returns the module name.
     *
     * @return name of the module : com.sun.cmm.as
     */
    public String getModuleName() {
        return _moduleName;
    }

    /**
     * Returns the server name.
     *
     * @return   server name
     */
    public String getServerName() {
        return _serverName;
    }

    /**
     * Returns the domain name.
     *
     * @return   domain name
     */
    public String getDomainName() {
        return _domainName;
    }

    /**
     * Adds the key-val pair as a token. During the processing of the 
     * declarative template, if any tokens are found that matches the 
     * key, it will be replaced with the given val. Tokens are 
     * expected to be expressed as ${key} in the template.
     *
     * @param  key  token key
     * @param  val  token value
     */
    public void addToken(String key, String val) {
        _tokens.put(key, val);
    }

    /**
     * Adds all available tokens from the given map. 
     * 
     * @param  tokens  map containing multiple key-value pair of tokens
     */
    public void addTokens(Map tokens) {
        _tokens.putAll(tokens);
    }

    /**
     * Returns all available tokens.
     *
     * @return  all available tokens to be used during template processing
     */
    public Map getTokens() {
        return _tokens;
    }

    // ---- VARIABLES - PRIVATE ----------------------------
    private Map _tokens                          = null;
    private String _moduleName                   = null;
    private String _serverName                   = null;
    private String _domainName                   = null;
}
