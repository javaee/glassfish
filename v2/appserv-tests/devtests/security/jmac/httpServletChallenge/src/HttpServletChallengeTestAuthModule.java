/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.jmac.httpservletchallenge;

import java.util.Map;
import java.security.AccessController;
import java.security.PrivilegedAction;
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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import sun.misc.BASE64Decoder;

public class HttpServletChallengeTestAuthModule implements ServerAuthModule {
    private CallbackHandler handler = null;

    public void initialize(MessagePolicy requestPolicy,
               MessagePolicy responsePolicy,
               CallbackHandler handler,
               Map options)
               throws AuthException {
        this.handler = handler;
    }

    public Class[] getSupportedMessageTypes() {
        return new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    }

    public AuthStatus validateRequest(MessageInfo messageInfo,
                               Subject clientSubject,
                               Subject serviceSubject) throws AuthException {

        if (!isMandatory(messageInfo)) {
            return AuthStatus.SUCCESS;
        }

        String username = null;
        String password = null;
        try {
            
            HttpServletRequest request =
                (HttpServletRequest)messageInfo.getRequestMessage();
            String authorization = request.getHeader("authorization");
            if (authorization != null && 
                    authorization.toLowerCase().startsWith("basic ")) {
                authorization = authorization.substring(6).trim();
                BASE64Decoder decoder = new BASE64Decoder();
                byte[] bs = decoder.decodeBuffer(authorization);
                String decodedString = new String(bs);
                int ind = decodedString.indexOf(':');
                if (ind > 0) {
                    username = decodedString.substring(0, ind);
                    password = decodedString.substring(ind + 1);
                }
            }

            HttpServletResponse response =
                     (HttpServletResponse)messageInfo.getResponseMessage();
            if (username == null || password == null) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"default\"");  
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                System.out.println("login prompt for username/password");
                return AuthStatus.SEND_CONTINUE;
            }

            HttpSession session = request.getSession(false);
            boolean secondPhase = (session != null &&
                    session.getValue("FIRST_DONE") != null);
            String loginName = ((secondPhase)? username + "_2" : username);
            char[] pwd = new char[password.length()];
            password.getChars(0, password.length(), pwd, 0);
            Callback[] callbacks;
            PasswordValidationCallback pwdCallback =
                new PasswordValidationCallback(clientSubject, loginName, pwd);
            if (secondPhase) {
                CallerPrincipalCallback cpCallback =
                    new CallerPrincipalCallback(clientSubject, username);
                callbacks = new Callback[] { pwdCallback, cpCallback };
            } else {
                callbacks = new Callback[] { pwdCallback };
            }
            System.out.println("Subject before invoking callbacks: " + clientSubject);
            handler.handle(callbacks);
            System.out.println("Subject after invoking callbacks: " + clientSubject);

            if (pwdCallback.getResult()) {
                System.out.print("login success: " + username + ", " + password);
                if (secondPhase) {
                    System.out.println(" for second level");
                    request.setAttribute("MY_NAME", getClass().getName());
                    messageInfo.getMap().put("javax.servlet.http.authType", "MC");
                    return AuthStatus.SUCCESS;
                } else {
                    System.out.println(" for first level");
                    response.setHeader("WWW-Authenticate", "Basic realm=\"default\"");
                    if (session == null) {
                        session = request.getSession(true);
                        session.putValue("FIRST_DONE", Boolean.TRUE);
                    }
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return AuthStatus.SEND_SUCCESS;
                }
            } else {
                System.out.println("login fails: " + username + ", " + password);
                return AuthStatus.SEND_FAILURE;
            }
        } catch(Throwable t) {
            System.out.println("login fails: " + username + ", " + password);
            t.printStackTrace();
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
