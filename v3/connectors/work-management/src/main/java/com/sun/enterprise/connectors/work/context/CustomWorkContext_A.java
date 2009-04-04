package com.sun.enterprise.connectors.work.context;

import javax.resource.spi.work.WorkContext;


public class CustomWorkContext_A implements WorkContext {
    public String getName() {
        return "CustomWorkContext_A";
    }

    public String getDescription() {
        return "CustomWorkContext_A";
    }
}
