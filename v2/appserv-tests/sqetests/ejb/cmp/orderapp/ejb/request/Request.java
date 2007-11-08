/*
 * Copyright © 2003 Sun Microsystems, Inc.  All rights reserved.  U.S.
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
 * Copyright © 2003 Sun Microsystems, Inc. Tous droits réservés.
 *
 * Droits du gouvernement américain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * suppléments à celles-ci.  Distribué par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants développés par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques déposées de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
 */


package request;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.*;


public interface Request extends EJBObject {

    public void createPart(PartRequest partRequest) throws RemoteException;

    public void addPartToBillOfMaterial(BomRequest bomRequest) throws RemoteException;

    public void createVendor(VendorRequest vendorRequest) throws RemoteException;

    public void createVendorPart(VendorPartRequest vendorPartRequest) throws RemoteException;

    public void createOrder(OrderRequest orderRequest) throws RemoteException;

    public void addLineItem(LineItemRequest lineItemRequest) throws RemoteException;

    public double getBillOfMaterialPrice(BomRequest bomRequest) throws RemoteException;

    public Double getAvgPrice() throws RemoteException;
    
    public Double getTotalPricePerVendor(VendorRequest vendorRequest) throws RemoteException;
    
    public double getOrderPrice(Integer orderId) throws RemoteException;
    
    public void adjustOrderDiscount(int adjustment) throws RemoteException;
    
    public Collection locateVendorsByPartialName(String name) throws RemoteException;

    public String reportVendorsByOrder(Integer orderId) throws RemoteException;

    public int countAllItems() throws RemoteException;

    public void removeOrder(Integer orderId) throws RemoteException;

}

