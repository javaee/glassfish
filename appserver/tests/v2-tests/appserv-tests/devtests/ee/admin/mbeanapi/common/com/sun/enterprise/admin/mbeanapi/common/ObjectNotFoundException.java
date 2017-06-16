package com.sun.enterprise.admin.mbeanapi.common;

/**
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Aug 24, 2004
 * @version $Revision: 1.1 $
 */
public class ObjectNotFoundException extends Exception{
    public ObjectNotFoundException(String msg){
        super(msg);
    }
}
