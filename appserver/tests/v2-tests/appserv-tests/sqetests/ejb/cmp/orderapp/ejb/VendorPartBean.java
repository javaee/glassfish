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


public abstract class VendorPartBean implements EntityBean {

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

    public abstract String getDescription();
    public abstract void setDescription(String description);

    public abstract double getPrice();
    public abstract void setPrice(double price);

    public abstract LocalPart getPart();
    public abstract void setPart(LocalPart part);

    public abstract LocalVendor getVendor();
    public abstract void setVendor(LocalVendor vendor);

    public Object ejbCreate(String description, double price, LocalPart part)
        throws CreateException {

        setDescription(description);
        setPrice(price);

        return null;
    }

    public void ejbPostCreate(String description, double price, LocalPart part)
        throws CreateException {

        setPart(part);
    }

    public abstract Double ejbSelectAvgPrice() throws FinderException;

    public abstract Double ejbSelectTotalPricePerVendor(int vendorId) throws FinderException;

    public Double ejbHomeGetAvgPrice() throws FinderException {
        return ejbSelectAvgPrice();
    }

    public Double ejbHomeGetTotalPricePerVendor(int vendorId) throws FinderException {
        return ejbSelectTotalPricePerVendor(vendorId);
    }
}
