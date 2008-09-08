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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.admin.connector.rmi;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public final class DumpRegistryBindings {
    // java -cp target/classes org.glassfish.admin.connector.rmi.DumpRegistryBindings <port>
    public static void main(final String[] args)
    {
        try
        {
            final int port = args.length == 1 ? Integer.parseInt(args[0]) : 8686;
            dumpRegistryBindings( "main", port );
        }
        catch( final Throwable t )
        {
            t.printStackTrace();
        }
    }
    
    public static void dumpRegistryBindings( final String msg, final int port ) throws Exception
    {
        final Registry r = LocateRegistry.getRegistry("localhost", port);
        final String[] bindings = r.list();
        if ( bindings.length != 0 )
        {
            System.out.println( "dumpRegistryBindings: " +msg + ": " + port  );
            for ( final String binding : bindings )
            {
                System.out.println( binding );
            }
        }
        else
        {
            System.out.println( "dumpRegistryBindings: " + msg + ": NONE ");
        }
    }
}
