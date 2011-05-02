package test;

import javax.ejb.*;
import java.rmi.*;

public interface TestHome extends EJBHome {
 
    Test create() throws RemoteException, CreateException;
}
