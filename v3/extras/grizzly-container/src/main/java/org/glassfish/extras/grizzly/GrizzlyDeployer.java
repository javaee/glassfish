package org.glassfish.extras.grizzly;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.DeploymentContext;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Dec 2, 2008
 * Time: 4:18:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="grizzly")
public class GrizzlyDeployer implements Deployer<GrizzlyContainer, GrizzlyAdapterApplication> {

    public MetaData getMetaData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean prepare(DeploymentContext context) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public GrizzlyAdapterApplication load(GrizzlyContainer container, DeploymentContext context) {

        GrizzlyApplicationsConfiguration configs = new GrizzlyApplicationsConfiguration(context.getSource());

        // I only get one for now...
        Map.Entry<String, String> config = configs.getTuples().entrySet().iterator().next();
        com.sun.grizzly.tcp.Adapter adapter = null;
        try {
            Class<com.sun.grizzly.tcp.Adapter> adapterClass = (Class<com.sun.grizzly.tcp.Adapter>)
                    context.getClassLoader().loadClass(config.getValue());
            adapter = adapterClass.newInstance();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return  new GrizzlyAdapterApplication(adapter, config.getKey(), context.getClassLoader());
    }

    public void unload(GrizzlyAdapterApplication appContainer, DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clean(DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
