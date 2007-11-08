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

import java.math.BigDecimal;
import java.util.*;

public class TestOrderCaller {
    public static void main(String[] args) {
        try {

            AddressBean address = new AddressBean("455 Apple Way",
               "Santa Clara", "CA", "95123");
            CustomerBean customer = new CustomerBean("Buzz",
               "Murphy", "247-5566", "buzz.murphy@clover.com");

            LineItemBean itemA = new LineItemBean("mocha", new BigDecimal("1.0"), new BigDecimal("9.50"));
            LineItemBean itemB = new LineItemBean("special blend", new BigDecimal("5.0"), new BigDecimal("8.00"));
            LineItemBean itemC = new LineItemBean("wakeup call", new BigDecimal("0.5"), new BigDecimal("10.00"));
            LineItemBean[] lineItems = {itemA, itemB, itemC};

            OrderBean order = new OrderBean(address, customer, "123", lineItems, 
                new BigDecimal("55.67"));

            OrderCaller oc = new OrderCaller(args[0]);
            ConfirmationBean confirmation = oc.placeOrder(order);

            System.out.println(confirmation.getOrderId()  + " " +
                DateHelper.format(confirmation.getShippingDate(), "MM/dd/yy"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
