/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved.  U.S.
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
 * Copyright (c) 2004 Sun Microsystems, Inc. Tous droits reserves.
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


package client;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import request.*;
import dataregistry.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class Client {
    private static OrderRequest orderRequest;
    private static MessageFormat mf =
        new MessageFormat(": {0, number, $#,##0.##}");

 private static SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        try {
            status.addDescription("Testing cmp order app.");
            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/Request");
            RequestHome home =
                (RequestHome) PortableRemoteObject.narrow(objref,
                    RequestHome.class);

            Request request = home.create();

            createData(request);
            printData(request);
            status.printSummary("cmp-orderAppID");
        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
        }
    }

    private static void createData(Request request) {
        try {
            request.createPart(new PartRequest("1234-5678-01", 1, "ABC PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("9876-4321-02", 2, "DEF PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("5456-6789-03", 3, "GHI PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("ABCD-XYZW-FF", 5, "XYZ PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));
            request.createPart(new PartRequest("SDFG-ERTY-BN", 7, "BOM PART",
                    new java.util.Date(), "PARTQWERTYUIOPASXDCFVGBHNJMKL", null));

            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "1234-5678-01", 1));
            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "9876-4321-02", 2));
            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "5456-6789-03", 3));
            request.addPartToBillOfMaterial(new BomRequest("SDFG-ERTY-BN", 7,
                    "ABCD-XYZW-FF", 5));

            request.createVendor(new VendorRequest(100, "WidgetCorp",
                    "111 Main St., Anytown, KY 99999", "Mr. Jones",
                    "888-777-9999"));
            request.createVendor(new VendorRequest(200, "Gadget, Inc.",
                    "123 State St., Sometown, MI 88888", "Mrs. Smith",
                    "866-345-6789"));

            request.createVendorPart(new VendorPartRequest("1234-5678-01", 1,
                    "PART1", 100.00, 100));
            request.createVendorPart(new VendorPartRequest("9876-4321-02", 2,
                    "PART2", 10.44, 200));
            request.createVendorPart(new VendorPartRequest("5456-6789-03", 3,
                    "PART3", 76.23, 200));
            request.createVendorPart(new VendorPartRequest("ABCD-XYZW-FF", 5,
                    "PART4", 55.19, 100));
            request.createVendorPart(new VendorPartRequest("SDFG-ERTY-BN", 7,
                    "PART5", 345.87, 100));

            Integer orderId = new Integer(1111);
            request.createOrder(new OrderRequest(orderId, 'N', 10,
                    "333 New Court, NewCity, CA 90000"));
            request.addLineItem(new LineItemRequest(orderId, "1234-5678-01", 1,
                    3));
            request.addLineItem(new LineItemRequest(orderId, "9876-4321-02", 2,
                    5));
            request.addLineItem(new LineItemRequest(orderId, "ABCD-XYZW-FF", 5,
                    7));

            orderId = new Integer(4312);
            request.createOrder(new OrderRequest(orderId, 'N', 0,
                    "333 New Court, NewCity, CA 90000"));
            request.addLineItem(new LineItemRequest(orderId, "SDFG-ERTY-BN", 7,
                    1));
            request.addLineItem(new LineItemRequest(orderId, "ABCD-XYZW-FF", 5,
                    3));
            request.addLineItem(new LineItemRequest(orderId, "1234-5678-01", 1,
                    15));
         status.addStatus("cmp order:createData", status.PASS);
        } catch (Exception ex) {
         status.addStatus("cmp order:createData", status.FAIL);
            System.err.println("Caught an exception:");
            ex.printStackTrace();
        }
    }

    private static void printData(Request request) {
        try {
            BomRequest bomRequest = new BomRequest("SDFG-ERTY-BN", 7, null, 0);
            double price = request.getBillOfMaterialPrice(bomRequest);
            System.out.println("Cost of Bill of Material for PN " +
                bomRequest.bomPartNumber + " Rev: " + bomRequest.bomRevision +
                mf.format(new Object[] { new Double(price) }));

            printCostOfOrders(request);

            System.out.println("\nAdding 5% discount");
            request.adjustOrderDiscount(5);
            printCostOfOrders(request);

            System.out.println("\nRemoving 7% discount");
            request.adjustOrderDiscount(-7);
            printCostOfOrders(request);

            java.lang.Double price0 = request.getAvgPrice();

            if (price0 == null) {
                System.out.println("\nNo parts found");
            } else {
                System.out.println("\nAverage price of all parts" +
                    mf.format(new Object[] { price0 }));
            }

            VendorRequest vendorRequest =
                new VendorRequest(100, null, null, null, null);
            price0 = request.getTotalPricePerVendor(vendorRequest);

            if (price0 == null) {
                System.out.println("\nNo parts found for Vendor " +
                    vendorRequest.vendorId);
            } else {
                System.out.println("\nTotal price of parts for Vendor " +
                    vendorRequest.vendorId + "" +
                    mf.format(new Object[] { price0 }));
            }

            System.out.println("\nOrdered list of vendors for order 1111");
            System.out.println(request.reportVendorsByOrder(new Integer(1111)));

            System.out.println("Counting all line items");

            int count = request.countAllItems();
            System.out.println("Found " + count + " line items");

            System.out.println("\nRemoving Order");
            request.removeOrder(new Integer(4312));
            count = request.countAllItems();
            System.out.println("Found " + count + " line items");

            Collection names = request.locateVendorsByPartialName("I");
            System.out.println("\nFound " + names.size() +
                " out of 2 vendors with 'I' in the name:");

            for (Iterator it = names.iterator(); it.hasNext();) {
                System.out.println(it.next());
            }
        status.addStatus("cmp order:printData", status.PASS);
        } catch (Exception ex) {
        status.addStatus("cmp order:printData", status.FAIL);
            System.err.println("Caught an exception:");
            ex.printStackTrace();
        }
    }

    private static void printCostOfOrders(Request request)
        throws java.rmi.RemoteException {
        Integer orderId = new Integer(1111);
        double price = request.getOrderPrice(orderId);
        System.out.println("Cost of Order " + orderId +
            mf.format(new Object[] { new Double(price) }));

        orderId = new Integer(4312);
        price = request.getOrderPrice(orderId);
        System.out.println("Cost of Order " + orderId +
            mf.format(new Object[] { new Double(price) }));
    }
}
