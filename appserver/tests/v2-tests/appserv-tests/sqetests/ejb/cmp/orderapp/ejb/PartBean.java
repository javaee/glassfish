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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.ejb.*;


public abstract class PartBean implements EntityBean {

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

    public abstract String getPartNumber();
    public abstract void setPartNumber(String partNumber);

    public abstract int getRevision();
    public abstract void setRevision(int revision);

    public abstract String getDescription();
    public abstract void setDescription(String description);

    public abstract Date getRevisionDate();
    public abstract void setRevisionDate(Date revisionDate);

    public abstract Serializable getDrawing();
    public abstract void setDrawing(Serializable drawing);

    public abstract String getSpecification();
    public abstract void setSpecification(String specification);

    public abstract LocalPart getBomPart();
    public abstract void setBomPart(LocalPart bomPart);

    public abstract Collection getParts();
    public abstract void setParts(Collection parts);

    public abstract LocalVendorPart getVendorPart();
    public abstract void setVendorPart(LocalVendorPart vendorPart);

    public PartKey ejbCreate(String partNumber, int revision, String description,
            Date revisionDate, String specification, Serializable drawing)
            throws CreateException {

        setPartNumber(partNumber);
        setRevision(revision);
        setDescription(description);
        setRevisionDate(revisionDate);
        setSpecification(specification);
        setDrawing(drawing);

        return null;
    }

    public void ejbPostCreate(String partNumber, int revision, String description,
            Date revisionDate, String specification, Serializable drawing)
            throws CreateException {
    }

}
