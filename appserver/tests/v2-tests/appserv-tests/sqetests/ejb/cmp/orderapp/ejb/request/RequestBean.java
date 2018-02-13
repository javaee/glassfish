/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.*;
import javax.naming.*;

import dataregistry.*;


public class RequestBean implements SessionBean {

    private SessionContext context;
    
    private LocalLineItemHome lineItemHome = null;
    
    private LocalOrderHome orderHome = null;
    
    private LocalPartHome partHome = null;
    
    private LocalVendorHome vendorHome = null;
    
    private LocalVendorPartHome vendorPartHome = null;
    
    
    /**
     * @see SessionBean#setSessionContext(SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context=aContext;
    }
    
    
    /**
     * @see SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    
    /**
     * @see SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    
    /**
     * @see SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     */
    public void ejbCreate() {
        try {
            lineItemHome   = lookupLineItem();
            orderHome      = lookupOrder();
            partHome       = lookupPart();
            vendorHome     = lookupVendor();
            vendorPartHome = lookupVendorPart();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    public void createPart(PartRequest partRequest) {
        try {
            LocalPart part = partHome.create(partRequest.partNumber, 
                    partRequest.revision, partRequest.description,
                    partRequest.revisionDate, partRequest.specification, 
                    partRequest.drawing);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    public void addPartToBillOfMaterial(BomRequest bomRequest) {
        try {
            PartKey bomkey = new PartKey();
            bomkey.partNumber = bomRequest.bomPartNumber;
            bomkey.revision = bomRequest.bomRevision;
    
            LocalPart bom = partHome.findByPrimaryKey(bomkey);
    
            PartKey pkey = new PartKey();
            pkey.partNumber = bomRequest.partNumber;
            pkey.revision = bomRequest.revision;
    
            LocalPart part = partHome.findByPrimaryKey(pkey);
            part.setBomPart(bom);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    public void createVendor(VendorRequest vendorRequest) {
        try {
            LocalVendor vendor = vendorHome.create(vendorRequest.vendorId, 
                    vendorRequest.name, vendorRequest.address, 
                    vendorRequest.contact, vendorRequest.phone);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }
    
    public void createVendorPart(VendorPartRequest vendorPartRequest) {
        try {
            PartKey pkey = new PartKey();
            pkey.partNumber = vendorPartRequest.partNumber;
            pkey.revision = vendorPartRequest.revision;
    
            LocalPart part = partHome.findByPrimaryKey(pkey);
            LocalVendorPart vendorPart = vendorPartHome.create(
                    vendorPartRequest.description, vendorPartRequest.price, 
                    part);

            VendorKey vkey = new VendorKey();
            vkey.vendorId = vendorPartRequest.vendorId;

            LocalVendor vendor = vendorHome.findByPrimaryKey(vkey);
            vendor.addVendorPart(vendorPart);

        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void createOrder(OrderRequest orderRequest) {
        try {
            LocalOrder order = orderHome.create(orderRequest.orderId, 
                    orderRequest.status, orderRequest.discount, 
                    orderRequest.shipmentInfo);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void addLineItem(LineItemRequest lineItemRequest) { 
        try {
            LocalOrder order = orderHome.findByPrimaryKey(lineItemRequest.orderId);
    
            PartKey pkey = new PartKey();
            pkey.partNumber = lineItemRequest.partNumber;
            pkey.revision = lineItemRequest.revision;
    
            LocalPart part = partHome.findByPrimaryKey(pkey);
    
            LocalLineItem lineItem = lineItemHome.create(order, lineItemRequest.quantity,
                    part.getVendorPart());
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public double getBillOfMaterialPrice(BomRequest bomRequest) {
        double price = 0.0;
        try {
            PartKey bomkey = new PartKey();
            bomkey.partNumber = bomRequest.bomPartNumber;
            bomkey.revision = bomRequest.bomRevision;
    
            LocalPart bom = partHome.findByPrimaryKey(bomkey);
            Collection parts = bom.getParts();
            for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
                LocalPart part = (LocalPart)iterator.next();
                LocalVendorPart vendorPart = part.getVendorPart();
                price += vendorPart.getPrice();
            }
    
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }

        return price;
    }

    public double getOrderPrice(Integer orderId) {
        double price = 0.0;
        try {
            LocalOrder order = orderHome.findByPrimaryKey(orderId);
            price = order.calculateAmmount();
    
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }

        return price;
    }

    public void adjustOrderDiscount(int adjustment) {
        orderHome.adjustDiscount(adjustment);
    }

    public Double getAvgPrice() {
        return vendorPartHome.getAvgPrice();
    }

    public Double getTotalPricePerVendor(VendorRequest vendorRequest) {
        return vendorPartHome.getTotalPricePerVendor(vendorRequest.vendorId);
    }

    public Collection locateVendorsByPartialName(String name) {

        Collection names = new ArrayList();
        try {
            Collection vendors = vendorHome.findByPartialName(name);
            for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
                LocalVendor vendor = (LocalVendor)iterator.next();
                names.add(vendor.getName());
            }
    
        } catch (FinderException e) {
        }

        return names;
    }

    public int countAllItems() {
        int count = 0;
        try {
            count = lineItemHome.findAll().size();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }

        return count;
    }

    public void removeOrder(Integer orderId) {
        try {
            orderHome.remove(orderId);
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public String reportVendorsByOrder(Integer orderId) {
        StringBuffer report = new StringBuffer();
        try {
            Collection vendors = vendorHome.findByOrder(orderId);
            for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
                LocalVendor vendor = (LocalVendor)iterator.next();
                report.append(vendor.getVendorId()).append(' ')
                      .append(vendor.getName()).append(' ')
                      .append(vendor.getContact()).append('\n');
            }
    
        } catch (FinderException e) {
            throw new EJBException(e.getMessage());
        }

        return report.toString();
    }

    private LocalLineItemHome lookupLineItem() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleLineItem");

        return (LocalLineItemHome) objref;
    }

    private LocalOrderHome lookupOrder() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleOrder");

        return (LocalOrderHome) objref;
    }

    private LocalPartHome lookupPart() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimplePart");

        return (LocalPartHome) objref;
    }

    private LocalVendorHome lookupVendor() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleVendor");

        return (LocalVendorHome) objref;
    }

    private LocalVendorPartHome lookupVendorPart() throws NamingException {
        Context initial = new InitialContext();
        Object objref = initial.lookup("java:comp/env/ejb/SimpleVendorPart");

        return (LocalVendorPartHome) objref;
    }

}
