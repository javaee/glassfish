
package pkvalidation;

import javax.ejb.*;
import javax.naming.*;

/**
 * @author mvatkina
 */


public abstract class BBean implements javax.ejb.EntityBean {
    
    private javax.ejb.EntityContext context;
    
    
    /**
     * @see javax.ejb.EntityBean#setEntityContext(javax.ejb.EntityContext)
     */
    public void setEntityContext(javax.ejb.EntityContext aContext) {
        context=aContext;
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbRemove()
     */
    public void ejbRemove() {
        System.out.println("Debug: BBean ejbRemove");
    }
    
    
    /**
     * @see javax.ejb.EntityBean#unsetEntityContext()
     */
    public void unsetEntityContext() {
        context=null;
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbLoad()
     */
    public void ejbLoad() {
        
    }
    
    
    /**
     * @see javax.ejb.EntityBean#ejbStore()
     */
    public void ejbStore() {
        
    }
    
    public abstract java.sql.Date getId();
    public abstract void setId(java.sql.Date id);

    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);

    public java.sql.Date ejbCreate(java.sql.Date id, java.lang.String name) throws javax.ejb.CreateException {

        setId(id);
        setName(name);

        return null;
    }
    
    public void ejbPostCreate(java.sql.Date id, java.lang.String name)
        throws javax.ejb.CreateException { }
    
}
