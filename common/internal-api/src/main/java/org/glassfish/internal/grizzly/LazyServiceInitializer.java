/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.internal.grizzly;

import java.nio.channels.SelectableChannel;

import org.jvnet.hk2.annotations.Contract;

/**
 * This interface is meant for all services that wish to be initialized lazily.
 * Such services are expected to implement this interface and those
 * implementatons should be available to HK2 for lookup. 
 * 
 * 
 * @author Vijay Ramachandran
*/
@Contract
public interface LazyServiceInitializer {
    /**
     * Returns the string associated with 'name' attribute of corresponding
     * <network-listener> element in domain.xml. The service initializer uses
     * this return value to select the proper service provider from the list
     * of service providers that implement this interface.
     * 
     * For GFv3, however, since IIOP and MQ service will not able to move
     * <network-listener> element, the implementations of this interface for
     * IIOP/JMS service will return String "iiop-service"/"mq-service"
     *
     * @return The value of the 'name' attribute in the corresponding 
     *         <network-listener> element for this service
     */
    public String getServiceName();

    /**
     * Upon accepting the first request on the port (to which this listener is
     * bound), the listener will select the appropriate provider and call this
     * method to let the actual service initialize itself. All further accept
     * requests on this port will wait while the service is initialized.
     * Upon successful completion of service initialization, all pending
     * requests are passed to the service using the handleRequest method
     *
     * @return Return true if service initialization went through fine; false
     *         otherwise
     */
    public boolean initializeService();

    /**
     * Upon successful ACCEPT of every request on this port, the service
     * is called upon to handle the request. The service is provided the 
     * channel itself. The service can setup connection, its characteristics,
     * decide on blocking/non-blocking modes etc. The service is expected to
     * return control back to the listener ASAP without consuming this thread
     * for processing the requst completely.
     *
     * @param channel where the incoming request was accepted.
     */
    public void handleRequest(SelectableChannel channel);
}
