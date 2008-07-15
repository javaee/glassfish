package org.glassfish.flashlight.client;


import org.jvnet.hk2.annotations.Contract;

/**
 * @author Mahesh Kannan
 *         Date: Jan 27, 2008
 */
@Contract
public interface ProbeClientMediator {

    public void registerListener(Object listener);

}
