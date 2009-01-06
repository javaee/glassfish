package org.glassfish.api.deployment;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 5, 2009
 * Time: 4:26:09 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MetaDataProvider {

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData();
    
}
