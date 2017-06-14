
package test;

import javax.ejb.*;
import java.util.*;

/**
 * @author mvatkina
 */

public interface A2Local extends javax.ejb.EJBLocalObject {

    public void setName(String name); 
 
    public void setNameWithFlush(String name); 
 
}
