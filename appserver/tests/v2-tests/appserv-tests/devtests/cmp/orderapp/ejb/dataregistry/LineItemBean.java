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
