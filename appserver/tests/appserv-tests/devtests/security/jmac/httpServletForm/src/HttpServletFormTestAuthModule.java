/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.s1asdev.security.jmac.httpservletform;

import java.util.Map;
import java.security.Principal;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.module.ServerAuthModule;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import sun.misc.BASE64Decoder;

public class HttpServletFormTestAuthModule implements ServerAuthModule {
    private static final String SAVED_REQUEST = "Saved_Request";
    private static final String SAVED_SUBJECT = "Saved_Subject";
    private CallbackHandler handler = null;
    private String pc = null;

    public void initialize(MessagePolicy requestPolicy,
               MessagePolicy responsePolicy,
               CallbackHandler handler,
               Map options)
               throws AuthException {
        this.handler = handler;
        if (options != null) {
            this.pc = (String)options.get("javax.security.jacc.PolicyContext");
        }
    }

    public Class[] getSupportedMessageTypes() {
        return new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    }

    public AuthStatus validateRequest(MessageInfo messageInfo,
                               Subject clientSubject,
                               Subject serviceSubject) throws AuthException {

        String username = null;
        String password = null;
        HttpServletRequest request =
            (HttpServletRequest)messageInfo.getRequestMessage();
        HttpServletResponse response =
            (HttpServletResponse)messageInfo.getResponseMessage();

        if (!isMandatory(messageInfo) &&
                !request.getRequestURI().endsWith("/j_security_check")) {
            return AuthStatus.SUCCESS;
        }

        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Subject savedClientSubject =
                        (Subject)session.getValue(SAVED_SUBJECT);
                if (savedClientSubject != null) {
                    System.out.println("already has saved subject");
                    // just copy principals for testing
                    clientSubject.getPrincipals().addAll(
                            savedClientSubject.getPrincipals());
                    request.setAttribute("MY_NAME", getClass().getName());
                    request.setAttribute("PC", pc);
                    return AuthStatus.SUCCESS;
                }
            }

            username = request.getParameter("j_username");
            password = request.getParameter("j_password");

            if (username == null || password == null) {
                System.out.println("forward to login form");
                if (session == null) {
                    session = request.getSession(true);
                }
                session.putValue(SAVED_REQUEST, new SavedRequest(request));
                RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
                rd.forward(request, response);
                System.out.println("Form: SEND_CONTINUE");
                return AuthStatus.SEND_CONTINUE;
            }

            char[] pwd = new char[password.length()];
            password.getChars(0, password.length(), pwd, 0);
            PasswordValidationCallback pwdCallback =
                new PasswordValidationCallback(clientSubject, username, pwd);
            CallerPrincipalCallback cpCallback =
                new CallerPrincipalCallback(clientSubject, username);
            System.out.println("Subject before invoking callbacks: " + clientSubject);
            handler.handle(new Callback[] { pwdCallback, cpCallback });
            System.out.println("Subject after invoking callbacks: " + clientSubject);

            if (pwdCallback.getResult()) {
                System.out.println("login success: " + username + ", " + password);
                SavedRequest sreq = null;
                if (session != null) {
                    sreq = (SavedRequest)session.getValue(SAVED_REQUEST);
                    // for testing only as Subject is not Serializable
                    session.putValue(SAVED_SUBJECT, clientSubject);
                }
                if (sreq != null) {
                    StringBuffer sb = new StringBuffer(sreq.getRequestURI());
                    if (sreq.getQueryString() != null) {
                        sb.append('?');
                        sb.append(sreq.getQueryString());
                    }
                    response.sendRedirect(
                           response.encodeRedirectURL(sb.toString()));
                    return AuthStatus.SEND_CONTINUE;
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return AuthStatus.SEND_FAILURE;
                }
            } else {
                System.out.println("login fails: " + username + ", " + password);
                RequestDispatcher rd = request.getRequestDispatcher("error.html");
                rd.forward(request, response);
                return AuthStatus.SEND_FAILURE;
            }
        } catch(Throwable t) {
            System.out.println("login fails: " + username + ", " + password);
            t.printStackTrace();
            RequestDispatcher rd = request.getRequestDispatcher("error.html");
            try {
                rd.forward(request, response);
            } catch(Exception ex) {
                AuthException ae = new AuthException();
                ae.initCause(ex);
                throw ae;
            }
            return AuthStatus.SEND_FAILURE;
        }
    }

    public AuthStatus secureResponse(MessageInfo messageInfo,
            Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject)
        throws AuthException {
    }


    private boolean isMandatory(MessageInfo messageInfo) {
        return Boolean.valueOf((String)messageInfo.getMap().get(
            "javax.security.auth.message.MessagePolicy.isMandatory"));
    }
}
