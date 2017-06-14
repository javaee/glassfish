package samples.ejb.subclassing.ejb;

import java.rmi.RemoteException;

public interface CustomerCheckingHome extends CustomerHome {
    public CustomerChecking create(String SSN, String lastName, String firstName, String address1, String address2, String city, String state, String zipCode) throws RemoteException,javax.ejb.CreateException;

    public CustomerChecking findByPrimaryKey(String SSN) throws RemoteException,javax.ejb.FinderException;
}

