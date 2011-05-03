
package test;

import javax.ejb.*;

/**
 * 1.1 Bean that is deployed as a read-only bean
 * @author mvatkina
 */


public class A1Bean implements EntityBean {
    
    public String id;
    public String shortName;
    public String description;

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

    /** Method is used to test read-only functionality */
    public String getShortName() {
        return shortName;
    }

    /** Method is used to test non-DFG field in read-only beans */
    public String getDescription() {
        return description;
    }
}
