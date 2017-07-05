/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package samples.lifecycle.simple;
import java.util.Properties;
import java.util.Date;
import javax.jms.*;
import javax.naming.*;
import java.sql.*;

import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.appserv.server.LifecycleEventContext;


/**
 *  LifecycleTopic is an implementation for the LifecycleListener interface.
 *  <p>
 *  Sun ONE Application Server emits five events during its lifecycle -
 *  1. INIT_EVENT: Server is initializing subsystems and setting up the runtime environment.
 *  2. STARTUP_EVENT: Server is starting up applications
 *  3. READY_EVENT: Server started up applications and is ready to service requests
 *  4. SHUTDOWN_EVENT: Server is shutting down applications
 *  5. TERMINATION_EVENT: Server is terminating the subsystems and the runtime environment.
 * 
 *  In this sample, on STARTUP_EVENT, a thread is started which sends a simple JMS message to 
 *  sampleTopic every minute. On SHUTDOWN_EVENT, this thread is stopped.
 *  </p>
 */

public class LifecycleListenerImpl implements LifecycleListener {

    /**
     *  Life cycle event context
     */
    LifecycleEventContext ctx;

    /** 
     *  Receives a server lifecycle event 
     *  @param event associated event
     *  @throws <code>ServerLifecycleException</code> for exceptional condition.
     */
    public void handleEvent(LifecycleEvent event) 
                         throws ServerLifecycleException {

        ctx = event.getLifecycleEventContext();

	switch(event.getEventType()) {
	    case LifecycleEvent.INIT_EVENT:
		onInitTask();
	  	break;

	    case LifecycleEvent.STARTUP_EVENT:
		onStartTask();
	  	break;

            case LifecycleEvent.READY_EVENT:
                onReadyTask();
                break;

	    case LifecycleEvent.SHUTDOWN_EVENT:
		onShutdownTask();
	  	break;

	    case LifecycleEvent.TERMINATION_EVENT:
		onTerminationTask();
	  	break;
	}

    }

    /**
     *  Task to be carried out in the INIT_EVENT.
     *  Logs a message.
     */
    private void onInitTask() {
        ctx.log("LifecycleTopic: INIT_EVENT");
    }

    /**
     *  Tasks to be carried out in the STARTUP_EVENT.
     *  Logs a message
     */
    private void onStartTask() {
        ctx.log("LifecycleTopic: STARTUP_EVENT");
        // my code
        QueueSession qsession[] = new QueueSession[10];
        Queue queue[] = new Queue[10];
        
            try{
                for (int i =0; i < 10; i++) {
                    // Get initial context
                    ctx.log("Get initial context");
                    InitialContext initialContext = new InitialContext();
                
                    // look up the connection factory from the object store
                    ctx.log("Looking up the queue connection factory from JNDI");
                    QueueConnectionFactory factory = (QueueConnectionFactory) initialContext.lookup("jms/QCFactory");
                
                    // look up queue from the object store
                    ctx.log("Create queue connection");
                    QueueConnection qconn = factory.createQueueConnection();
                
                    ctx.log("Create queue session");
                    qsession[i] = qconn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
                
                    ctx.log("Looking up the queue from JNDI");
                    queue[i] = (Queue) initialContext.lookup("jms/SampleQueue");
                }

                updateDB();
      
            }
            catch( Exception e ){
                ctx.log( "Exception caught in test code" );
                e.printStackTrace();
            }

       
        
        // end my code        
        
        // my code
        //createAccount();
        // end my code        
    }

    /**
     *  Tasks to be carried out in the READY_EVENT. 
     *  Logs a message.
     */
    private void onReadyTask() {
        ctx.log("LifecycleTopic: READY_EVENT");
    }

    /**
     *  Tasks to be carried out in the SHUTDOWN_EVENT. 
     *  Logs a message
     */
    private void onShutdownTask() {
        ctx.log("LifecycleTopic: SHUTDOWN_EVENT");
    }

    private void updateDB() {
        try {
            //Class.forName("com.inet.ora.OraDriver");
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            ////String url = "jdbc:inetora::wrx.india.sun.com:1521:dbsmpl1";
            String url = "jdbc:derby://localhost:1527/testdb;create=true;";
            java.sql.Connection con = DriverManager.getConnection(url,"dbuser", "dbpassword");
            String qry = "update lifecycle_test1 set status=1" ;
            con.createStatement().executeUpdate(qry);
            con.close();
        } catch(Exception e) {
           System.out.println("Error:" + e.getMessage());
        }
    }


    /**
     *  Tasks to be carried out in the TERMINATION_EVENT. 
     *  Log a message.
     */
    private void onTerminationTask() {
        ctx.log("LifecycleTopic: TERMINATION_EVENT");
    }

}
