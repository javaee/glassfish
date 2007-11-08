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
package com.sun.enterprise.ee.admin.event;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.admin.event.AdminEvent;
import com.sun.enterprise.admin.event.AdminEventResult;

/**
 * Responsible for forwarding an event to a set of end points.
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
class DispatchMgr {

    /**
     * Constructor.
     *
     * @param  e  event to be sent
     * @param  destinations  array of end point applicable for this event
     */
    DispatchMgr(AdminEvent e, EndPoint[] destinations) {

        assert(destinations != null);
        assert(e != null);

        _event = e;
        _info  = destinations;
    }

    private static Logger getLogger() {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }

    /**
     * Forwards the events to the targetted end points.
     *
     * @return  result from each end point
     */
    synchronized AdminEventResult forward() {
        
//        AdminEventResult[] results = null;

        AdminEventResult retResult = null;
        try {
//            results = new AdminEventResult[_info.length];
            retResult = new AdminEventResult(_event.getSequenceNumber());
            EndPointHandler[] handlers = new EndPointHandler[_info.length];
            Thread[] eThreads = new Thread[_info.length];
            AdminEvent[] cloneEvents = new AdminEvent[_info.length];

            // make clones of the event
            for (int i=0; i<_info.length; i++) {
                cloneEvents[i] = (AdminEvent) _event.clone();
            }

            for (int i=0; i<_info.length; i++) {
                assert(_info[i].getHost() != null);

                cloneEvents[i].setEffectiveDestination( _info[i].getHost() );

                handlers[i] = new EndPointHandler(cloneEvents[i], _info[i]);
                eThreads[i] = new Thread(handlers[i], HANDLER_NAME+i);
                eThreads[i].start();
            }

            int j =0;
            for (int i=0; i<_info.length; i++) {

                eThreads[i].join(DEFAULT_TIME_OUT);

                retResult.addEventResult(_info[i].getHost(),
                                    handlers[i].getResult());
                AdminEventResult r = handlers[i].getResult();
                if (( r == null) || (r != null && 
                    !r.getResultCode().equals(AdminEventResult.SUCCESS))) {
                    j++;
               }
            }
            if ( j == 0 ) {
                retResult.setResultCode(AdminEventResult.SUCCESS);
            } else {
                if ( j == _info.length) {
                    retResult.setResultCode(AdminEventResult.ERROR);
                } else {
                    retResult.setResultCode(AdminEventResult.MIXED_RESULT);
                }
            }
        } catch (Exception e) {
            // fatal error - this should never happen
            getLogger().log(Level.WARNING, "eeadmin.dispatchmgr.exception",e); 
            if (retResult != null) {
                retResult.setResultCode(retResult.ERROR);
                if (_event != null) {
                    retResult.addException(_event.getTargetDestination(), e);
                }
            }
        }

        return retResult;
    }


    // ---- VARIABLE(S) - PRIVATE ----------------------------
    private AdminEvent _event                   = null;
    private EndPoint[] _info                    = null;
    private static final String HANDLER_NAME    = "event-handler-";
    private static final long DEFAULT_TIME_OUT  = 3600000;
    private static Logger _logger     = null;             
}
