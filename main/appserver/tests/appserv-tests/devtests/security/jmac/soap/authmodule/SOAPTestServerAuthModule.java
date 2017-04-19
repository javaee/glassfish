/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.jmac.soap;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import javax.xml.soap.SOAPMessage;

public class SOAPTestServerAuthModule implements ServerAuthModule {
    private CallbackHandler handler = null;

    public void initialize(MessagePolicy requestPolicy,
               MessagePolicy responsePolicy,
               CallbackHandler handler,
               Map options)
               throws AuthException {
        this.handler = handler;
    }

    public Class[] getSupportedMessageTypes() {
        return new Class[] { SOAPMessage.class };
    }

    public AuthStatus validateRequest(MessageInfo messageInfo,
            Subject clientSubject,
            Subject serviceSubject) throws AuthException {
        SOAPMessage reqMessage = (SOAPMessage)messageInfo.getRequestMessage();
        try {
            String value = Util.getValue(reqMessage);
            if (value == null || !value.startsWith("SecReq ")) {
                return AuthStatus.FAILURE;
            }
            Util.prependSOAPMessage(reqMessage, "ValReq ");
        } catch(Exception ex) {
            AuthException aex = new AuthException();
            aex.initCause(ex);
            throw aex;
        }
        return AuthStatus.SUCCESS;
    }

    public AuthStatus secureResponse(MessageInfo messageInfo,
            Subject serviceSubject) throws AuthException {
        SOAPMessage respMessage = (SOAPMessage)messageInfo.getResponseMessage();
        try {
            Util.prependSOAPMessage(respMessage, "SecResp ");
        } catch(Exception ex) {
            AuthException aex = new AuthException();
            aex.initCause(ex);
            throw aex;
        }
        return AuthStatus.SUCCESS;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject)
        throws AuthException {
    }
}
