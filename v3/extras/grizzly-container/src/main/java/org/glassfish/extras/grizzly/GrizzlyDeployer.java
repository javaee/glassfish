package org.glassfish.extras.grizzly;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.container.RequestDispatcher;

import java.util.Map;
import java.util.LinkedList;
import java.util.logging.Level;

import com.sun.logging.LogDomains;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Dec 2, 2008
 * Time: 4:18:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="grizzly")
public class GrizzlyDeployer implements Deployer<GrizzlyContainer, GrizzlyApp> {

    @Inject
    RequestDispatcher dispatcher;
    
    public MetaData getMetaData() {
        return new MetaData(false, new Class[] { GrizzlyModuleDescriptor.class}, null);
    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return type.cast(new GrizzlyModuleDescriptor(context.getSource(), context.getLogger()));
    }

    public boolean prepare(DeploymentContext context) {
        return true;
    }

    public GrizzlyApp load(GrizzlyContainer container, DeploymentContext context) {

        GrizzlyModuleDescriptor configs = context.getModuleMetaData(GrizzlyModuleDescriptor.class);

        LinkedList<GrizzlyApp.Adapter> modules = new LinkedList<GrizzlyApp.Adapter>();
        for (Map.Entry<String, String> config : configs.getAdapters().entrySet()) {
            com.sun.grizzly.tcp.Adapter adapter;
            try {
                Class adapterClass = context.getClassLoader().loadClass(config.getValue());
                adapter = com.sun.grizzly.tcp.Adapter.class.cast(adapterClass.newInstance());
            } catch(Exception e) {
                context.getLogger().log(Level.SEVERE, e.getMessage(),e);
                return null;
            }
            modules.add(new GrizzlyApp.Adapter(config.getKey(), adapter));
        }
        return new GrizzlyApp(modules, dispatcher, context.getClassLoader());

    }

    public void unload(GrizzlyApp appContainer, DeploymentContext context) {
    }

    public void clean(DeploymentContext context) {
    }
}
