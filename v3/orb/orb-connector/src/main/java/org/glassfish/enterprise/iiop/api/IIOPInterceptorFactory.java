package org.glassfish.enterprise.iiop.api;

import org.jvnet.hk2.annotations.Contract;
import org.omg.IOP.Codec;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * @author Mahesh Kannan
 *         Date: Jan 16, 2009
 */
@Contract
public interface IIOPInterceptorFactory {

    public ClientRequestInterceptor createClientRequestInterceptor(ORBInitInfo info, Codec codec);

    public ServerRequestInterceptor createServerRequestInterceptor(ORBInitInfo info, Codec codec);
}
