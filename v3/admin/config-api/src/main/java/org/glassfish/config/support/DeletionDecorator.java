package org.glassfish.config.support;

import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * A decorator for acting upon a configuration element deletion.
 *
 * @param <T> the deleted element parent type
 * @param <U> the deleted element
 *
 * @author Jerome Dochez
 */
@Scoped(PerLookup.class)
public interface DeletionDecorator<T extends ConfigBeanProxy, U extends ConfigBeanProxy> {

    /**
     * notification of a configuration element of type U deletion.
     * 
     * Note that this notification is called within the boundaries of the
     * configuration transaction, therefore the parent instance is a
     * writable copy and further changes to the parent can be made without
     * enrolling it inside a transaction.
     *
     * @param context the command context to lead to the element deletion
     * @param parent the parent instance the element was removed from
     * @param child the deleted instance
     */
    public void decorate(AdminCommandContext context, T parent, U child);

    @Service
    public static class NoDecoration implements DeletionDecorator<ConfigBeanProxy, ConfigBeanProxy> {
        @Override
        public void decorate(AdminCommandContext context, ConfigBeanProxy parent, ConfigBeanProxy child) {
            // do nothing.
        }
    }
}
