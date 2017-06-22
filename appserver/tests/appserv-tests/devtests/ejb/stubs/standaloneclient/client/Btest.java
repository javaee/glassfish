/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

import java.util.Properties;

import java.io.PrintStream;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;

import javax.rmi.PortableRemoteObject;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;


public class Btest{

    public static  Context _context=null;

    public static void main(String[] args)
    {
       try {
           // Step 1:  It is important to call initailizeSystemProperties to 
           // avoid problems with switching ORBs between J2SE and AppServer. 
           // These are system properties that needs to be run once or these 
           // properties can be passed through -D flags
           initializeSystemProperties( );
        
           // Step 2: Now do the EJB lookup, and call the methods on the 
           // Metadata Object
           getContext();

           Object boundObj = doLookup("ejb/ejb_stubs_ejbapp_HelloBean");
           EJBHome home = getHome( boundObj );
           getMetaData( home );

           // Step 3:  Repeat Step 2 as many times as needed. Although the 
           // advise is to pass "javax.naming.CORBA.ORB" property
           // to avoid huge memory footprint due to new initialContexts. 
           // Remember every new InitialContext will initialize a new ORB
           // which is very expensive memory wise.
/*PG->
           for( int i = 0; i < 3; i++ ) {
               _context = null;
               System.out.println( "Iteration Number: " + i );
               getContext();
               boundObj= doLookup("greeter");
               home = getHome( boundObj );
               getMetaData( home );
         }
*/
           
       } catch ( Exception e ) {
          e.printStackTrace( );
          System.out.println( e );
       }
    }


    // Initialize to use SUN ONE AppServer 7 ORB and UtilDelegate
    // NOTE: All these are OMG standard properties provided to plug in an ORB 
    // to JDK
    private static void initializeSystemProperties( ) {
        System.setProperty( "org.omg.CORBA.ORBClass",
            "com.sun.corba.ee.impl.orb.ORBImpl" );
        System.setProperty( "javax.rmi.CORBA.UtilClass", 
            "com.sun.corba.ee.impl.javax.rmi.CORBA.Util" );

        System.setProperty( "javax.rmi.CORBA.StubClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl");  
        System.setProperty( "javax.rmi.CORBA.PortableRemoteClass",
            "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject");
    }

    //GetContext
    public static void getContext()
    {
        Properties _props = new Properties();
        _props.put(Context.INITIAL_CONTEXT_FACTORY, 
            "com.sun.jndi.cosnaming.CNCtxFactory");
        _props.put(Context.PROVIDER_URL, "iiop://achumba:3700");

        try {
            _context = new InitialContext(_props);
        } catch(NamingException ne) {
            System.out.println("test - 'new InitialContext()'" + 
                "threw this: type(" + ne.getClass().getName() +
                "), msg(" + ne.getMessage() + ")");
            Throwable rootCause = ne.getRootCause();
            System.out.println("test.initialContext -" + 
                "root cause of previous exception: " + "type(" + 
                rootCause.getClass().getName() + "), msg(" + 
                rootCause.getMessage() + ")");
        }
    }
 

    //DoLookup

    public static Object doLookup(String inName)
    {
        Object boundObj =null;
        try {
            boundObj = _context.lookup(inName);
        } catch(NamingException ne) {
            System.out.println("test - 'lookup()'" + "threw this: type(" + 
                ne.getClass().getName() + "), msg(" + ne.getMessage() + ")");
            Throwable rootCause = ne.getRootCause();
            System.out.println("test.lookup -" + 
                "root cause of previous exception: " + "type(" + 
                rootCause.getClass().getName() + "), msg(" + 
                rootCause.getMessage() + ")");
        }
        return boundObj;
    }

    //GetEJBHome

    public static EJBHome getHome(Object boundObj)
    {
        EJBHome hboundHome = (EJBHome) javax.rmi.PortableRemoteObject.narrow(
            boundObj, EJBHome.class);
        System.out.println(hboundHome.toString());
        return hboundHome;
    }


    //GetEJBMetaData
    public static void getMetaData(EJBHome formalboundHome)
    {
        EJBMetaData meta=null;
        try {
            meta = formalboundHome.getEJBMetaData();
            System.out.println( "meta.getClass().getName() = " + 
                meta.getClass().getName() );
        } catch(Exception ne) {
            ne.printStackTrace();
            System.out.println("test - 'metadata()'" + "threw this: type(" + 
                ne.getClass().getName() + "), msg(" + ne.getMessage() + ")");
        }
        if(meta != null)
        {
            System.out.println(meta.toString());

            Class tempClass = null;
            try {
                tempClass = meta.getHomeInterfaceClass();
            } catch(Error e) {
                System.out.println( 
                    "found EJB Home,meta.getHomeInterfaceClass() " + 
                    e.getMessage() );
                tempClass = null;
            }
            boolean isSessionBean = meta.isSession();
            System.out.println("Session Bean"+isSessionBean);
            System.out.println("HomeInteface"+tempClass);
            meta = null;
        }
    } 

}
