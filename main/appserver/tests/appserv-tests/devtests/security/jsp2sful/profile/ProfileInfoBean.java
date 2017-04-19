package profile;

import javax.ejb.SessionContext;
import javax.ejb.SessionBean;
import java.util.Vector;
import java.lang.String;
import java.util.Iterator;
import javax.ejb.EJBException;
import java.rmi.RemoteException;
/**
 *
 * @author  hsingh
 */

public class ProfileInfoBean implements SessionBean {
    
    private String name;
    
    private SessionContext sc = null;
    
    /** Creates a new instance of ProfieInfo */
    public void ejbCreate(String name) {
        this.name = name;
    }

    public String getCallerInfo() {
        return sc.getCallerPrincipal().toString();
    }

    public String getSecretInfo() {
        return "Keep It Secret!";
    }
    
    public void ejbActivate() {
        System.out.println("In ShoppingCart ejbActivate");
    }
    
    
    public void ejbPassivate() {
        System.out.println("In ShoppingCart ejbPassivate");
    }
    
    
    public void ejbRemove()  {
        System.out.println("In ShoppingCart ejbRemove");
    }
    
    
    public void setSessionContext(javax.ejb.SessionContext sessionContext) {
        sc = sessionContext;
    }
    
}
