package org.glassfish.flashlight.datatree;

import java.lang.reflect.Method;

/**
 * A method implementation can be setup to return a value for a particular statistic
 * On a call to {@link org.glassfish.flashlight.datatree.TreeNode#getValue()}, the method
 * object on the instance is invoked
 * @author Harpreet Singh
 */
public interface MethodInvoker extends TreeElement {

    void setMethod (Method m);

    Method getMethod ();

    void setInstance (Object i);

    Object getInstance ();
}
