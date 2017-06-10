/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package connector;

import javax.resource.spi.*;

public abstract class AbstractResourceAdapter{

    private String testName;
    protected boolean debug = true;

    
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
    }

    public void
    debug (String message) {
        if (debug)
            System.out.println("["+this.getClass().getName()+"] ==> " + message);
    }
}