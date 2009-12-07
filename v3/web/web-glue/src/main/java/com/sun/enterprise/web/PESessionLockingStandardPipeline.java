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

package com.sun.enterprise.web;

import java.io.IOException;
import java.util.logging.Level;
import javax.servlet.ServletException;
import com.sun.logging.LogDomains;
import org.apache.catalina.*;
import org.apache.catalina.session.StandardSession;

/**
 *
 * @author Larry White
 */
public class PESessionLockingStandardPipeline extends WebPipeline {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final java.util.logging.Logger _logger =
        LogDomains.getLogger(PESessionLockingStandardPipeline.class, LogDomains.WEB_LOGGER);
    
    /** 
     * creates an instance of PESessionLockingStandardPipeline
     * @param container
     */       
    public PESessionLockingStandardPipeline(Container container) {
        super(container);
    }    
    
    /**
     * Cause the specified request and response to be processed by the Valves
     * associated with this pipeline, until one of these valves causes the
     * response to be created and returned.  The implementation must ensure
     * that multiple simultaneous requests (on different threads) can be
     * processed through the same Pipeline without interfering with each
     * other's control flow.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception is thrown
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {
        
        Session sess = request.lockSession();
        try {
            super.invoke(request, response);
        } finally {
            request.unlockSession();
        }
    }    
}
