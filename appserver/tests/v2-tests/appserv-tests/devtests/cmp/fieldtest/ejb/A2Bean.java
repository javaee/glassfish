
package fieldtest;

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

    public void update() {

        setSqlDate(new java.sql.Date(getMyDate().getTime()));
        java.util.Date d = getMyDate();
        d.setTime(0);
        setMyDate(d);
        java.util.ArrayList c = getList();
        c.add(getName());
        setList(c);
    }

    public abstract java.lang.String getId1() ;
    public abstract void setId1(java.lang.String s) ;

    public abstract java.util.Date getIddate();
    public abstract void setIddate(java.util.Date d);

    public abstract java.lang.String getName() ;
    public abstract void setName(java.lang.String s) ;

    public abstract java.util.ArrayList getList();
    public abstract void setList(java.util.ArrayList l);

    public abstract java.util.Date getMyDate();
    public abstract void setMyDate(java.util.Date d);

    public abstract java.sql.Date getSqlDate() ;
    public abstract void setSqlDate(java.sql.Date d) ;

    public abstract byte[] getBlb() ;
    public abstract void setBlb(byte[] b) ;

    public A2PK ejbCreate(java.lang.String name) throws javax.ejb.CreateException {

        long now = System.currentTimeMillis();
        setId1(name);
        setName(name);
        setIddate(new java.util.Date(0));
        setMyDate(new java.util.Date(now));

        return null;
    }
    
    public void ejbPostCreate(java.lang.String name) throws javax.ejb.CreateException { 
        setBlb(new byte[]{1,2});
        setList(new java.util.ArrayList());
    }
    
}
