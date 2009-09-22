package org.glassfish.internal.api;

/**
 * Such service implementations are invoked when the domain directory
 * is upgraded following a upgrade event.
 *
 * At the time of execution, the configuration file (domain.xml) has
 * been upraded but the deployed applications have not been redeployed
 * yet.
 *
 * @author Jerome Dochez
 */
public interface DomainUpgrade {
    // tag interface, implementations should rely on postConstruct
    // for behaviour.
}
