/*
 * Account.java
 *
 * Created on January 17, 2007, 11:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sqetests.jbi.ejbws;

/**
 *
 * @author Sony Manuel
 */
public class Account {
    
    public String name = "";
    
    public int id = 0;
    
    public double balance = 0.0;
    
    /** Creates a new instance of Account */
    public Account() {
    }
    
    public Account(String name, int id, double balance) {
        this.name = name;
        this.id = id;
        this.balance = balance;
    }
}
