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
import javax.xml.rpc.Stub;

public class OrderCaller {

    private SupplierIF supplier;

    public OrderCaller(String endpoint) {

        try {
		    Stub stub = (Stub)(new Supplier_Impl().getSupplierIFPort());
            //stub = (SupplierIF_Stub)(new Supplier_Impl().getSupplierIFPort());
            stub._setProperty(
                javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY,
                endpoint);
            supplier = (SupplierIF) stub;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public ConfirmationBean placeOrder(OrderBean order) {

        ConfirmationBean result = null;
        try {
            result = supplier.placeOrder(order);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

} // class
