
package test;

import javax.ejb.*;

/**
 * 1.1 bean. 
 * @author mvatkina
 */


public class A1Bean implements EntityBean {
    
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

    public String id;
    public String name;

    public String ejbCreate(String id) throws CreateException {

        this.id = id;
        name = "ABC";
        return null;
    }
    
    public void ejbPostCreate(String id) throws CreateException { 
    }

    public void setName(String s) {
        name = s;
    }

    public void setNameWithFlush(String s) {
        name = s;
    }

}
