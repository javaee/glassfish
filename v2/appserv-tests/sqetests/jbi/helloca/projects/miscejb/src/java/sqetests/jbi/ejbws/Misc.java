/*
 * Misc.java
 *
 * Created on January 12, 2007, 2:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sqetests.jbi.ejbws;

import javax.ejb.Stateless;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.ws.Holder;

/**
 *
 * @author sm122304
 */

@Stateless()
@WebService()
public class Misc {
    
    @WebMethod
    @Oneway
    public void ping() {
        System.out.println("ping ping ping");
    }
       
    @WebMethod
    public void createAccount(@WebParam(name="UserName", mode=Mode.IN) String name, 
            @WebParam(name="Balance", mode=Mode.IN) double d,
            @WebParam(name="Account", mode=Mode.INOUT) Holder<Account> holder) {
        if (d >= 10.0) {
            Account account = new Account(name, 1000, d);
            holder.value = account;
        } else {
            throw new RuntimeException("Minimun balance should be more than 10.00");
        }
    }
}
