
package com.sun.enterprise.v3.admin;

import org.jvnet.hk2.component.ComponentException;

/**
 * HK2 has an Injection Manager.  CommandRunner makes an instance of this Injection
 * Manager and overrides/overrides some methods.  Now we throw an Exception out.  If it
 * is a ComponentException and if the field is optional -- HK2 swallows the
 * Exception.
 * So, instead, we throw this RuntimeException and HK2 will propagate it back as
 * a wrapped Exception.
 * Then we look at the cause and pull out the real error message.
 * @author bnevins
 */
class UnacceptableValueException extends RuntimeException{

    UnacceptableValueException(String msg) {
        super(msg);
    }

}
