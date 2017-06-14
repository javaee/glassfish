// HelloServer.java
// Copyright and License 
import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import javax.naming.*;



import java.util.*;


class HelloImpl extends HelloPOA {
  private ORB orb;

  public void setORB(ORB orb_val) {
    orb = orb_val; 
  }
    
  // implement sayHello() method
  public String sayHello() {
    return "\nHello world !!\n";
  }
    
  // implement shutdown() method
  public void shutdown() {
    orb.shutdown(false);
  }
}


public class HelloServer {

  public static void main(String args[]) {
    try{

      Properties p = System.getProperties();
      // add runtime properties here
      p.put("org.omg.CORBA.ORBClass", "com.inprise.vbroker.orb.ORB");
      p.put("org.omg.CORBA.ORBSingletonClass", "com.inprise.vbroker.orb.ORBSingleton");
      
      // create and initialize the ORB		
      ORB orb = ORB.init( args, p );
      Hashtable env = new Hashtable();
      env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.cosnaming.CNCtxFactory");
      env.put("java.naming.corba.orb", orb);
      
      
      // get reference to rootpoa & activate the POAManager
      POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
      rootpoa.the_POAManager().activate();
      
      // create servant and register it with the ORB
      HelloImpl helloImpl = new HelloImpl();
      helloImpl.setORB(orb); 
      
      // get object reference from the servant
      org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloImpl);
      Hello href = HelloHelper.narrow(ref);
      
      // get the root naming context
      // NameService invokes the name service
           org.omg.CORBA.Object objRef =
          orb.resolve_initial_references("NameService");
      // Use NamingContextExt which is part of the Interoperable
      // Naming Service (INS) specification.
      NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

      // bind the Object Reference in Naming
      String name = "Hello";
      NameComponent path[] = ncRef.to_name( name );
      ncRef.rebind(path, href);

     
      javax.naming.Context nctx = new InitialContext(env);
      System.out.println("Bind the HelloServer object to "
          + "name " + "Hello" + "....");
      nctx.rebind("Hello", href);
      

      System.out.println("HelloServer ready and waiting ...");

      // wait for invocations from clients
      orb.run();
    } 
	
      catch (Exception e) {
        System.err.println("ERROR: " + e);
        e.printStackTrace(System.out);
      }
	  
      System.out.println("HelloServer Exiting ...");
	
  }
}
 

