/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.lang.IllegalStateException;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.*;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

//@README : test : this @Connector should not be considered as component definition annotation.
// if there are multiple @Connector annotations,one that is a class as specified in ra.xml should be considered


/**
 * This is a sample resource adapter
 *
 * @author	Qingqing Ouyang
 */
@Connector(
   displayName = "Simple Resource Adapter",
   vendorName = "Java Software",
   eisType = "Generic Type",
   version = "1.0Alpha"
)
public class SimpleResourceAdapterImpl_1 
implements ResourceAdapter, java.io.Serializable {

    private BootstrapContext ctx;
    private WorkManager wm;
    private String testName;
    @ConfigProperty(
            type = java.lang.String.class
    )
    private String testName1="testName1";

    private boolean debug = true;
    private Work work;

    public SimpleResourceAdapterImpl_1() {
        debug ("constructor...");
    }

    public void
    start(BootstrapContext ctx) throws ResourceAdapterInternalException{
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

    public void
    stop() {
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

    public void
    endpointActivation ( MessageEndpointFactory factory, ActivationSpec spec)
        throws NotSupportedException {
            throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

    public void
    endpointDeactivation (
            MessageEndpointFactory endpointFactory, 
            ActivationSpec spec) {
        throw new IllegalStateException("This resource-adapter should not have been initialized");  
    }
  
    public String getTestName() {
        return testName;
    }

    @ConfigProperty(
            defaultValue = "ConfigPropertyForRA",
            type = java.lang.String.class
    )
    public void setTestName(String name) {
        debug("setTestName called... name = " + name);
        testName = name;
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }

  public String getTestName1() {
        return testName1;
    }

    public void setTestName1(String name) {
        debug("setTestName1 called... name = " + name);
        testName1 = name;
        throw new IllegalStateException("This resource-adapter should not have been initialized");
    }


    public void
    debug (String message) {
        if (debug)
            System.out.println("[SimpleResourceAdapterImpl] ==> " + message);
    }

    public XAResource[] getXAResources(ActivationSpec[] specs) 
        throws ResourceException {
        throw new UnsupportedOperationException();
    }
}
