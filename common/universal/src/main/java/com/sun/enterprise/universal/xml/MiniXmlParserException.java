/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.universal.xml;

/**
 *
 * @author bnevins
 */
public class MiniXmlParserException extends Exception {
    /**
     * Wrapper for XML processing errors
     * @param t The real parsing Exception
     */
    public MiniXmlParserException(Throwable t)
    {
        super(t);
    }
    public MiniXmlParserException(String msg, Throwable t) {
        super(msg, t);
    }
    public MiniXmlParserException(String msg) {
        super(msg);
    }
}
