
package com.sun.s1asdev.ejb.ejbflush;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A2Local extends javax.ejb.EJBLocalObject {

    public void setName(String name); 
 
    public void setNameWithFlush(String name); 
 
}
