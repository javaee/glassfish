/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

/**
 * @author arungupta
 */
public class HelloException extends RuntimeException {
    public HelloException() {

    }

    public HelloException(String message) {
        super(message);
    }
}
