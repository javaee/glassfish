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
package com.sun.ejb.containers;


import java.util.logging.*;
import java.util.Vector;
import java.util.Iterator;

import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;

import com.sun.ejb.Container;
import com.sun.ejb.InvocationInfo;
import com.sun.enterprise.deployment.*;

import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.serverbeans.EjbContainer;
import com.sun.enterprise.config.serverbeans.EjbTimerService;


public class TimerBeanContainer
    extends EntityContainer
{
    
    private EJBTimerService ejbTimerService;
        
    /**
     * This constructor is called when the timer service system application is
     * loaded.
     * @exception Exception on error
     */
    protected TimerBeanContainer(EjbDescriptor desc, ClassLoader loader)
        throws Exception
    {
        super(desc, loader);

        _logger.log(Level.FINE,"[TimerBeanContainer] Created "
                + " TimerBeanContainer: " + logParams[0]);

    }

    public void onShutdown() {
        _logger.log(Level.FINE,"[TimerBeanContainer] onShutdown() called....");

        super.onShutdown();

        if (ejbTimerService != null) {
            ejbTimerService.onShutdown();
        }
    }

    /**
     * Called after all the components in the container's application
     * have deployed successfully.
     */
    public void doAfterApplicationDeploy() {

        super.doAfterApplicationDeploy();    

        try {

            TimerLocalHome timerLocalHome = (TimerLocalHome) ejbLocalHome;

            // Do "health check" on access to persistent timer info.
            EjbBundleDescriptor bundle = 
                ejbDescriptor.getEjbBundleDescriptor();

            // Get timer data source name set in timer service system app's
            // sun-ejb-jar.xml
            ResourceReferenceDescriptor cmpResource = 
                bundle.getCMPResourceReference();

            String cmpResourceJndiName = cmpResource.getJndiName();

            // Get the timer data source name from the domain.xml
            ServerContext sc = ApplicationServer.getServerContext();
            EjbContainer ejbc = ServerBeansFactory.
                getConfigBean(sc.getConfigContext()).getEjbContainer();
            EjbTimerService ejbt = ejbc.getEjbTimerService();
            // EjbTimerService is an optional element
            String ejbtDatasource = (ejbt != null) ?
                ejbt.getTimerDatasource() : null;

            // Override the timer datasource with the one from domain.xml 
            // if necessary.  
            if( (ejbtDatasource != null) && 
                (!ejbtDatasource.equals(cmpResourceJndiName)) ) {

                cmpResourceJndiName = ejbtDatasource;               
                 
                // overwrite datasource jndi name in descriptor
                cmpResource.setJndiName(cmpResourceJndiName);
            }

            // Make sure cmp resource is available in the namespace.
            Context context = new InitialContext();
            context.lookup(cmpResourceJndiName);
            
            // Make an invocation on timer bean to ensure that app is 
            // initialized properly.  Second param determines whether 
            // exhaustive database checks are performed.  These are time 
            // consuming so they will be disabled by default.  
            boolean checkStatus = 
                timerLocalHome.checkStatus(cmpResourceJndiName, false);

            if( checkStatus ) {

                //descriptor object representing this application or module
                Application application = ejbDescriptor.getApplication();
        
                //registration name of the applicaton
                String appID = application.getRegistrationName();

                Vector ejbs = application.getEjbDescriptors();
                TimerMigrationLocalHome timerMigrationLocalHome = null;
                for(Iterator iter = ejbs.iterator(); iter.hasNext();) {
                    EjbDescriptor next = (EjbDescriptor) iter.next();
                    if( next.getLocalHomeClassName().equals
                        (TimerMigrationLocalHome.class.getName()) ) {
                        BaseContainer container = (BaseContainer)
                            containerFactory.getContainer(next.getUniqueId());
                        timerMigrationLocalHome = (TimerMigrationLocalHome)
                            container.getEJBLocalHome();
                        break;
                    }
                }

                // Create EJB Timer service. 
                ejbTimerService = 
                    new EJBTimerService(appID, timerLocalHome,
                                        timerMigrationLocalHome);

                containerFactory.setEJBTimerService(ejbTimerService);

                _logger.log(Level.INFO, "ejb.timer_service_started",
                            new Object[] { cmpResourceJndiName } );

            } else {
                // error logged by timer bean.
            }

        } catch (NamingException nnfe) {

            // This is most likely caused by the timer datasource not being
            // configured for this server instance.
            _logger.log(Level.WARNING, "ejb.timer_service_init_error", 
                        logParams);

        } catch (Exception ex) {
            _logger.log(Level.WARNING, "ejb.timer_service_init_error", 
                        logParams);
            _logger.log(Level.WARNING, "", ex);
        } 
        
    }
    
} //TimerBeanContainer.java
