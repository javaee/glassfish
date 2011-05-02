
package create;

import javax.ejb.*;
import javax.naming.*;

/**
 * 1.1 bean. 
 * @author mvatkina
 */


public class A1Bean implements javax.ejb.EntityBean {
    
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
        System.out.println("Debug: A1Bean ejbRemove");
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
    public java.lang.String ejbCreate(java.lang.String name) throws javax.ejb.CreateException {

        this.name = name;
        return null;
    }
    
    public void ejbPostCreate(java.lang.String name) throws javax.ejb.CreateException { 
        throw new javax.ejb.CreateException("A1Bean.ejbPostCreate");
    }
    
    /** This ejbCreate/ejbPostCreate combination tests CreateException
     * thrown from either ejbCreate (if b is false) or ejbPostCreate.
     * Executed in a non-transactional context to test both options.
     */
    public java.lang.String ejbCreate(java.lang.String name, boolean b)  throws javax.ejb.CreateException {
        if (b) {
             this.name = name;
        } else { 
             throw new javax.ejb.CreateException("A1Bean.ejbCreate");
        }
        return null;
    }

    public void ejbPostCreate(java.lang.String name, boolean b)   throws javax.ejb.CreateException {
        if (b) {
             throw new javax.ejb.CreateException("A1Bean.ejbPostCreate");
       }
    }

    /** This ejbCreate/ejbPostCreate combination tests that bean state is
     * reset prior to call to ejbCreate.
     */
    public java.lang.String ejbCreate(int i)  throws javax.ejb.CreateException {
        if (this.name != null) { 
             throw new java.lang.IllegalStateException("A1Bean.ejbCreate not reset");
        }

        this.name = "A1Bean_" + i;
        return null;
    }

    public void ejbPostCreate(int i)   throws javax.ejb.CreateException {
    }

}
