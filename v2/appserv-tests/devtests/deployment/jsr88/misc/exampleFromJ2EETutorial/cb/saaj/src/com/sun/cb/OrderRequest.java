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

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

import javax.xml.soap.*;

public class OrderRequest {
    String url;

    public OrderRequest(String url){
        this.url = url;
    }

    public ConfirmationBean placeOrder(OrderBean orderBean) {
        ConfirmationBean cb = null;   

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

            // Create the appropriate elements and add them
            Name bodyName = envelope.createName("coffee-order", "PO",
                "http://sonata.coffeebreak.com");
            SOAPBodyElement order = body.addBodyElement(bodyName);

            // orderID
            Name orderIDName = envelope.createName("orderID");
            SOAPElement orderID = order.addChildElement(orderIDName);
            orderID.addTextNode(orderBean.getId());

            // customer
            Name childName = envelope.createName("customer");
            SOAPElement customer = order.addChildElement(childName);

            childName = envelope.createName("last-name");
            SOAPElement lastName = customer.addChildElement(childName);
            lastName.addTextNode(orderBean.getCustomer().getLastName());

            childName = envelope.createName("first-name");
            SOAPElement firstName = customer.addChildElement(childName);
            firstName.addTextNode(orderBean.getCustomer().getFirstName());

            childName = envelope.createName("phone-number");
            SOAPElement phoneNumber = customer.addChildElement(childName);
            phoneNumber.addTextNode(
                orderBean.getCustomer().getPhoneNumber());

            childName = envelope.createName("email-address");
            SOAPElement emailAddress = 
                customer.addChildElement(childName);
            emailAddress.addTextNode(
                orderBean.getCustomer().getEmailAddress());

            // address
            childName = envelope.createName("address");
            SOAPElement address = order.addChildElement(childName);

            childName = envelope.createName("street");
            SOAPElement street = address.addChildElement(childName);
            street.addTextNode(orderBean.getAddress().getStreet());

            childName = envelope.createName("city");
            SOAPElement city = address.addChildElement(childName);
            city.addTextNode(orderBean.getAddress().getCity());

            childName = envelope.createName("state");
            SOAPElement state = address.addChildElement(childName);
            state.addTextNode(orderBean.getAddress().getState());

            childName = envelope.createName("zip");
            SOAPElement zip = address.addChildElement(childName);
            zip.addTextNode(orderBean.getAddress().getZip());
    
            LineItemBean[] lineItems=orderBean.getLineItems();            
            for (int i=0;i < lineItems.length;i++) {
                LineItemBean lib = lineItems[i];

                childName = envelope.createName("line-item");
                SOAPElement lineItem = order.addChildElement(childName);

                childName = envelope.createName("coffeeName");
                SOAPElement coffeeName = 
                    lineItem.addChildElement(childName);
                coffeeName.addTextNode(lib.getCoffeeName());

                childName = envelope.createName("pounds");
                SOAPElement pounds = lineItem.addChildElement(childName);
                pounds.addTextNode(lib.getPounds().toString());

                childName = envelope.createName("price");
                SOAPElement price = lineItem.addChildElement(childName);
                price.addTextNode(lib.getPrice().toString());
            }

            // total
            childName = envelope.createName("total");
            SOAPElement total = 
                order.addChildElement(childName);
            total.addTextNode(orderBean.getTotal().toString()); 
              
            URL endpoint = new URL(url);
            SOAPMessage reply = con.call(msg, endpoint);
            con.close();

            // Extract content of reply
            // Extract order ID and ship date
            SOAPBody sBody = reply.getSOAPPart().getEnvelope().getBody();
            Iterator bodyIt = sBody.getChildElements();
            SOAPBodyElement sbEl = (SOAPBodyElement)bodyIt.next();
            Iterator bodyIt2 = sbEl.getChildElements();

            // Get orderID
            SOAPElement ID = (SOAPElement)bodyIt2.next();
            String id = ID.getValue();

            // Get ship date
            SOAPElement sDate = (SOAPElement)bodyIt2.next();
            String shippingDate = sDate.getValue();
            SimpleDateFormat df = 
                new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            Date date = df.parse(shippingDate);
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            cb = new ConfirmationBean(id, cal);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return cb;
    }
}
