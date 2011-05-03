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

import java.util.Collection;

import javax.ejb.*;


public abstract class VendorBean implements EntityBean {

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

    public abstract int getVendorId();
    public abstract void setVendorId(int vendorId);

    public abstract String getName();
    public abstract void setName(String name);

    public abstract String getAddress();
    public abstract void setAddress(String address);

    public abstract String getContact();
    public abstract void setContact(String contact);

    public abstract String getPhone();
    public abstract void setPhone(String phone);

    public abstract Collection getVendorParts();
    public abstract void setVendorParts(Collection vendorParts);

    public VendorKey ejbCreate(int vendorId, String name, String address,
            String contact, String phone) throws CreateException {

        setVendorId(vendorId);
        setName(name);
        setAddress(address);
        setContact(contact);
        setPhone(phone);

        return null;
    }

    public void ejbPostCreate(int vendorId, String name, String address,
            String contact, String phone) throws CreateException {
    }

    public void addVendorPart(LocalVendorPart part) {
        getVendorParts().add(part);
    }
}
