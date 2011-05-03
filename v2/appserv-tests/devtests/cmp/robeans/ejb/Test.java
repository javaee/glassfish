package test;

import javax.ejb.*;
import java.rmi.*;

public interface Test extends EJBObject {
 
    /** Insert values via jdbc call */
    public void insertValues(String table_name) throws RemoteException;
        
    /** Update values via jdbc call */
    public void updateValues(String table_name) throws RemoteException;
        
}
