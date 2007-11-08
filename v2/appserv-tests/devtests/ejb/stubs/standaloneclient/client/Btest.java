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
