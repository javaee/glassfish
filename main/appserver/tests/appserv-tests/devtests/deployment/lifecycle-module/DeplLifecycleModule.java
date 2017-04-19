import com.sun.appserv.server.*;

/**
 * This is a dummy implementation for the LifecycleListener interface.
 */

public class DeplLifecycleModule implements LifecycleListener {

  public void handleEvent(LifecycleEvent event) throws ServerLifecycleException {
    System.out.println("got event" + event.getEventType() + " event data: "
      + event.getData());

    if (LifecycleEvent.INIT_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: INIT_EVENT");
      return;
    }

    if (LifecycleEvent.STARTUP_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: STARTUP_EVENT");
      return;
    }

    if (LifecycleEvent.READY_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: READY_EVENT");
      return;
    }

    if (LifecycleEvent.SHUTDOWN_EVENT== event.getEventType()) {
      System.out.println("DeplLifecycleListener: SHUTDOWN_EVENT");
      return;
    }

    if (LifecycleEvent.TERMINATION_EVENT == event.getEventType()) {
      System.out.println("DeplLifecycleListener: TERMINATE_EVENT");
      return;
    }
  }
} 
