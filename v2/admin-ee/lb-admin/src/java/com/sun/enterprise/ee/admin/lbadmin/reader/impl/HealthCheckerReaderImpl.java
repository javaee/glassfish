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
package com.sun.enterprise.ee.admin.lbadmin.reader.impl;

import com.sun.enterprise.ee.admin.lbadmin.transform.Visitor;
import com.sun.enterprise.ee.admin.lbadmin.transform.HealthCheckerVisitor;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.HealthCheckerReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;
import com.sun.enterprise.config.serverbeans.HealthChecker;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Provides health checker information relavant to Load balancer tier.
 *
 * @author Satish Viswanatham
 */
public class HealthCheckerReaderImpl implements HealthCheckerReader {

    /**
     * Constructor 
     */
    public HealthCheckerReaderImpl(HealthChecker hc) {
        if (hc == null) {
            String msg = _localStrMgr.getString("ConfigBeanAndNameNull");
            throw new IllegalArgumentException(msg);
        }
        _hc = hc;
    }

    /**
     * Return health checker url
     *
     * @return String           health checker url, it shoudld conform to
     *                          RFC 2396. java.net.URI.resolve(url) shoudl
     *                          return a valid URI.
     */
    public String getUrl() throws LbReaderException {
        return _hc.getUrl();
    }

    /**
     * Health checker runs in the specified interval time.
     *
     * @return String           value must be > 0
     */
    public String getIntervalInSeconds() throws LbReaderException {
        return _hc.getIntervalInSeconds();
    }

    /**
     *  Timeout where a server is considered un healthy.
     *
     * @return String           value must be > 0
     */
    public String getTimeoutInSeconds() throws LbReaderException{
        return _hc.getTimeoutInSeconds();
    }

    // --- VISITOR IMPLEMENTATION ---

    public void accept(Visitor v) {

        HealthCheckerVisitor pv = (HealthCheckerVisitor) v;
        pv.visit(this);
    }

    //--- PRIVATE VARIABLES ------
    HealthChecker _hc = null;
    private static final StringManager _localStrMgr = 
               StringManager.getManager(HealthCheckerReaderImpl.class);

}
