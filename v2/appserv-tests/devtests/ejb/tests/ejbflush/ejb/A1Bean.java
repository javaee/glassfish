
package com.sun.s1asdev.ejb.ejbflush;

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

    public java.lang.String id;
    public java.lang.String name;

    public java.lang.String ejbCreate(java.lang.String id) throws javax.ejb.CreateException {

        this.id = id;
        name = "ABC";
        return null;
    }
    
    public void ejbPostCreate(java.lang.String id) throws javax.ejb.CreateException { 
    }

    public void setName(String s) {
        name = s;
    }

    public void setNameWithFlush(String s) {
        name = s;
    }

}
