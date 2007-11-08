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

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;


/**
 * <code>ListenerManager</code> interface allows plugin to 
 * register listener on a specific server.
 * 
 * <code>addNotificationListener</code> method is used to register the listener.
 * Notifications are filtered based on <code>NotificationFilter</code> parameter
 * to this method. When NotificationFilter parameter is null, default filter is 
 * used. Default filter is specified using notification.xml. Listener is registered
 * on the server delegate object of the specified server.
 */
public interface ListenerManager {
   
   /**
    * Adds notification listener for a given Server.
    * 
    * @param server     the name of the server on which the listener should be added
    * @param domain     the name of the application server domain
    * @param listener   the listener object which will handle the notifications emitted by the registered server delegate MBean
    * @param filter     the filter object. If filter is null, default filtering will be performed before handling notifications.
    *                   Default filtering is specified through notification.xml.
    * @param handback   the context to be sent to the listener when a notification is emitted
    *
    * @throws InstanceNotFoundException the registered delegate MBean not found for the given server
    * @throws IOException a communication problem occurred when talking to the MBean server
    */
   public void addNotificationListener(String server, String domain, NotificationListener listener, 
                NotificationFilter filter, Object handback) throws InstanceNotFoundException,
                IOException;


   /**
    * Removes notification listener for the given Server.
    * There must be a registered listener on the given Server that exactly 
    * matches the given filter, and handback parameters. If there is more
    * than one such listener, only one is removed.
    * 
    * @param server     the name of the Server on which the listener should be removed.
    * @param domain     the domain name of the application server
    * @param listener   a listener that was previously added for this server
    * @param filter     the filter that was specified when the listener was added
    * @param handback   the handback that was specified when the listener was added
    *
    * @throws InstanceNotFoundException the server provided does not have delegate
    * MBean registered for it
    * ListenerNotFoundException  the listener is not registered for the given server.
    * IOException  a communication problem occurred when talking to the MBean server.
    */
    public void removeNotificationListener(String server, String domain, NotificationListener listener,
            NotificationFilter filter, Object handback) throws InstanceNotFoundException,
            ListenerNotFoundException, IOException;


    /**
    * Removes notification listener for the given Server.
    * If the listener is registered more than once, perhaps with different filters
    * or callbacks, this method will remove all those registrations.
    * 
    * @param server     the name of the Server on which the listener should be removed.
    * @param domain     the domain name of the application server
    * @param listener   a listener that was previously added for this server
    *
    * @throws InstanceNotFoundException the server provided does not have delegate
    * MBean registered for it
    * ListenerNotFoundException  the listener is not registered for the given server or
    * it is not registered with the given filter and handback
    * IOException  a communication problem occurred when talking to the MBean server.
    */
    public void removeNotificationListener(String server, String domain, NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException, IOException;

}
