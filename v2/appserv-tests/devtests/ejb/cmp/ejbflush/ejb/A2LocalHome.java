
package com.sun.s1asdev.ejb.ejbflush;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A2LocalHome extends javax.ejb.EJBLocalHome {
    
    public  A2Local findByPrimaryKey(java.lang.String pk)  throws javax.ejb.FinderException;
    
    public  A2Local create(java.lang.String name) throws javax.ejb.CreateException;
    
}
