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

/** See 6517284 on bugs.sun.com
 *  for the details of the reason why this class exists.
 */
package com.sun.enterprise.admin.server.core;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.admin.event.ClusterEvent;
import com.sun.enterprise.admin.event.ClusterEventListener;
import com.sun.enterprise.admin.event.ElementChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
    
public class NoopClusterEventListener implements ClusterEventListener {
    private static final Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
    
    public void processEvent (ElementChangeEvent event) throws AdminEventListenerException {
        final String msg = (event == null) ? 
            "null event" : "ignoring receieved event: " + event.getElementId() + " xpath: " + event.getElementXPath();
        finest(msg);
    }
    public void handleCreate(ClusterEvent event) throws AdminEventListenerException {
        final String msg = (event == null) ? 
            "null event" : "ignoring receieved cluster create event: " + event.getElementId() + " xpath: " + event.getElementXPath();
        finest(msg);
    }
    public void handleDelete(ClusterEvent event) throws AdminEventListenerException {
        final String msg = (event == null) ? 
            "null event" : "ignoring receieved cluster delete event: " + event.getElementId() + " xpath: " + event.getElementXPath();
        finest(msg);
    }
    public void handleUpdate(ClusterEvent event) throws AdminEventListenerException {
        final String msg = (event == null) ? 
            "null event" : "ignoring receieved cluster update event: " + event.getElementId() + " xpath: " + event.getElementXPath();
        finest(msg);
    }
    private void finest(final String msg) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest(msg);
        }
    }
}
