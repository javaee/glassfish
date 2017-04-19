/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package libclasspath2.client;

import java.io.IOException;
import javax.ejb.EJB;
import libclasspath2.ResourceHelper;
import libclasspath2.ejb.LookupSBRemote;

/**
 *
 * @author tjquinn
 */
public class LibDirTestClient {

    private static @EJB() LookupSBRemote lookup;
    
    /** Creates a new instance of LibDirTestClient */
    public LibDirTestClient() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new LibDirTestClient().run(args);
        } catch (Throwable thr) {
            thr.printStackTrace(System.err);
            System.exit(-1);
        }
    }
    
    private void run(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("result=-1");
            System.out.println("note=You must specify the expected result string as the command-line argument");
            System.exit(1);
        }
        String expected = args[0];

        /*
         *Ask the EJB to find the required resources and property values.
         */
        ResourceHelper.Result serverResult = lookup.runTests(args, ResourceHelper.TestType.SERVER);
        
        /*
         *Now try to get the same results on the client side.
         */
        StringBuilder clientResults = new StringBuilder();
        ResourceHelper.Result clientResult = ResourceHelper.checkAll(args, ResourceHelper.TestType.CLIENT);
        
        if (serverResult.getResult() && clientResult.getResult()) {
            System.out.println("result=0");
            System.out.println("note=Received expected results");
        } else {
            System.out.println("result=-1");
            dumpResults(serverResult.getResult(), serverResult.getResults().toString(), "server");
            dumpResults(clientResult.getResult(), clientResult.getResults().toString(), "client");
            System.exit(1);
        }
    }

    private void dumpResults(boolean result, String results, String whereDetected) {
        if ( ! result) {
            System.out.println("note=Error(s) on the " + whereDetected + ":");
            for (String error : results.split("@")) {
                System.out.println("note=  " + error);
            }
            System.out.println("note=");
        }
        
    }
}
