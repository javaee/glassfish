/**
 * test server lifecycle event listener
 */
import java.util.Properties;
import java.io.InputStream;

import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleEventContext;
import com.sun.appserv.server.ServerLifecycleException;

public class TestLifecycleModule implements LifecycleListener {

    // receive a server lifecycle event 
    public void handleEvent(LifecycleEvent event) throws ServerLifecycleException {

	LifecycleEventContext ctx = event.getLifecycleEventContext();
        if (LifecycleEvent.INIT_EVENT == event.getEventType()) {
	    System.out.println("*");
	    System.out.println("*");
            ctx.log("TestLifecycleModule: INIT_EVENT PASSED");
	    System.out.println("*");
	    System.out.println("*");
            return;
        }

        if (LifecycleEvent.STARTUP_EVENT == event.getEventType()) {
	    System.out.println("*");
	    System.out.println("*");
            ctx.log("TestLifecycleModule: STARTUP_EVENT PASSED");
	    System.out.println("*");
	    System.out.println("*");
            return;
        }

        if (LifecycleEvent.READY_EVENT == event.getEventType()) {
	    System.out.println("*");
	    System.out.println("*");
            ctx.log("TestLifecycleModule: READY_EVENT PASSED");
	    System.out.println("*");
	    System.out.println("*");

            return;
        }
        if (LifecycleEvent.SHUTDOWN_EVENT== event.getEventType()) {
	    System.out.println("*");
	    System.out.println("*");
            ctx.log("TestLifecycleModule: SHUTDOWN_EVENT PASSED");
	    System.out.println("*");
	    System.out.println("*");

            return;
        }

        if (LifecycleEvent.TERMINATION_EVENT == event.getEventType()) {
	    System.out.println("*");
	    System.out.println("*");
            ctx.log("TestLifecycleModule: TERMINATION_EVENT PASSED");
	    System.out.println("*");
	    System.out.println("*");

            return;
        }
   }
}
