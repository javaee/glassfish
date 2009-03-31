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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.spi.copyobject.CopierManager;
import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.orbutil.copyobject.ObjectCopierFactory;
import com.sun.corba.ee.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.presentation.rmi.InvocationInterceptor;
import com.sun.logging.LogDomains;
import org.glassfish.enterprise.iiop.api.IIOPConstants;
import org.glassfish.enterprise.iiop.util.S1ASThreadPoolManager;


// TODO import org.omg.CORBA.TSIdentification;

// TODO import com.sun.corba.ee.impl.txpoa.TSIdentificationImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class PEORBConfigurator implements ORBConfigurator {

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(LogDomains.CORBA_LOGGER);

    // TODO private static TSIdentification tsIdent;
    private static ORB theORB;
    private static ThreadPoolManager threadpoolMgr = null;
    private static boolean txServiceInitialized = false;

    static {
        // TODO tsIdent = new TSIdentificationImpl();
    }

    public void configure(DataCollector dc, ORB orb) {
        //begin temp fix for bug 6320008
        // this is needed only because we are using transient Name Service
        //this should be removed once we have the persistent Name Service in place
        /*TODO
        orb.setBadServerIdHandler(
                new BadServerIdHandler() {
                    public void handle(ObjectKey objectkey) {
                        // NO-OP
                    }
                }
        );
        */

        //end temp fix for bug 6320008
        if (threadpoolMgr != null) {
            // This will be the case for the Server Side ORB created
            // For client side threadpoolMgr will be null, so we will
            // never come here
            orb.setThreadPoolManager(threadpoolMgr);
        }

        configureCopiers(orb);
        configureCallflowInvocationInterceptor(orb);
    }

    private static void configureCopiers(ORB orb) {

        CopierManager cpm = orb.getCopierManager();

        ObjectCopierFactory stream = CopyobjectDefaults.makeORBStreamObjectCopierFactory(orb) ;
        ObjectCopierFactory reflect = CopyobjectDefaults.makeReflectObjectCopierFactory( orb ) ;
        ObjectCopierFactory fallback = CopyobjectDefaults.makeFallbackObjectCopierFactory( reflect, stream ) ;
        ObjectCopierFactory reference = CopyobjectDefaults.getReferenceObjectCopierFactory() ;

        cpm.registerObjectCopierFactory( fallback, IIOPConstants.PASS_BY_VALUE_ID ) ;
        cpm.registerObjectCopierFactory( reference, IIOPConstants.PASS_BY_REFERENCE_ID ) ;
        cpm.setDefaultId( IIOPConstants.PASS_BY_VALUE_ID ) ;
       
    }



    // Called from GlassFishORBManager only when the ORB is running on server side
    public static void setThreadPoolManager() {
        threadpoolMgr = S1ASThreadPoolManager.getThreadPoolManager();
    }

    private static void configureCallflowInvocationInterceptor(ORB orb) {
        orb.setInvocationInterceptor(
                new InvocationInterceptor() {
                    public void preInvoke() {
                        /*    TODO
                  Agent agent = Switch.getSwitch().getCallFlowAgent();
                  if (agent != null) {
                      agent.startTime(
                          ContainerTypeOrApplicationType.ORB_CONTAINER);
                  }
                  */
                    }

                    public void postInvoke() {
                        /*   TODO
                  Agent agent = Switch.getSwitch().getCallFlowAgent();
                  if (agent != null) {
                      agent.endTime();
                  }
                  */
                    }
                }
        );
    }
}
