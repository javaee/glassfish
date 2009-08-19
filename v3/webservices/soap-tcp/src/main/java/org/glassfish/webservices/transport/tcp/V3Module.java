/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.webservices.transport.tcp;

import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import com.sun.xml.ws.transport.tcp.server.WSTCPDelegate;
import com.sun.xml.ws.transport.tcp.server.WSTCPModule;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelWSImpl;
import java.util.List;
import javax.xml.namespace.QName;
import org.glassfish.webservices.InstanceResolverImpl;
import org.glassfish.webservices.WebServiceDeploymentListener;
import org.glassfish.webservices.WebServicesDeployer;
import org.xml.sax.EntityResolver;

/**
 *
 * @author oleksiys
 */
public class V3Module extends WSTCPModule {
    private final WSTCPDelegate delegate;

    V3Module() {
        WebServicesDeployer.getDeploymentNotifier().
            addListener(new WebServiceDeploymentListener() {

            public void onDeployed(WebServiceEndpoint endpoint) {
                endpoint.getWebComponentImpl().setLoadOnStartUp(0);
            }

            public void onUndeployed(WebServiceEndpoint endpoint) {
            }
        });

        AppServRegistry.getInstance();
        delegate = new WSTCPDelegate();
        delegate.setCustomWSRegistry(WSTCPAdapterRegistryImpl.getInstance());
        WSTCPModule.setInstance(this);
    }

    @Override
    public void register(String contextPath, List<TCPAdapter> adapters) {
        delegate.registerAdapters(contextPath, adapters);
    }

    @Override
    public void free(String contextPath, List<TCPAdapter> adapters) {
        delegate.freeAdapters(contextPath, adapters);
    }

    @Override
    public int getPort() {
        return -1;
    }

    public WSTCPDelegate getDelegate() {
        return delegate;
    }

    public WSEndpoint<ServiceChannelWSImpl> createServiceChannelEndpoint() {
        Class<ServiceChannelWSImpl> serviceEndpointClass = ServiceChannelWSImpl.class;
        final QName serviceName = WSEndpoint.getDefaultServiceName(ServiceChannelWSImpl.class);
        final QName portName = WSEndpoint.getDefaultPortName(serviceName, ServiceChannelWSImpl.class);
        final BindingID bindingId = BindingID.parse(ServiceChannelWSImpl.class);
        final WSBinding binding = bindingId.createBinding();

        final Invoker inv= (new InstanceResolverImpl(serviceEndpointClass)).createInvoker();

        return WSEndpoint.create(serviceEndpointClass, false,
                    inv,
                    serviceName, portName, Container.NONE, binding,
                    null, null, (EntityResolver) null, true);
    }
}
