package samples.ejb.subclassing.ejb;

public interface CustomerCheckingHome extends CustomerHome {
    public CustomerChecking create(String SSN, String lastName, String firstName, String address1, String address2, String city, String state, String zipCode) throws javax.ejb.CreateException;

    public CustomerChecking findByPrimaryKey(String SSN) throws javax.ejb.FinderException;
}

