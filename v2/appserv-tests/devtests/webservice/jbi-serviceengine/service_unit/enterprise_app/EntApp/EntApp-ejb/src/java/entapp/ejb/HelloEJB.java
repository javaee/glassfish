/*
 * HelloEJB.java
 *
 * Created on July 27, 2007, 9:06 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package entapp.ejb;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author bhavani
 */

@Stateless()
@WebService()
public class HelloEJB {

    /**
     * Web service operation
     */
    @WebMethod
    public String sayHello(@WebParam(name = "name")
    String name) {
        String hello = "Hello from HelloEJB - " + name;
        System.out.println(hello);
        return hello;
    }
    
}
