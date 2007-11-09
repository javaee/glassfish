package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;

/**
 * A domain.xml reference
 *
 * @author Jerome Dochez
 */
public interface Ref {

    public String getRef();

    public void setRef(String refName) throws PropertyVetoException;
}
