
package test;

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
    
    public abstract Integer getId();
    public abstract void setId(Integer id);

    public abstract java.lang.String getName();
    public abstract void setName(java.lang.String name);

    public abstract java.util.Date getDate();
    public abstract void setDate(java.util.Date date);

    public abstract byte[] getBlb();
    public abstract void setBlb(byte[] b);

    public java.lang.Integer ejbCreate(Integer id, java.lang.String name, java.util.Date date, byte[] b) 
        throws javax.ejb.CreateException {

        setId(id);
        setName(name);
        setDate(date);
        setBlb(b);

        return null;
    }
    
    public void ejbPostCreate(Integer id, java.lang.String name, java.util.Date date, byte[] b) 
        throws javax.ejb.CreateException { }
    
    public void test() {
        java.util.Date d1 = getDate();
        System.out.println("Debug: ABean d1: " + d1);
        
        d1.setYear(2000);
        System.out.println("Debug: ABean d1 after setYear: " + d1);

        java.util.Date d2 = getDate();
        System.out.println("Debug: ABean d2: " + d2);
        if (d1.equals(d2))
            throw new EJBException("Same d1 and d2!");

        setDate(d1);
        d2.setMonth(2);
        System.out.println("Debug: ABean d2 after setMonth: " + d2);

        d1 = getDate();
        System.out.println("Debug: ABean d1: " + d1);
        if (d1.equals(d2)) 
            throw new EJBException("Same d1 and d2 after set!"); 

        setDate(null);
        if (getDate() != null)
            throw new EJBException("Date is not null after set!");

        byte[] b = getBlb();
        System.out.println("Debug: ABean b[0]: " + b[0]);
        b[0] = 90;

        byte[] b1 = getBlb(); 
        System.out.println("Debug: ABean b[0]: " + b[0]);
        System.out.println("Debug: ABean b1[0]: " + b1[0]);

        if (b[0] == b1[0])
            throw new EJBException("Same b and b1!"); 
 
        setBlb(b);
        b1[1] = 90; 
        System.out.println("Debug: ABean b[0]: " + b[0]);
        System.out.println("Debug: ABean b[1]: " + b[1]);
        System.out.println("Debug: ABean b1[0]: " + b1[0]);
        System.out.println("Debug: ABean b1[1]: " + b1[1]);

        b = getBlb();  

        System.out.println("Debug: ABean b[0]: " + b[0]);
        System.out.println("Debug: ABean b[1]: " + b[1]);
        System.out.println("Debug: ABean b1[0]: " + b1[0]);
        System.out.println("Debug: ABean b1[1]: " + b1[1]);

        if (b[1] == b1[1])
            throw new EJBException("Same b and b1 after set!"); 

        setBlb(null);
        if (getBlb() != null)
            throw new EJBException("Blob is not null after set!");

    }
}
