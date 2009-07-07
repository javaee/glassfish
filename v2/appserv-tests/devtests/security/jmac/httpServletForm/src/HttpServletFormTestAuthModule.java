/*
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
