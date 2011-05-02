
package test;

import javax.ejb.*;

/**
 * 2.0 bean. 
 * @author mvatkina
 */


public abstract class A2Bean implements EntityBean {
    
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

    public abstract String getId() ;
    public abstract void setId(String s) ;

    public abstract String getName() ;
    public abstract void setName(String s) ;

    public String ejbCreate(String id) throws CreateException {

        setId(id);
        setName("ABC");
        return null;
    }
    
    public void ejbPostCreate(String name) throws CreateException { 
    }

    public void setNameWithFlush(String s) {
        setName(s);
    }
}
