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


