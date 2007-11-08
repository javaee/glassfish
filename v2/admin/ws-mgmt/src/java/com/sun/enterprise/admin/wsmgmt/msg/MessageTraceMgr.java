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
package com.sun.enterprise.admin.wsmgmt.msg;

import java.util.Map;
import java.util.List;
import java.util.Hashtable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigFactory;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigProvider;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Backend facade for the SOAP message visualization. 
 */
public class MessageTraceMgr {

    /**
     * Returns the singleton instance of this class. 
     *
     * @return  singleton instance of this class
     */
    public static MessageTraceMgr getInstance() {
        return _instance;
    }

    /**
     * Private constructor.
     */
    private MessageTraceMgr() {
        _applications  = new Hashtable();

    } 

    /**
     * Initializes the message visualization service.
     */
    public void init() {
        try {
            ConfigFactory cf = ConfigFactory.getConfigFactory();
            ConfigProvider cp = cf.getConfigProvider();
            List list = cp.getManagedWebserviceApplicationIds();
            for (Iterator iter=list.iterator(); iter.hasNext();) {
                String appId = (String) iter.next();
                try {
                    ApplicationMediator am = new ApplicationMediator(appId);
                    _applications.put(appId, am);
                } catch (MessageTraceException me) {
                    String msg="Initialization error for application: " + appId;
                    _logger.log(Level.FINE, msg, me);
                }
            }
        } catch (Exception e) {
            String msg="Configuration initialization error.";
            _logger.log(Level.FINE, msg, e);
        }
    }

    /**
     * Disables message trace for the given web service endpoint. This 
     * method is called to dynamically reconfigure the size.
     *
     * @param  appId  name of the application
     * @param  endpoint partially qualified endpoint name 
     * 
     * @throws MessageTraceException  if application id is invalid
     */
    public void disable(String appId, String endpoint) 
            throws MessageTraceException {

        ApplicationMediator am = (ApplicationMediator) _applications.get(appId);
        if (am != null) {
            am.disable(endpoint);
            if (am.isEmpty()) {
                _applications.remove(appId);
            }
        } else {
            String msg = _stringMgr.getString("MessageTraceMgr_InvalidAppEx", 
                                    appId, endpoint);
            throw new MessageTraceException(msg);
        }
    }

    /**
     * Enables message visualization for the given webservice endpoint.
     *
     * @param  appId  name of the application
     * @param  endpoint partially qualified endpoint name 
     * @param  size  max size of messages in history
     *
     * @throws MessageTraceException if a configuration initialization exception
     */
    public void enable(String appId, String endpoint, int size) 
            throws MessageTraceException {

        ApplicationMediator am = (ApplicationMediator) _applications.get(appId);
        if (am != null) {
            am.enable(endpoint, size);
        } else {
            am = new ApplicationMediator(appId);
            am.enable(endpoint, size);
            _applications.put(appId, am);
        }
    }

    /**
     * Sets the number of messages stored in memory. This method is 
     * called to dynamically reconfigure the size.
     *
     * @param  appId  name of the application
     * @param  endpoint partially qualified endpoint name 
     * @param  size  number of message stored in memory
     *
     * @throws MessageTraceException  if an invalid application id
     */
    public void setMessageHistorySize(String appId, String endpoint, int size) 
            throws MessageTraceException {

        ApplicationMediator am = (ApplicationMediator) _applications.get(appId);
        if (am != null) {
            am.setMessageHistorySize(endpoint, size);
        } else {
            String msg = _stringMgr.getString("MessageTraceMgr_InvalidAppEx", 
                                    appId, endpoint);
            throw new MessageTraceException(msg);
        }
    }

    /**
     * Shuts down message visualization.
     */
    public void stop() {
        Collection mediators = _applications.values();
        for (Iterator itr=mediators.iterator(); itr.hasNext();) {
            ApplicationMediator am = (ApplicationMediator) itr.next();
            am.destroy();
        }
        _applications.clear();
    }

    /**
     * Returns the available messages.
     *
     * @return  array of message trace objects currently available
     */
    public MessageTrace[] getMessages() {
        Collection c = new ArrayList();

        Collection mediators = _applications.values();
        for (Iterator itr=mediators.iterator(); itr.hasNext();) {
            ApplicationMediator am = (ApplicationMediator) itr.next();
            c.addAll( am.getMessages() );
        }

        MessageTrace[] trace = new MessageTrace[c.size()];
        return ((MessageTrace[]) c.toArray(trace));
    }

    /**
     * Returns messages for a web service endpoint.
     *
     * @param  appId  name of the application
     * @param  endpoint partially qualified endpoint name 
     *
     * @return  messages for the given endpoint
     */
    public MessageTrace[] getMessages(String appId, String endpoint) {
        Collection c = null;
        ApplicationMediator am = (ApplicationMediator) _applications.get(appId);
        if (am != null) {
            c = am.getMessages(endpoint);
        }
        MessageTrace[] trace = null;
        if ( c != null) {
            trace = new MessageTrace[c.size()];
            return ((MessageTrace[]) c.toArray(trace));
        } else {
            return null;
        }
    }

    // ---- VARIABLES - PRIVATE ---------------------------------------
    private Map _applications                = null;
    private static final MessageTraceMgr _instance = new MessageTraceMgr();
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(MessageTraceMgr.class);

}
