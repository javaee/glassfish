package org.glassfish.api.deployment;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Feb 4, 2009
 * Time: 9:42:41 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OpsParams {

    public enum Origin { load, deploy, unload, undeploy }
    public Origin origin; 

    public abstract String name();

    public abstract String libraries();

}
