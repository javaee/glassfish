package org.glassfish.api.deployment;

import org.glassfish.api.Param;
import org.glassfish.api.admin.ParameterNames;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Feb 4, 2009
 * Time: 10:49:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class UndeployCommandParameters extends DeploymentOperationParameters {

    @Param(primary = true, name= ParameterNames.NAME)
    public String name=null;

    @Param(optional=true)
    public String target = "server";

    @Param(optional=true, defaultValue="false")
    public Boolean keepreposdir;

    @Param(optional=true)
    public Properties properties=null;

    public String name() {
        return name;
    }

    public UndeployCommandParameters() {
    }

    public UndeployCommandParameters(String name) {
        this.name = name;
    }

    public String libraries() {
        throw new IllegalStateException("We need to be able to get access to libraries when undeploying");
    }
}
