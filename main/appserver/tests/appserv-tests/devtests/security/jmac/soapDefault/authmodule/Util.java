/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.jmac.soapdefault;

import java.io.IOException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

class Util {
    static String getValue(SOAPMessage message) throws SOAPException {
        SOAPBody body = message.getSOAPBody();
        SOAPElement paramElement =
                (SOAPElement)body.getFirstChild().getFirstChild();
        return paramElement.getValue();
    }

    static void prependSOAPMessage(SOAPMessage message, String prefix)
            throws IOException, SOAPException {
        //message.writeTo(System.out); System.out.println();
        SOAPBody body = message.getSOAPBody();
        SOAPElement paramElement =
                (SOAPElement)body.getFirstChild().getFirstChild();
        paramElement.setValue(prefix + paramElement.getValue());
    }
}
