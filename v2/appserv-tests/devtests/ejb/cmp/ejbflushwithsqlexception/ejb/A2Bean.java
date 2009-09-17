
package com.sun.s1asdev.ejb.ejbflush;

import javax.ejb.*;
import javax.naming.*;

/**
 * 2.0 bean. 
 * @author mvatkina
 */


public abstract class A2Bean implements javax.ejb.EntityBean {
    
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
        System.out.println("Debug: A2Bean ejbRemove");
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

    public abstract java.lang.String getId() ;
    public abstract void setId(java.lang.String s) ;

    public abstract java.lang.String getName() ;
    public abstract void setName(java.lang.String s) ;

    public java.lang.String ejbCreate(java.lang.String id) throws javax.ejb.CreateException {

        setId(id);
        setName("ABC");
        return null;
    }
    
    public void ejbPostCreate(java.lang.String name) throws javax.ejb.CreateException { 
    }

    public void setNameWithFlush(java.lang.String s) {
        setName(s);
    }
}
