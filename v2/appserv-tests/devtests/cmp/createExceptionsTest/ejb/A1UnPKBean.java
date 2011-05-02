
package create;

import javax.ejb.*;
import javax.naming.*;

/**
 * 1.1 bean with unknown PK.
 * @author mvatkina
 */


public class A1UnPKBean implements javax.ejb.EntityBean {
    
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
        System.out.println("Debug: A1UnPKBean ejbRemove");
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

    public java.lang.String name;

    /** This ejbCreate/ejbPostCreate combination tests CreateException 
     * thrown from ejbPostCreate. 
     */
    public java.lang.Object ejbCreate(java.lang.String name) throws javax.ejb.CreateException {

        this.name = name;
        return null;
    }
    
    public void ejbPostCreate(java.lang.String name) throws javax.ejb.CreateException { 
        throw new javax.ejb.CreateException("A1UnPKBean.ejbPostCreate");
    }
    
    /** This ejbCreate/ejbPostCreate combination tests that bean state is
     * reset prior to call to ejbCreate.
     */
    public java.lang.Object ejbCreate(int i)  throws javax.ejb.CreateException {
        if (this.name != null) {
             throw new java.lang.IllegalStateException("A1UnPKBean.ejbCreate not reset");
        }

        this.name = "A1UnPKBean_" + i;
        return null;
    }

    public void ejbPostCreate(int i)   throws javax.ejb.CreateException {
    }

}
