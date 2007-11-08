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

package com.sun.enterprise.server.logging;

import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.enterprise.admin.event.LogLevelChangeEventListener;
import com.sun.enterprise.admin.event.LogLevelChangeEvent;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigChange;
import com.sun.enterprise.config.ConfigAdd;
import com.sun.enterprise.config.ConfigUpdate;      

public class LogLevelChangeEventListenerImpl 
    implements LogLevelChangeEventListener
{
    public void logLevelChanged(LogLevelChangeEvent event)
        throws AdminEventListenerException 
    {
        try {
            if( event.isPropertyChanged() ) {
                ConfigContext newConfig = event.getConfigContext();                
                Iterator iter = event.getConfigChangeList().iterator();
                while (iter.hasNext() )  {
                    Object change= iter.next();
                    if (change instanceof ConfigAdd || change instanceof ConfigUpdate) {
                        String xpath = ((ConfigChange)change).getXPath();
                        if( xpath != null){
                            ConfigBean item = newConfig.exactLookup(xpath); 
                            if (item instanceof ElementProperty) {
                                ElementProperty elementProperty = (ElementProperty)item;
                                String loggerName  = elementProperty.getName();
                                String logLevel    = elementProperty.getValue();
                                boolean logExists = LogMBean.getInstance().findLogger(loggerName);
                                if (!logExists) {
                                    Logger.getLogger(loggerName);  //if the logger doesn't exist, create it.
				}        
                                LogMBean.getInstance().setLogLevel( loggerName, logLevel );
                            }
                        }
                    }
                }
            } else {
                // This is not a property change event, so it must be a 
                // module log level change.
                LogMBean.getInstance().setLogLevelForModule(
                    event.getModuleName(), event.getNewLogLevel() );
            }
        } catch( Exception e ) {
            new ErrorManager().error( "Error In LogLevelChanged event", e,
                ErrorManager.GENERIC_FAILURE );
        }
    }
}
