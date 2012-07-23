/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session3;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =  new SimpleReporterAdapter("appserv-tests");

    public Client(String[] args) {
    }

    public static void main(String[] args) throws Exception {
        stat.addDescription("connectore-resource-definitionclient");
        Client client = new Client(args);
        stat.printSummary("connectore-resource-definitionclient");
    }

}

