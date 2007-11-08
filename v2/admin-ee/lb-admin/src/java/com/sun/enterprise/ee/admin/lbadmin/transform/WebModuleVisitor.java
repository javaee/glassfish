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
package com.sun.enterprise.ee.admin.lbadmin.transform;

import com.sun.enterprise.ee.admin.lbadmin.reader.api.BaseReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.LbReaderException;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.ClusterReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.InstanceReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.WebModuleReader;
import com.sun.enterprise.ee.admin.lbadmin.reader.api.IdempotentUrlPatternReader;

import com.sun.enterprise.ee.admin.lbadmin.beans.Cluster;
import com.sun.enterprise.ee.admin.lbadmin.beans.WebModule;
import org.netbeans.modules.schema2beans.AttrProp;


/**
 * Provides transform capabilites for web module
 *
 * @author Satish Viswanatham
 */
public class WebModuleVisitor implements Visitor {

    // ------ CTOR ------
    public WebModuleVisitor(WebModule w, Cluster c) {
        _w = w;
        _c = c;
    }

    /**
     * Visit reader class 
     */
    public void visit(BaseReader br) {
        // FIXME, make as assert here about no class cast exception
        WebModuleReader wRdr = (WebModuleReader) br; 

        try {
            _w.setContextRoot(wRdr.getContextRoot());
        } catch (LbReaderException le) {
            // XXX ignore
        }

        try {
            String url = wRdr.getErrorUrl();
            if (( url != null) && ( !"".equals(url))) {
                // XXX start of bug fix for 6171814
                _c.createAttribute(Cluster.WEB_MODULE,"error-url", "ErrorUrl", 
						AttrProp.CDATA,
						null, "");
                // XXX end of bug fix for 6171814
                _w.setErrorUrl(wRdr.getErrorUrl());
            }
        } catch (LbReaderException le) {
            // XXX ignore
        }

        try {
            _w.setEnabled(Boolean.toString(wRdr.getLbEnabled()));
        } catch (LbReaderException le) {
            // XXX ignore
        }

        try {
            _w.setDisableTimeoutInMinutes(wRdr.getDisableTimeoutInMinutes());
        } catch (LbReaderException le) {
            // XXX ignore
        }

        IdempotentUrlPatternReader[] iRdrs = null;
        try {
            iRdrs = wRdr.getIdempotentUrlPattern();
        } catch (LbReaderException le) {
            // XXX ignore
        }

        if ((iRdrs != null) &&  (iRdrs.length > 0 ) ){
            for(int i=0; i < iRdrs.length; i++) {
                iRdrs[i].accept(new IdempotentUrlPatternVisitor(_w, i));
            }
        }

    }

    //--- PRIVATE VARS ----

    WebModule _w = null;
    Cluster _c = null;
}
