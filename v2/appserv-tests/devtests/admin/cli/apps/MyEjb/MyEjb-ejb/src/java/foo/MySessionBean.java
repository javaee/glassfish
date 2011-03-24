/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package foo;

import javax.ejb.Stateless;

/**
 *
 * @author wnevins
 */
@Stateless
public class MySessionBean implements MySessionBeanRemote {

    public String getMessage() {
        return "Message from Stateless EJB Here!!";
    }
}
