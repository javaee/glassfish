/*
 * Copyright � 2003 Sun Microsystems, Inc.  All rights reserved.  U.S.
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
 * Copyright � 2003 Sun Microsystems, Inc. Tous droits r�serv�s.
 *
 * Droits du gouvernement am�ricain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions
 * en vigueur de la FAR (Federal Acquisition Regulations) et des
 * suppl�ments � celles-ci.  Distribu� par des licences qui en
 * restreignent l'utilisation.
 *
 * Cette distribution peut comprendre des composants d�velopp�s par des
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE
 * sont des marques de fabrique ou des marques d�pos�es de Sun
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
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
