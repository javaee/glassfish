
package test;

import javax.ejb.*;

/**
 * 2.0 Bean that is deployed as a read-only bean
 * @author mvatkina
 */


public abstract class A2Bean implements EntityBean {
    
    private EntityContext context;
    
    public abstract String getId();
    public abstract void setId(String s);

    public abstract String getShortName();
    public abstract void setShortName(String s);

    public abstract String getDescription();
    public abstract void setDescription(String s);

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

}
