/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.  U.S.
 * Government Rights - Commercial software.  Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.  Use is subject
 * to license terms.
 *
 * This distribution may include materials developed by third parties.
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and
 * other countries.
 *
 * Copyright (c) 2003 Sun Microsystems, Inc. Tous droits reserves.
 *
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * supplements a celles-ci.  Distribue par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants developpes par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques deposees de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */
package com.sun.cb;

import javax.xml.soap.*;
import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.transform.*;

import java.util.*;
import java.io.*;

public class ConfirmationServlet extends HttpServlet {
    static MessageFactory fac = null;
    
    static {
        try {
            fac = MessageFactory.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void init(ServletConfig servletConfig) 
            throws ServletException {
        super.init(servletConfig);
    }
    
    public void doPost( HttpServletRequest req, 
        HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // Get all the headers from the HTTP request
            MimeHeaders headers = getHeaders(req);

            // Get the body of the HTTP request
            InputStream is = req.getInputStream();

            // Now internalize the contents of a HTTP request
            // and create a SOAPMessage
            SOAPMessage msg = fac.createMessage(headers, is);
      
            SOAPMessage reply = null;
            reply = onMessage(msg);

            if (reply != null) {
                
                /* 
                 * Need to call saveChanges because we're 
                 * going to use the MimeHeaders to set HTTP 
                 * response information. These MimeHeaders
                 * are generated as part of the save.
                 */
                if (reply.saveRequired()) {
                    reply.saveChanges(); 
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                putHeaders(reply.getMimeHeaders(), resp);
                    
                // Write out the message on the response stream
                OutputStream os = resp.getOutputStream();
                reply.writeTo(os);
                os.flush();
            } else {
                resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Exception ex) {
            throw new ServletException("SAAJ POST failed: " + 
                ex.getMessage());
        }
    }

    static MimeHeaders getHeaders(HttpServletRequest req) {

        Enumeration enum = req.getHeaderNames();
        MimeHeaders headers = new MimeHeaders();

        while (enum.hasMoreElements()) {
            String headerName = (String)enum.nextElement();
            String headerValue = req.getHeader(headerName);

            StringTokenizer values =
                new StringTokenizer(headerValue, ",");
            while (values.hasMoreTokens()) {
                headers.addHeader(headerName, 
                    values.nextToken().trim());
            }
        }
        return headers;
    }

    static void putHeaders(MimeHeaders headers, 
            HttpServletResponse res) {

        Iterator it = headers.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader header = (MimeHeader)it.next();

            String[] values = headers.getHeader(header.getName());
            if (values.length == 1) {
                res.setHeader(header.getName(), 
                    header.getValue());
            } else {
                StringBuffer concat = new StringBuffer();
                int i = 0;
                while (i < values.length) {
                    if (i != 0) {
                        concat.append(',');
                    }
                    concat.append(values[i++]);
                }
                res.setHeader(header.getName(), concat.toString());
            }
        }
    }

    // This is the application code for handling the message.

    public SOAPMessage onMessage(SOAPMessage message) {

        SOAPMessage confirmation = null;

        try {
            // Retrieve orderID from message received
            SOAPBody sentSB = 
                message.getSOAPPart().getEnvelope().getBody();
            Iterator sentIt = sentSB.getChildElements();
            SOAPBodyElement sentSBE = 
                (SOAPBodyElement)sentIt.next();
            Iterator sentIt2 = sentSBE.getChildElements();
            SOAPElement sentSE = (SOAPElement)sentIt2.next();

            // Get the orderID text to put in confirmation
            String sentID = sentSE.getValue();

            // Create the confirmation message
            confirmation = fac.createMessage();
            SOAPPart sp = confirmation.getSOAPPart();
            SOAPEnvelope env = sp.getEnvelope();
            SOAPBody sb = env.getBody();

            Name newBodyName = env.createName("confirmation", 
                "Confirm", "http://sonata.coffeebreak.com");
            SOAPBodyElement confirm = 
                sb.addBodyElement(newBodyName);

            // Create the orderID element for confirmation
            Name newOrderIDName = env.createName("orderId");
            SOAPElement newOrderNo =
                confirm.addChildElement(newOrderIDName);
            newOrderNo.addTextNode(sentID);

            // Create ship-date element
            Name shipDateName = env.createName("ship-date");
            SOAPElement shipDate = 
                confirm.addChildElement(shipDateName);

            // Create the shipping date
            Date today = new Date();
            long msPerDay = 1000 * 60 * 60 * 24;
            long msTarget = today.getTime();
            long msSum = msTarget + (msPerDay * 2);
            Date result = new Date();
            result.setTime(msSum);
            String sd = result.toString();
            shipDate.addTextNode(sd);

            confirmation.saveChanges();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return confirmation;
    }
}

