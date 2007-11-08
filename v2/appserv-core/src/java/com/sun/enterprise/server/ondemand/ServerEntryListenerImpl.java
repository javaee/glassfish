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

package com.sun.enterprise.server.ondemand;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.server.ondemand.entry.*;

/**
 * Concrete implementation of ServerEntryListener. This directs the
 * entry to servicegroups.
 *
 * @author Binod PG
 * @see ServiceGroup
 */
public class ServerEntryListenerImpl implements ServerEntryListener {

    private OnDemandServer server = null;
    private ArrayList listeners = new ArrayList();

    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    
    public ServerEntryListenerImpl(OnDemandServer server) {
        this.server  = server;
    }

    /**
     * If atleast one servicegroup that need to be started is interested 
     * in the entry context, return false. Otherwise return true.
     */
    public boolean isNotified(EntryContext context) {
        context.setServerContext(server.getServerContext());
        return this.server.getServiceGroup().isNotified(context);
    }

    /**
     * Notify the main servicegroup about the server entry. If ondemand 
     * initialization is switched off, no entry event (other than the 
     * one special STARTUP event) will be processed.
     */
    public void notifyEntry(EntryContext context) {
        if (server.isOnDemandOff() == true && 
            context.getEntryPointType() != EntryPoint.STARTUP) {
            return;
        }
        context.setServerContext(server.getServerContext());
        try {
            this.server.getServiceGroup().start(context);
        } catch (Exception e) {
            _logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ServerEntryListener listener = (ServerEntryListener) it.next();
            listener.notifyEntry(context);
        }
    }

    /**
     * If some other part of the code want to listen to the events this is
     * a helper methods that can be used to add a listener.
     */
    public void addServerEntryListener(ServerEntryListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the list.
     */
    public void removeServerEntryListener(ServerEntryListener listener) {
        listeners.remove(listener);
    }
}
