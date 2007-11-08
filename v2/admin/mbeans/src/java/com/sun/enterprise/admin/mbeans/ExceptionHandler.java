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
 * ExceptionHandler.java
 *
 * Created on February 26, 2004, 3:39 PM
 */

package com.sun.enterprise.admin.mbeans;

import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.util.i18n.StringManagerBase;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author  kebbs
 */
public class ExceptionHandler {
    
    private Logger _logger;
    
    
    /**
     * Create a new exception handler with the specified logger
     * @param logger
     */    
    public ExceptionHandler(Logger logger) {
        _logger = logger;
    }
     
    protected Logger getLogger() 
    {
        return _logger;
    }  
                           
    public InstanceException handleInstanceException(Exception ex, String messageId, String arg) 
    {
        return handleInstanceException(ex, messageId, new String[] {arg});
    }
               
    /**
     * Convert an incoming exception to an InstanceException and log if the incoming exception
     * is not an InstanceException.
     * @param ex The exception
     * @param messageId the resource bundle id of the message to log
     * @param args arguments to be passed to the log message
     * @return InstanceException
     */    
    public InstanceException handleInstanceException(Exception ex, String messageId, String[] args) 
    {
        InstanceException result = null;
        Level level = Level.FINE;
        if (ex instanceof InstanceException) {                       
            result = (InstanceException)ex;            
        } else if (ex instanceof ConfigException) {            
            result = new InstanceException(ex);
        } else {  
            level = Level.WARNING;
            result = new InstanceException(ex);
        }
        StringManagerBase sm = StringManagerBase.getStringManager(getLogger().getResourceBundleName());            
        getLogger().log(level, sm.getString(messageId, args), ex);             
        return result;
    }                    
            
    public ConfigException handleConfigException(Exception ex, String messageId, String arg) 
    {
        return handleConfigException(ex, messageId, new String[] {arg});
    }
    
    /**
     * Convert an incoming exception to an ConfigException and log if the incoming exception
     * is not a ConfigException
     * @param ex The exception
     * @param messageId the resource bundle id of the message to log
     * @param args arguments to be passed to the log message
     * @return ConfgException
     */    
    public ConfigException handleConfigException(Exception ex, String messageId, String[] args)
    {
        ConfigException result = null;
        Level level = Level.FINE;
        if (ex instanceof ConfigException) {                       
            result = (ConfigException)ex;
        } else {  
            level = Level.WARNING;
            result = new ConfigException(ex);
        }
        StringManagerBase sm = StringManagerBase.getStringManager(getLogger().getResourceBundleName());            
        getLogger().log(level, sm.getString(messageId, args), ex);             
        return result;
    }    
}
