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
import java.util.*;
import java.math.BigDecimal;

import java.net.*;

public class PriceListRequest {
    String url;
  
    public PriceListRequest(String url){
        this.url = url;
    }

    public PriceListBean getPriceList() {
        PriceListBean plb = null;
        try {
            SOAPConnectionFactory scf = 
                SOAPConnectionFactory.newInstance();
            SOAPConnection con = scf.createConnection();

            MessageFactory mf = MessageFactory.newInstance();
            SOAPMessage msg = mf.createMessage();

            // Access the SOAPBody object
            SOAPPart part = msg.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            SOAPBody body = envelope.getBody();

            // Create SOAPBodyElement request 
            Name bodyName = envelope.createName("request-prices",
                "RequestPrices", "http://sonata.coffeebreak.com");
            SOAPBodyElement requestPrices =
                body.addBodyElement(bodyName);
      
            Name requestName = envelope.createName("request");
            SOAPElement request = 
                requestPrices.addChildElement(requestName);
            request.addTextNode("Send updated price list.");

            msg.saveChanges();

            // Create the endpoint and send the message
            URL endpoint = new URL(url);
            SOAPMessage response = con.call(msg, endpoint);
            con.close();

            // Get contents of response

            Vector list = new Vector();

            SOAPBody responseBody = 
                response.getSOAPPart().getEnvelope().getBody();
            Iterator it1 = responseBody.getChildElements(); 

            // Get price-list element
            while (it1.hasNext()) {
                SOAPBodyElement bodyEl = (SOAPBodyElement)it1.next();
                Iterator it2 = bodyEl.getChildElements();
                // Get coffee elements
                while (it2.hasNext()) {
                    SOAPElement child2 = (SOAPElement)it2.next();
                    Iterator it3 = child2.getChildElements();
                    // get coffee-name and price elements
                    while (it3.hasNext()) {
                        SOAPElement child3 = (SOAPElement)it3.next();
                        String value = child3.getValue();
                        list.addElement(value);
                    }
                }
            }

            ArrayList items = new ArrayList();
            for (int i = 0; i < list.size(); i = i + 2) {
                items.add(
                    new PriceItemBean(list.elementAt(i).toString(), 
                    new BigDecimal(list.elementAt(i + 1).toString())));
                System.out.print(list.elementAt(i) + "        ");
                System.out.println(list.elementAt(i + 1));
            }

            PriceItemBean[] priceItems = new PriceItemBean[items.size()];
            int i=0;
      			for (Iterator it=items.iterator(); it.hasNext(); ) {
              priceItems[i] = (PriceItemBean)it.next();
              i++;	
            }
            Date today = new Date();
            Date endDate = DateHelper.addDays(today, 30);
            Calendar todayCal = new GregorianCalendar();
            todayCal.setTime(today);
            Calendar cal = new GregorianCalendar();
            cal.setTime(endDate);
            plb = new PriceListBean();
            plb.setStartDate(todayCal);
            plb.setPriceItems(priceItems);
            plb.setEndDate(cal);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return plb;
    }
}


