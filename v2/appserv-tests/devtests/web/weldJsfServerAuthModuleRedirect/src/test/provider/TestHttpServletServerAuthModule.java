/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package test.provider;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("rawtypes")
public class TestHttpServletServerAuthModule implements ServerAuthModule {

    /**
     * This is the URI of the action which will trigger a redirect.
     */
    private static final String LOGIN_ACTION_URI = "/samlogin";

    /**
     * This must point to a JSF Facelets page to trigger the bug.
     */
    private static final String REDIRECT_FACES_URI = "/message.xhtml";

    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    }

    @Override
    public void initialize(final MessagePolicy reqPolicy, final MessagePolicy resPolicy, final CallbackHandler cBH,
            final Map opts) throws AuthException {
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject)
            throws AuthException {
        try {
            final HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
            final HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

            if (!request.getRequestURI().endsWith(LOGIN_ACTION_URI)) {
                return AuthStatus.SUCCESS;
            }

            request.getRequestDispatcher(REDIRECT_FACES_URI).forward(request, response);
            return AuthStatus.SEND_CONTINUE;
        } catch (Throwable e) {
            AuthException authException = new AuthException();
            authException.initCause(e);
            throw authException;
        }
    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return AuthStatus.SEND_SUCCESS;
    }

}
