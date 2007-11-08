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

package com.sun.enterprise.iiop;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.TSIdentification;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import com.sun.corba.ee.impl.logging.POASystemException;
import com.sun.corba.ee.impl.txpoa.TSIdentificationImpl;
import com.sun.corba.ee.spi.costransactions.TransactionService;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.orb.ParserImplBase;
import com.sun.corba.ee.spi.orb.PropertyParser;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.OperationFactory;
import com.sun.corba.ee.spi.orbutil.copyobject.ObjectCopierFactory ;
import com.sun.corba.ee.spi.copyobject.CopierManager;
import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults;
import com.sun.corba.ee.impl.orbutil.ORBConstants;
import com.sun.corba.ee.impl.orbutil.closure.Constant;
import com.sun.corba.ee.spi.orbutil.threadpool.ThreadPoolManager;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.util.S1ASThreadPoolManager;

import com.sun.enterprise.Switch;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.ContainerTypeOrApplicationType;
import com.sun.corba.ee.spi.presentation.rmi.InvocationInterceptor;
import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler ;
import com.sun.corba.ee.spi.ior.ObjectKey ;

// Internal JTS interceptor implementation
import com.sun.jts.pi.InterceptorImpl;

import java.util.logging.*;
import com.sun.logging.*;

public class PEORBConfigurator implements ORBConfigurator {

    private static final java.util.logging.Logger logger =
		java.util.logging.Logger.getLogger(LogDomains.CORBA_LOGGER);

    private static final String OPT_COPIER_CLASS = 
	"com.sun.corba.ee.spi.copyobject.OptimizedCopyobjectDefaults";

    private static com.sun.jts.pi.InterceptorImpl jtsInterceptor;
    private static TSIdentification tsIdent;
    private static ORB theORB;
    private static ThreadPoolManager threadpoolMgr = null;
    private static boolean txServiceInitialized = false;

    static {
	tsIdent = new TSIdentificationImpl();
    }

    public void configure( DataCollector dc, ORB orb ) {
        //begin temp fix for bug 6320008
        // this is needed only because we are using transient Name Service
        //this should be removed once we have the persistent Name Service in place
	orb.setBadServerIdHandler( 
	    new BadServerIdHandler() {
		public void handle( ObjectKey objectkey ) {
		    // NO-OP
		}
	    }
	) ;
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
	ObjectCopierFactory stream;
	CopierManager cpm = orb.getCopierManager();

	// Get the default copier factory
	stream = CopyobjectDefaults.makeORBStreamObjectCopierFactory(orb);
	cpm.registerObjectCopierFactory(stream, 
			    POARemoteReferenceFactory.PASS_BY_VALUE_ID);
	cpm.setDefaultId(POARemoteReferenceFactory.PASS_BY_VALUE_ID);

	// Detect if the optimized copier class exists in the classpath
	// or not. For the RI, one should get a ClassNotFoundException
	try {
	    Class cls =  Class.forName(OPT_COPIER_CLASS);
	    configureOptCopier(orb, cls, stream);
	} catch (ClassNotFoundException cnfe) {
	    // Don't do anything. This is true for RI and the default 
	    // stream copier is fine for that
	}
    }

