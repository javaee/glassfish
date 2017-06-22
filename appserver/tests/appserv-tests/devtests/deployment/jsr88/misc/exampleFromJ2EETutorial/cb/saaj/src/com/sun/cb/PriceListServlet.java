/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import javax.xml.soap.*;
import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.transform.*;

import java.util.*;
import java.io.*;

public class PriceListServlet extends HttpServlet {
    static MessageFactory fac = null;
    
    static {
        try {
            fac = MessageFactory.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    };

    public void init(ServletConfig servletConfig) 
            throws ServletException {
        super.init(servletConfig);
    }
    
    public void doPost(HttpServletRequest req, 
        HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // Get all the headers from the HTTP request
            MimeHeaders headers = getHeaders(req);

            // Get the body of the HTTP request
            InputStream is = req.getInputStream();

            // Now internalize the contents of the HTTP request
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

    // This is the application code for responding to the message.

    public SOAPMessage onMessage(SOAPMessage msg) {
        SOAPMessage message = null;
        try {
            // create price list message
            message = fac.createMessage();

            // Access the SOAPBody object
            SOAPPart part = message.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            SOAPBody body = envelope.getBody();

            // Create the appropriate elements and add them

            Name bodyName = envelope.createName("price-list",
                "PriceList", "http://sonata.coffeebreak.com");
            SOAPBodyElement list = body.addBodyElement(bodyName);

            // coffee
            Name coffeeN = envelope.createName("coffee");
            SOAPElement coffee = list.addChildElement(coffeeN);

            Name coffeeNm1 = envelope.createName("coffee-name");
            SOAPElement coffeeName = 
                coffee.addChildElement(coffeeNm1);
            coffeeName.addTextNode("Arabica");

            Name priceName1 = envelope.createName("price");
            SOAPElement price1 = 
                coffee.addChildElement(priceName1); 
            price1.addTextNode("4.50");

            Name coffeeNm2 = envelope.createName("coffee-name");
            SOAPElement coffeeName2 = 
                coffee.addChildElement(coffeeNm2);
            coffeeName2.addTextNode("Espresso");

            Name priceName2 = envelope.createName("price");
            SOAPElement price2 = 
                coffee.addChildElement(priceName2); 
            price2.addTextNode("5.00");

            Name coffeeNm3 = envelope.createName("coffee-name");
            SOAPElement coffeeName3 = 
                coffee.addChildElement(coffeeNm3);
            coffeeName3.addTextNode("Dorada");

            Name priceName3 = envelope.createName("price");
            SOAPElement price3 = 
                coffee.addChildElement(priceName3); 
            price3.addTextNode("6.00");

            Name coffeeNm4 = envelope.createName("coffee-name");
            SOAPElement coffeeName4 = 
                coffee.addChildElement(coffeeNm4);
            coffeeName4.addTextNode("House Blend");

            Name priceName4 = envelope.createName("price");
            SOAPElement price4 = 
                coffee.addChildElement(priceName4); 
            price4.addTextNode("5.00");

            message.saveChanges();

        } catch(Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}
