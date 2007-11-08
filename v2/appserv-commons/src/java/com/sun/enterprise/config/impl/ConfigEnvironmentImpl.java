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
 * DefaultConfigContextSettings.java
 *
 * Created on January 12, 2004, 11:44 AM
 */

package com.sun.enterprise.config.impl;

import com.sun.enterprise.config.pluggable.ConfigEnvironment;
import com.sun.enterprise.config.pluggable.ConfigBeansSettings;
import com.sun.enterprise.config.pluggable.ConfigBeanInterceptor;

/**
 * This class creates a default implementation of ConfigEnvironment
 * but this class has to be extended for setting 2 attributes.
 * url and rootClass return empty.
 *
 * @author  sridatta
 */
public class ConfigEnvironmentImpl implements ConfigEnvironment {
    
    private boolean _autoCommitFlag = false;
    private boolean _readOnlyFlag = true;
    private boolean _cachingEnabledFlag = false;
    private boolean _assertNotInCache = false;
    
    private ConfigBeanInterceptor _cbInterceptor = null;
    private String _url = null;
    private String _handler = "com.sun.enterprise.config.serverbeans.ServerValidationHandler";
    private String _rootClass = "com.sun.enterprise.config.serverbeans.Domain";

    
    /** Creates a new instance */
    public ConfigEnvironmentImpl() {
    }

    public boolean isAutoCommitOn() {
        return _autoCommitFlag;
    }    
    
    public void setAutoCommitOn(boolean value) {
        _autoCommitFlag = value;
    }
    
    public ConfigBeanInterceptor getConfigBeanInterceptor() {
        return _cbInterceptor;
    }
    
    public void setConfigBeanInterceptor(ConfigBeanInterceptor cbInterceptor) {
        _cbInterceptor = cbInterceptor;
    }
    
    /**
     * returns null
     * Needs to be overridden
     */
    public String getUrl() {
        return _url;
    }
    
    public void setUrl(String url) {
        _url = url;
    }
    
    public String getHandler() {
        return _handler;
    }
    
    public void setHandler(String handler) {
        _handler = handler;
    }
    
    public boolean isReadOnly() {
        return _readOnlyFlag;
    }
    
     public void setReadOnly(boolean value) {
        _readOnlyFlag = value;
    }
     
    /**
     * returns null by default.
     * Needs to be overridden
     */
    public String getRootClass() {
        return _rootClass;
    }
    
     public void setRootClass(String rootClass) {
        _rootClass = rootClass;
    }
     
    public boolean isCachingEnabled() {
        return _cachingEnabledFlag;
    }
    
    public void setCachingEnabled(boolean value) {
        _cachingEnabledFlag = value;
    }
    
    public ConfigBeansSettings getConfigBeansSettings() {
        return new ConfigBeansSettingsImpl();
    }
    
}
