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
 
 */

package dataregistry;

import javax.ejb.*;


public abstract class LineItemBean implements EntityBean {

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

    public abstract int getItemId();
    public abstract void setItemId(int itemId);

    public abstract int getQuantity();
    public abstract void setQuantity(int quantity);

    public abstract LocalVendorPart getVendorPart();
    public abstract void setVendorPart(LocalVendorPart vendorPart);

    public abstract LocalOrder getOrder();
    public abstract void setOrder(LocalOrder order);

    public LineItemKey ejbCreate(LocalOrder order, int quantity,
            LocalVendorPart vendorPart) throws CreateException {

        setOrderId(order.getOrderId());
        setItemId(order.getNextId());
        setQuantity(quantity);

        return null;
    }

    public void ejbPostCreate(LocalOrder order, int quantity,
            LocalVendorPart vendorPart) throws CreateException {

        setVendorPart(vendorPart);

        // This assignment is not necessary if the CMP container
        // treats setOrderId() as a relationship assignment.
        setOrder(order);
    }

}
