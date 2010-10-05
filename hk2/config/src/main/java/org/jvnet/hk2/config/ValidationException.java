/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hk2.config;

/**
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since hk2 0.3.10
 */
public class ValidationException extends Exception {

    public ValidationException() {
        super();
    }
    public ValidationException(String msg) {
        super(msg);
    }
    public ValidationException(Throwable e) {
        super(e);
    }
}
