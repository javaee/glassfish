package org.glassfish.api.admin;

/**
 *
 * Interface denoting administrative commands that provide their
 * model.
 *
 * @author Jerome Dochez
 */
public interface CommandModelProvider {

    CommandModel getModel();
}
