package org.glassfish.api.admin;

import java.beans.PropertyVetoException;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Oct 31, 2007
 * Time: 9:50:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SingleConfigCode<T> {

    public boolean run(T param) throws PropertyVetoException;
}
