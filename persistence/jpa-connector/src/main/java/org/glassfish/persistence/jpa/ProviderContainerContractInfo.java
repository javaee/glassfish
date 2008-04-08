package org.glassfish.persistence.jpa;

import javax.persistence.spi.ClassTransformer;
import javax.sql.DataSource;
import javax.naming.NamingException;

/**
 * @author Mitesh Meswani
 * This encapsulates information needed  to load or unload persistence units.
 */
public interface ProviderContainerContractInfo {

    /**
     *
     * @return a class loader that is used to load persistence entities
     * bundled in this application.
     */
    ClassLoader getClassLoader();

    /**
     *
     * @return a temp class loader that is used to load persistence entities
     * bundled in this application.
     */
    ClassLoader getTempClassloader();

    /**
     *
     * @return Adds ClassTransformer to underlying Application's classloader
     */
    void addTransformer(ClassTransformer transformer);


    /**
     * @return absolute path of the location where application is exploded.
     */
    String getApplicationLocation();

    /**
     * Looks up DataSource with JNDI name given by <code>dataSourceName</code>
     * @param dataSourceName
     * @return DataSource with JNDI name given by <code>dataSourceName</code>
     * @throws javax.naming.NamingException
     */
    DataSource lookupDataSource(String dataSourceName) throws NamingException;

    /**
     * Looks up Non transactional DataSource with JNDI name given by <code>dataSourceName</code>
     * @param dataSourceName
     * @return Non transactional DataSource with JNDI name given by <code>dataSourceName</code>
     * @throws NamingException
     */
    DataSource lookupNonTxDataSource(String dataSourceName) throws NamingException;
}
