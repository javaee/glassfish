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
package com.sun.enterprise.ee.synchronization;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;


/**
 * Encapsulates all synchronization requests for an application or 
 * stand alone module.
 *
 * @author Nazrul Islam
 */
public class ApplicationSynchRequest implements Serializable {

    SynchronizationRequest getApplicationRequest() {
        return _applicationRequest;
    }

    SynchronizationRequest getEJBRequest() {
        return _ejbRequest;
    }

    SynchronizationRequest getXMLRequest() {
        return _xmlRequest;
    }

    SynchronizationRequest getPolicyRequest() {
        return _policyRequest;
    }

    SynchronizationRequest getJSPRequest() {
        return _jspRequest;
    }

    SynchronizationRequest getAppLibsRequest() {
        return _applibsRequest;
    }

    SynchronizationRequest getJwsRequest() {
        return _jwsRequest;
    }

    void setApplicationRequest(SynchronizationRequest req) {
        _applicationRequest = req;
    }

    void setEJBRequest(SynchronizationRequest req) {
        _ejbRequest = req;
    }

    void setXMLRequest(SynchronizationRequest req) {
        _xmlRequest = req;
    }

    void setPolicyRequest(SynchronizationRequest req) {
        _policyRequest = req;
    }

    void setJSPRequest(SynchronizationRequest req) {
        _jspRequest = req;
    }

    void setAppLibsRequest(SynchronizationRequest req) {
        _applibsRequest = req;
    }

    void setJwsRequest(SynchronizationRequest req) {
        _jwsRequest = req;
    }

    private void setTS(SynchronizationRequest req) {

        File tsFile = req.getCacheTimestampFile();
        long modTime = 0;
        BufferedReader is = null;
        try {
            if (tsFile != null) {
                is = new BufferedReader(new FileReader(tsFile));
                modTime = Long.parseLong(is.readLine());
                is.close();
                is = null;
            }
        } catch (Exception e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) { }
            }
        }

        req.setTimestamp(modTime);
    }

    public List toSynchronizationRequest() {
        ArrayList list = new ArrayList();
        if (_ejbRequest != null) {
            setTS(_ejbRequest);
            list.add(_ejbRequest);
        }
        if (_xmlRequest != null) {
            setTS(_xmlRequest);
            list.add(_xmlRequest);
        }
        if (_policyRequest != null) {
            setTS(_policyRequest);
            list.add(_policyRequest);
        }
        if (_jspRequest != null) {
            setTS(_jspRequest);
            list.add(_jspRequest);
        }
        if (_applicationRequest != null) {
            setTS(_applicationRequest);
            list.add(_applicationRequest);
        }
        if (_applibsRequest != null) {
            setTS(_applibsRequest);
            list.add(_applibsRequest);
        }
        if (_jwsRequest != null) {
            setTS(_jwsRequest);
            list.add(_jwsRequest);
        }

        return list;
    }

    // ---- INSTANCE VARIABLE - PRIVATE ---------------------------
    SynchronizationRequest _ejbRequest          = null;
    SynchronizationRequest _xmlRequest          = null;
    SynchronizationRequest _policyRequest       = null;
    SynchronizationRequest _jspRequest          = null;
    SynchronizationRequest _applicationRequest  = null;
    SynchronizationRequest _applibsRequest      = null;
    SynchronizationRequest _jwsRequest          = null;
}
