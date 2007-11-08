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

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;

import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.common.constant.AdminConstants;

import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.security.store.IdentityManager;

import java.util.logging.Logger;
import java.util.Enumeration;
import java.util.ArrayList;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class PasswordAliasConfigMBean extends BaseConfigMBean 
{              
    private static final StringManager _strMgr = 
        StringManager.getManager(PasswordAliasConfigMBean.class);

    private static Logger _logger = null;   
        
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(AdminConstants.kLoggerName);
        }
        return _logger;
    }           

   private static ExceptionHandler _handler = null;
    
    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler() 
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }
    
    public PasswordAliasConfigMBean() {
        super();        
    }       
                
    private PasswordAdapter getPasswordAdapter()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException
    {
        //The masterPassword in the IdentityManager is available only through
        //a running DAS, server instance, or node agent.
        String masterPassword = IdentityManager.getMasterPassword();
        return new PasswordAdapter(masterPassword.toCharArray());
    }
    
    /**
     * Add a non-existing password alias
     * @param aliasName the name of the alias   
     * @param password the password of the alias
     * @throws ConfigException
     */    
    public void addPasswordAlias(String aliasName, String password) throws ConfigException        
    {
        try {            
            PasswordAdapter p = getPasswordAdapter();
            if (p.aliasExists(aliasName)) {
                throw new ConfigException(_strMgr.getString("passwordAliasExists", aliasName));
            } 
            p.setPasswordForAlias(aliasName, password.getBytes());           
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "addPasswordAliasException", aliasName);
        }
    }
    
    /**
     * Remove an existing password alias
     * @param aliasName the name of the password alias to remove
     * @throws ConfigException
     */    
    public void removePasswordAlias(String aliasName) throws ConfigException        
    {
        try {            
            PasswordAdapter p = getPasswordAdapter();
            if (!p.aliasExists(aliasName)) {
                throw new ConfigException(_strMgr.getString("passwordAliasDoesNotExist", aliasName));
            }
            p.removeAlias(aliasName);
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "removePasswordAliasException", aliasName);
        }
    }
    
    /**
     * Update the password for an existing alias
     * @param aliasName the name of the alias whose password is to be updated
     * @param password the new password
     * @throws ConfigException
     */    
    public void updatePasswordAlias(String aliasName, String password) throws ConfigException        
    {
        try {
            PasswordAdapter p = getPasswordAdapter();
            if (!p.aliasExists(aliasName)) {
                throw new ConfigException(_strMgr.getString("passwordAliasDoesNotExist", aliasName));
            }
            p.setPasswordForAlias(aliasName, password.getBytes());
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "updatePasswordAliasException", aliasName);
        }
    }
    
    /**
     * Get all the password aliases
     * @throws ConfigException
     * @return The list of password aliases
     */    
    public String[] getPasswordAliases() throws ConfigException        
    {
        try {   
            ArrayList result = new ArrayList();
            Enumeration en = getPasswordAdapter().getAliases();
            while (en.hasMoreElements()) {                            
                result.add((String)en.nextElement());                               
            }            
            return (String[])result.toArray(new String[result.size()]);
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "listPasswordAliasException", "");
        }
    }
}
