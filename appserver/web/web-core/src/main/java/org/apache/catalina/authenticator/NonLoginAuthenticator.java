/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.authenticator;


import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation that checks
 * only security constraints not involving user authentication.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2007/03/05 20:42:26 $
 */

public final class NonLoginAuthenticator
    extends AuthenticatorBase {


    // ----------------------------------------------------- Instance Variables

    //START SJSAS 6202703
    /**
     * Principal name of nonlogin principal.
     */
    private static final String NONLOGIN_PRINCIPAL_NAME = "nonlogin-principal";
    
    /**
     * A dummy principal that is set to request in authenticate method.
     */
    private final GenericPrincipal NONLOGIN_PRINCIPAL =
        new GenericPrincipal(NONLOGIN_PRINCIPAL_NAME, (char[]) null, 
                            (java.util.List<String>) null);
    //END SJSAS 6202703
    
    /**
     * Descriptive information about this implementation.
     */
    private static final String info =
        "org.apache.catalina.authenticator.NonLoginAuthenticator/1.0";


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    @Override
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Authenticate the user making this request, based on the specified
     * login configuration.  Return <code>true</code> if any specified
     * constraint has been satisfied, or <code>false</code> if we have
     * created a response challenge already.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config Login configuration describing how authentication
     * should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public boolean authenticate(HttpRequest request,
                                HttpResponse response,
                                LoginConfig config)
        throws IOException {

        if (debug >= 1)
            log("User authentication is not required");
        
        //START SJSAS 6202703
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        if (hreq.getUserPrincipal() == null) {
            request.setUserPrincipal(NONLOGIN_PRINCIPAL);
        }
        //END SJSAS 6202703
        return (true);


    }

    /**
     * Return the authentication method, which is vendor-specific and
     * not defined by HttpServletRequest.
     */
    @Override
    protected String getAuthMethod() {
        return "NONE";
    }
}
