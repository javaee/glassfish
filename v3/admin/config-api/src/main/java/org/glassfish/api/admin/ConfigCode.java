package org.glassfish.api.admin;

import org.glassfish.api.admin.ConfigBean;

import java.beans.PropertyVetoException;

/**
 * Allows multiple object as part of the transaction but requires manual casting.
 *
 * @see SingleConfigCode Single Oject equivalent
 *
 * @author Jerome Dochez
 */
public interface ConfigCode {

	/**
	 * Runs the following command passing the configration object. The code will be run
	 * within a transaction, returning true will commit the transaction, false will abort
	 * it.
	 *
	 * @param params is the list of configuration objects protected by the transaction
     * @return true if the changes on param should be commited or false for abort.
     * @throws PropertyVetoException if the changes cannot be applied
     * to the configuration
	 */
    public boolean run(ConfigBean... params) throws PropertyVetoException;
}
