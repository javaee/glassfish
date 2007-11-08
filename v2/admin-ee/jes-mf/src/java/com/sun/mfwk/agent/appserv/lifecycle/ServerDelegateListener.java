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
package com.sun.mfwk.agent.appserv.lifecycle;

import java.awt.*;

import java.io.*;
import java.net.ConnectException;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import javax.management.InstanceNotFoundException;
import javax.management.*;


/**
 * Class used by ListenerManagerImpl to implement its APIs. Listens to specific
 * mbean on the Server.
 */
class ServerDelegateListener extends AbstractListener {

   /**
    * Constructs the ServerDelegateListener object to listen to notifications 
    * from the given server for the given client.
    * 
    * @param connection the mbean server connection to listen to
    * @param client     the client for which to listen to
    * @param filter     the object to filter out unwanted notifcations 
    * @param handback   the object which is sent back along to the listener
    *                   along with the notification without any modifications
    */
    ServerDelegateListener(MBeanServerConnection connection, String server, 
        String domain, NotificationListener client, NotificationFilter filter, 
            Object handback) {
        super(connection, server, domain, client, filter, handback);
    }


   /**
    * Specifies the registered object to listen to.
    */
    protected String getObjectNameString() {
	return MBEAN_SERVER_DELEGATE;
    }


    protected final static String	MBEAN_SERVER_DELEGATE	=
        "JMImplementation:type=MBeanServerDelegate";                    //NOI18N
}
