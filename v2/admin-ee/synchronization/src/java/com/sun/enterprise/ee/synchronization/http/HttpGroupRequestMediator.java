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
package com.sun.enterprise.ee.synchronization.http;

import com.sun.enterprise.ee.admin.servermgmt.DASPropertyReader;
import com.sun.enterprise.ee.synchronization.SynchronizationRequest;
import com.sun.enterprise.ee.synchronization.BaseGroupRequestMediator;
import com.sun.enterprise.ee.synchronization.tx.Transaction;
import com.sun.enterprise.util.i18n.StringManager;


/**
 * Executes a group of synchronization requests in one thread using web path.
 *
 * @author Nazrul Islam
 */
public class HttpGroupRequestMediator extends BaseGroupRequestMediator {

    /**
     * Constructor.
     *
     * @param  p  domain admin server info
     * @param  reqs  array of synchronization request
     * @param  tx  transaction associated with this execution
     * @param  url synchronization servlet url
     */
    public HttpGroupRequestMediator(DASPropertyReader p, 
            SynchronizationRequest[] reqs, Transaction tx, String url) {
            
        _tx = tx;
        _mediators = new HttpRequestMediator[reqs.length];

        if (url == null) {
            String msg = _localStrMgr.getString("synchronizationUrlIsNull");
            throw new IllegalArgumentException();
        }

        for (int i=0; i<reqs.length; i++) {
            _mediators[i] = new HttpRequestMediator(p, reqs[i], tx, url);
        }
    }

    // ---- PRIVATE VARIABLES ---------------------------------------
    private static final StringManager _localStrMgr = 
                StringManager.getManager(HttpGroupRequestMediator.class);
}
