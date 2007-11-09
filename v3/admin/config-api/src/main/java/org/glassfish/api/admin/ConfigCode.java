package org.glassfish.api.admin;

import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;

/**
 * Replacement for Runnable interface with more muscle
 *
 * @author Jerome Dochez
 */
public interface ConfigCode {

    public boolean run(ConfigBean... objects) throws PropertyVetoException;
}
