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

package dataregistry;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.ejb.*;


public abstract class OrderBean implements EntityBean {

    private EntityContext context;


    /**
     * @see EntityBean#setEntityContext(EntityContext)
     */
    public void setEntityContext(EntityContext aContext) {
        context=aContext;
    }


    /**
     * @see EntityBean#ejbActivate()
     */
    public void ejbActivate() {

    }


    /**
     * @see EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {

    }


    /**
     * @see EntityBean#ejbRemove()
     */
    public void ejbRemove() {

    }


    /**
     * @see EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context=null;
    }


    /**
     * @see EntityBean#ejbLoad()
     */
    public void ejbLoad() {

    }


    /**
     * @see EntityBean#ejbStore()
     */
    public void ejbStore() {

    }

    public abstract Integer getOrderId();
    public abstract void setOrderId(Integer orderId);

    public abstract char getStatus();
    public abstract void setStatus(char status);

    public abstract Date getLastUpdate();
    public abstract void setLastUpdate(Date lastUpdate);

    public abstract int getDiscount();
    public abstract void setDiscount(int discount);

    public abstract String getShipmentInfo();
    public abstract void setShipmentInfo(String shipmentInfo);

    public abstract Collection getLineItems();
    public abstract void setLineItems(Collection lineItems);

    public Integer ejbCreate(Integer orderId, char status, int discount,
            String shipmentInfo) throws CreateException {

        setOrderId(orderId);
        setStatus(status);
        setLastUpdate(new Date());
        setDiscount(discount);
        setShipmentInfo(shipmentInfo);

        return null;
    }

    public void ejbPostCreate(Integer orderId, char status, int discount,
            String shipmentInfo) throws CreateException {
    }

    public abstract Collection ejbSelectAll() throws FinderException;

    public double calculateAmmount() {
        double ammount = 0;
        Collection items = getLineItems();
        for (Iterator it = items.iterator(); it.hasNext();) {
            LocalLineItem item = (LocalLineItem)it.next();
            LocalVendorPart part = item.getVendorPart();
            ammount += part.getPrice() * item.getQuantity();
        }

        return (ammount * (100 - getDiscount()))/100;
    }

    public int getNextId() {
        return getLineItems().size() + 1;
    }

    public void ejbHomeAdjustDiscount(int adjustment) {
        try {
            Collection orders = ejbSelectAll();
            for (Iterator it = orders.iterator(); it.hasNext();) {
                LocalOrder order = (LocalOrder)it.next();
                int newDiscount = order.getDiscount() + adjustment;
                order.setDiscount((newDiscount > 0)? newDiscount : 0);
            }
        } catch (Exception ex) {
            throw new EJBException (ex.getMessage());
        }
    }

}