    private static void configureOptCopier(ORB orb, Class cls, 
					ObjectCopierFactory stream) {
	CopierManager cpm = orb.getCopierManager();
	
	// Get the reference copier factory
	ObjectCopierFactory reference = CopyobjectDefaults.
				getReferenceObjectCopierFactory();
	
	try {
	    Method m = cls.getMethod("makeReflectObjectCopierFactory",
		    new Class[] {com.sun.corba.ee.spi.orb.ORB.class});
	    ObjectCopierFactory reflect = 
		(ObjectCopierFactory)m.invoke(cls, new Object[] {orb});
	    ObjectCopierFactory fallback = 
		CopyobjectDefaults.makeFallbackObjectCopierFactory(reflect, stream);
	    cpm.registerObjectCopierFactory(fallback, 
		    POARemoteReferenceFactory.PASS_BY_VALUE_ID);
	    cpm.registerObjectCopierFactory(reference, 
		    POARemoteReferenceFactory.PASS_BY_REFERENCE_ID);
	    cpm.setDefaultId(POARemoteReferenceFactory.PASS_BY_VALUE_ID);
	} catch (NoSuchMethodException e) {
	    logger.log(Level.FINE,"Caught NoSuchMethodException - " + e.getMessage());
	    logger.log(Level.FINE,"Proceeding with pass-by-value copier set to stream copier");
	} catch (IllegalAccessException e) {
	    logger.log(Level.FINE,"Caught IllegalAccessException - " + e.getMessage());
	    logger.log(Level.FINE,"Proceeding with pass-by-value copier set to stream copier");
	} catch (IllegalArgumentException e) {
	    logger.log(Level.FINE,"Caught IllegalArgumentException - " + e.getMessage());
	    logger.log(Level.FINE,"Proceeding with pass-by-value copier set to stream copier");
	} catch (InvocationTargetException e) {
	    logger.log(Level.FINE,"Caught InvocationTargetException - " + e.getMessage());
	    logger.log(Level.FINE,"Proceeding with pass-by-value copier set to stream copier");
	} catch (NullPointerException e) {
	    logger.log(Level.FINE,"Caught NullPointerException - " + e.getMessage());
	    logger.log(Level.FINE,"Proceeding with pass-by-value copier set to stream copier");
	} catch (ExceptionInInitializerError e) {
	    logger.log(Level.FINE,"Caught ExceptionInInitializerError - " + e.getMessage());
	    logger.log(Level.FINE,"Proceeding with pass-by-value copier set to stream copier");
	}
    }

    // Called from J2EEInitializer
    static void setJTSInterceptor(InterceptorImpl intr, ORB orb)
    {
	theORB = orb;
        jtsInterceptor = intr;

        // Set ORB and TSIdentification: needed for app clients, 
        // standalone clients.
        jtsInterceptor.setOrb(theORB);
    }

    // Called from ORBManager only when the ORB is running on server side
    public static void setThreadPoolManager() {
	threadpoolMgr = S1ASThreadPoolManager.getThreadPoolManager();
    }

    public synchronized static void initTransactionService(String jtsClassName, Properties 
	jtsProperties ) 
    {
        if (txServiceInitialized == false ) {
	    String clsName = (jtsClassName == null) ? 
	      "com.sun.jts.CosTransactions.DefaultTransactionService" : jtsClassName;
	    try {
	        Class theJTSClass = Class.forName(clsName);

		if (theJTSClass != null) {
		    try {
		        TransactionService jts = (TransactionService)theJTSClass.newInstance();
			jts.identify_ORB(theORB, tsIdent, jtsProperties ) ; 
			jtsInterceptor.setTSIdentification(tsIdent);
			// XXX should jts.get_current() be called everytime
			// resolve_initial_references is called ??
			org.omg.CosTransactions.Current transactionCurrent =
			  jts.get_current();
			
			theORB.getLocalResolver().register(
							   ORBConstants.TRANSACTION_CURRENT_NAME,
							   new Constant(transactionCurrent));
			
			// the JTS PI use this to call the proprietary hooks
			theORB.getLocalResolver().register(
							   "TSIdentification", new Constant(tsIdent));
			txServiceInitialized = true;	
		    } catch (Exception ex) {
		        throw new org.omg.CORBA.INITIALIZE(
							   "JTS Exception: "+ex, POASystemException.JTS_INIT_ERROR ,
							   CompletionStatus.COMPLETED_MAYBE);
		    }
		}
	    } catch (ClassNotFoundException cnfe) {
	        logger.log(Level.SEVERE,"iiop.inittransactionservice_exception",cnfe);
	    }	    
	}

    }
    
    private static void configureCallflowInvocationInterceptor(ORB orb) {
        orb.setInvocationInterceptor(
	    new InvocationInterceptor() {
		public void preInvoke() {		    
		    Agent agent = Switch.getSwitch().getCallFlowAgent();
		    if (agent != null) {
		        agent.startTime(
					ContainerTypeOrApplicationType.ORB_CONTAINER);
		    }
		}
		public void postInvoke() {
		    Agent agent = Switch.getSwitch().getCallFlowAgent();
		    if (agent != null) {
		        agent.endTime();
		    }
		}
	    }
	);
    }
}
