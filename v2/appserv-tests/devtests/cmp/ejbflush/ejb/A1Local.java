
package test;

import javax.ejb.*;

/**
 * @author mvatkina
 */

public interface A1Local extends javax.ejb.EJBLocalObject {

    public void setName(String name); 

    public void setNameWithFlush(String name); 

}

