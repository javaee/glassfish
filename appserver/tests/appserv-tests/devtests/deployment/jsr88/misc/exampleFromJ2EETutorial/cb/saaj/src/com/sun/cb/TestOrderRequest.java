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

import java.net.*;
import java.io.*;
import java.util.*;

import javax.xml.soap.*;

public class TestOrderRequest {
    public static void main(String [] args) {    
        try {
            SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
            SOAPConnection con = scf.createConnection();

            MessageFactory mf = MessageFactory.newInstance();
        
            SOAPMessage msg = mf.createMessage();

            // Access the SOABBody object
            SOAPPart part = msg.getSOAPPart();
            SOAPEnvelope envelope = part.getEnvelope();
            SOAPBody body = envelope.getBody();

            // Create the appropriate elements and add them

            Name bodyName = envelope.createName("coffee-order", "PO",
                            "http://sonata.coffeebreak.com");
            SOAPBodyElement order = body.addBodyElement(bodyName);

            // orderID
            Name orderIDName = envelope.createName("orderID");
            SOAPElement orderID =
                order.addChildElement(orderIDName);
            orderID.addTextNode("1234");

            // customer
            Name childName = envelope.createName("customer");
            SOAPElement customer = order.addChildElement(childName);

            childName = envelope.createName("last-name");
            SOAPElement lastName = customer.addChildElement(childName);
            lastName.addTextNode("Pental");

            childName = envelope.createName("first-name");
            SOAPElement firstName = customer.addChildElement(childName);
            firstName.addTextNode("Ragni");

            childName = envelope.createName("phone-number");
            SOAPElement phoneNumber = customer.addChildElement(childName);
            phoneNumber.addTextNode("908 983-6789");

            childName = envelope.createName("email-address");
            SOAPElement emailAddress = 
                customer.addChildElement(childName);
            emailAddress.addTextNode("ragnip@aol.com");

            // address
            childName = envelope.createName("address");
            SOAPElement address = order.addChildElement(childName);

            childName = envelope.createName("street");
            SOAPElement street = address.addChildElement(childName);
            street.addTextNode("9876 Central Way");

            childName = envelope.createName("city");
            SOAPElement city = address.addChildElement(childName);
            city.addTextNode("Rainbow");

            childName = envelope.createName("state");
            SOAPElement state = address.addChildElement(childName);
            state.addTextNode("CA");

            childName = envelope.createName("zip");
            SOAPElement zip = address.addChildElement(childName);
            zip.addTextNode("99999");
    
            // line-item 1
            childName = envelope.createName("line-item");
            SOAPElement lineItem = order.addChildElement(childName);

            childName = envelope.createName("coffeeName");
            SOAPElement coffeeName = lineItem.addChildElement(childName);
            coffeeName.addTextNode("arabica");

            childName = envelope.createName("pounds");
            SOAPElement pounds = lineItem.addChildElement(childName);
            pounds.addTextNode("2");

            childName = envelope.createName("price");
            SOAPElement price = lineItem.addChildElement(childName);
            price.addTextNode("10.95");

            // line-item 2
            childName = envelope.createName("coffee-name");
            SOAPElement coffeeName2 = lineItem.addChildElement(childName);
            coffeeName2.addTextNode("espresso");

            childName = envelope.createName("pounds");
            SOAPElement pounds2 = lineItem.addChildElement(childName);
            pounds2.addTextNode("3");

            childName = envelope.createName("price");
            SOAPElement price2 = lineItem.addChildElement(childName);
            price2.addTextNode("10.95");

            // total
            childName = envelope.createName("total");
            SOAPElement total = order.addChildElement(childName);
            total.addTextNode("21.90");
    
            URL endpoint = new URL(
                URLHelper.getSaajURL() + "/orderCoffee");
            SOAPMessage reply = con.call(msg, endpoint);
            con.close();

            // extract content of reply
            //Extracting order ID and ship date
            SOAPBody sBody = reply.getSOAPPart().getEnvelope().getBody();
            Iterator bodyIt = sBody.getChildElements();
            SOAPBodyElement sbEl = (SOAPBodyElement)bodyIt.next();
            Iterator bodyIt2 = sbEl.getChildElements();

            // get orderID
            SOAPElement ID = (SOAPElement)bodyIt2.next();
            String id = ID.getValue();

            // get ship date
            SOAPElement sDate = (SOAPElement)bodyIt2.next();
            String shippingDate = sDate.getValue();

            System.out.println("");
            System.out.println("");
            System.out.println("Confirmation for order #" + id);
            System.out.print("Your order will be shipped on ");
            System.out.println(shippingDate);    
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
