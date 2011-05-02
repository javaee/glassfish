
package pkvalidation;

import javax.ejb.*;
import javax.naming.*;

/**
 * @author mvatkina
 */


public abstract class ABean implements javax.ejb.EntityBean {
    
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
        System.out.println("Debug: ABean ejbRemove");
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
    
    public abstract long getId();
    public abstract void setId(long id);

    public abstract java.lang.String getLastname();
    public abstract void setLastname(java.lang.String name);

    public abstract double getSalary();
    public abstract void setSalary(double s);

    public APK ejbCreate(long id, java.lang.String name, double s) throws javax.ejb.CreateException {

        setId(id);
        setLastname(name);
        setSalary(s);

        return null;
    }
    
    public void ejbPostCreate(long id, java.lang.String name, double s)
        throws javax.ejb.CreateException { }
    
}
