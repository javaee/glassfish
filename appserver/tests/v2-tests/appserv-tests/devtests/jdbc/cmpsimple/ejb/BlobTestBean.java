
package com.sun.s1asdev.jdbc.cmpsimple.ejb;

import javax.ejb.*;
import javax.naming.*;

/**
 * @author mvatkina
 */


public abstract class BlobTestBean implements javax.ejb.EntityBean {
    
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
        System.out.println("Debug: BlobTest ejbRemove");
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
    
    public abstract Integer getId();
    public abstract void setId(Integer id);

    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);
    
    /*
    public abstract byte[] getBlb();
    public abstract void setBlb(byte[] b);
    

    public java.lang.Integer ejbCreate(Integer id, java.lang.String name, byte[] b) throws javax.ejb.CreateException {
        setId(id);
        setName(name);
        setBlb(b);

        return null;
    }
    */  
    public java.lang.Integer ejbCreate(Integer id, java.lang.String name) 
        throws javax.ejb.CreateException 
    {
        setId(id);
        setName(name);

        return null;
    }
  
    /*
    public void ejbPostCreate(Integer id, java.lang.String name, byte[] b) throws javax.ejb.CreateException {
    }
    */
    
    public void ejbPostCreate(Integer id, java.lang.String name ) 
        throws javax.ejb.CreateException {}
}	
