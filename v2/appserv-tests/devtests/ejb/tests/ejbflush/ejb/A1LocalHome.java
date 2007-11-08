
package com.sun.s1asdev.ejb.ejbflush;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A1LocalHome extends javax.ejb.EJBLocalHome {
    
    public  A1Local findByPrimaryKey(java.lang.String pk)  throws javax.ejb.FinderException;
    
    public  A1Local create(java.lang.String id) throws javax.ejb.CreateException;
    
}
