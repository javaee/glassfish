/*
 * Bank.java
 *
 * Created on February 16, 2007, 10:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bank.ejb;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 *
 * @author sony
 */

@Stateless()
@WebService()
public class Bank {
    
    @Resource SessionContext context;
    
    @WebMethod
    @PermitAll
    public double getCheckingAccountInterestRate() {
        return 5.25;        
    }

    @WebMethod
    @RolesAllowed(value = { "bankmanager" } )
    public int createAccount(String name, float balance) {
        System.out.println("Bank.createAccount()");
        System.out.println("Caller principal : " + 
                context.getCallerPrincipal().getName());
        return 1001;
    }

    @WebMethod
    @RolesAllowed(value = { "bankcustomer" , "bankmanager" })
    public double debit(int accountId, double amount) {
        System.out.println("Bank.debit() ");
        System.out.println("Caller principal : " + 
                context.getCallerPrincipal().getName());
        return amount + 1000;
    }
}
