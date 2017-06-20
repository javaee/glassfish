package samples.ejb.installed_libraries_embedded.ejb;


public interface CustomerCheckingLocalHome extends CustomerLocalHome {
    public CustomerChecking create(String SSN, String lastName, String firstName, String address1, String address2, String city, String state, String zipCode) throws javax.ejb.CreateException;

    public CustomerChecking findByPrimaryKey(String SSN) throws javax.ejb.FinderException;
}

