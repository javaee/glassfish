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

package com.sun.enterprise.connectors.work;

import com.sun.enterprise.util.Utility;
import com.sun.enterprise.connectors.ConnectorRuntimeException;
import com.sun.logging.LogDomains;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.spi.work.WorkManager;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * WorkManagerFactory allows other customized WorkManager implementation
 * to be plugged into the server.  The name of the customized 
 * implementation class for the WorkManager has to be specified as
 * a system property "workmanager.class".
 * <p>
 * It is assumed that the implementation for WorkManager also provides
 * a public method called "getInstance" that returns a WorkManager object.
 * This frees the WorkManagerFactory from deciding whether WorkManager
 * is implemented as a Singleton in the server.
 * <p>
 * @author	Qingqing Ouyang, Binod P.G.
 */
public final class WorkManagerFactory {
    
    private static final String DEFAULT = 
    "com.sun.enterprise.connectors.work.CommonWorkManager";
    
    private static final String WORK_MANAGER_CLASS = "workmanager.class";
    
    private static final Logger logger = 
    LogDomains.getLogger(LogDomains.RSR_LOGGER);
 
    private static final StringManager localStrings = 
                        StringManager.getManager(WorkManagerFactory.class);
   
    /**
     * This is called by the constructor of BootstrapContextImpl
     */
    public static WorkManager getWorkManager(String poolName) 
                              throws ConnectorRuntimeException {
        
        String className = null;
        String methodName = "getInstance";
        Class cls = null;
        WorkManager wm = null;
        
        try {
            className = System.getProperty(WORK_MANAGER_CLASS, DEFAULT);

            // Default work manager implementation is not a singleton.
            if (className.equals(DEFAULT)) {
                return new CommonWorkManager(poolName);
            }
            
            cls = Class.forName(className);
            if (cls != null) {
                Method method = cls.getMethod("getInstance", new Class[]{});
                wm = (WorkManager) method.invoke(cls, new Object[] {});
            }
        } catch (Exception e) {
            String msg = localStrings.getString("workmanager.instantiation_error");
            logger.log(Level.SEVERE, msg, e);
        }
        
        return wm;
    }

}
