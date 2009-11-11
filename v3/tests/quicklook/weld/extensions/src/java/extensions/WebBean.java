/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package extensions;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author paulsandoz
 */
@RequestScoped
public class WebBean {
    @Resource(name="injectedResource") int injectedResource;

    public int getI() {
        return injectedResource;
    }
    
    public String get() { return "BEAN" + injectedResource; }
}
