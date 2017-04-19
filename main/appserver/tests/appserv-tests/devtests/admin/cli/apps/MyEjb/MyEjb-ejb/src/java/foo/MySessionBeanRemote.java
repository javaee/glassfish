/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package foo;

import javax.ejb.Remote;

/**
 *
 * @author wnevins
 */
@Remote
public interface MySessionBeanRemote {

    String getMessage();
    
}
