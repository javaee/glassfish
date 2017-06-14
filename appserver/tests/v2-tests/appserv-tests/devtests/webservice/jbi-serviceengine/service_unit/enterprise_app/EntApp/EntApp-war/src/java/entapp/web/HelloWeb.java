/*
 * HelloWeb.java
 *
 * Created on July 27, 2007, 9:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package entapp.web;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author bhavani
 */
@WebService()
public class HelloWeb {

    /**
     * Web service operation
     */
    @WebMethod
    public String sayHello(@WebParam(name = "name")
    String name) {
        String hello = "Hello from HelloWeb - " + name;
        System.out.println(hello);
        return hello;
    }
    
}
