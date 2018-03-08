/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina;

import javax.management.NotificationBroadcasterSupport;

/**
 * A <strong>Service</strong> is a group of one or more
 * <strong>Connectors</strong> that share a single <strong>Container</strong>
 * to process their incoming requests.  This arrangement allows, for example,
 * a non-SSL and SSL connector to share the same population of web apps.
 * <p>
 * A given JVM can contain any number of Service instances; however, they are
 * completely independent of each other and share only the basic JVM facilities
 * and classes on the system class path.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:20 $
 */

public interface Service {


    // ------------------------------------------------------------- Properties


    /**
     * Return the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     */
    public Container getContainer();


    /**
     * Set the <code>Container</code> that handles requests for all
     * <code>Connectors</code> associated with this Service.
     *
     * @param container The new Container
     */
    public void setContainer(Container container);


    /**
     * Return descriptive information about this Service implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Return the name of this Service.
     */
    public String getName();


    /**
     * Set the name of this Service.
     *
     * @param name The new service name
     */
    public void setName(String name);


    /**
     * Return the <code>Server</code> with which we are associated (if any).
     */
    public Server getServer();


    /**
     * Set the <code>Server</code> with which we are associated (if any).
     *
     * @param server The server that owns this Service
     */
    public void setServer(Server server);


    /**
     * Return the <code>NotificationBroadcasterSupport</code> that sends notification for this Service.
     */
    public NotificationBroadcasterSupport getBroadcaster();


    /**
     * Set the <code>NotificationBroadcasterSupport</code> that sends notification for this Service
     *
     * @param broadcaster The new NotificationBroadcasterSupport
     */
    public void setBroadcaster(NotificationBroadcasterSupport broadcaster);

    
    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Connector to the set of defined Connectors, and associate it
     * with this Service's Container.
     *
     * @param connector The Connector to be added
     */
    public void addConnector(Connector connector);


    /**
     * Find and return the set of Connectors associated with this Service.
     */
    public Connector[] findConnectors();


    /**
     * Remove the specified Connector from the set associated from this
     * Service.  The removed Connector will also be disassociated from our
     * Container.
     *
     * @param connector The Connector to be removed
     */
    // START SJSAS 6231069
    //public void removeConnector(Connector connector);
    public void removeConnector(Connector connector) throws LifecycleException;
     // END SJSAS 6231069

    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     *
     * @exception LifecycleException If this server was already initialized.
     */
    public void initialize()
    throws LifecycleException;

}
