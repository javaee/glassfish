/*
 * TestProgressObjectImpl.java
 *
 * Created on January 15, 2004, 10:10 AM
 */

import javax.enterprise.deploy.spi.status.ProgressListener;
import org.glassfish.deployapi.ProgressObjectImpl;
import org.glassfish.deployapi.TargetImpl;
import org.glassfish.deployment.client.DeploymentFacilityFactory;
import org.glassfish.deployment.client.DeploymentFacility;
import org.glassfish.deployment.client.AbstractDeploymentFacility;
import javax.enterprise.deploy.shared.StateType;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 *Makes sure that the ProgressObjectImpl class functions correctly.
 *<p>
 *In particular, bug 4977764 reported that the ProgressObjectImpl class was susceptible to 
 *concurrent update failures in the vector that holds registered progress listeners.  The
 *fireProgressEvent method worked with the vector of listeners itself rather than a clone of the vector.
 *One of the listeners unregistered itself from the progress object, so when the iterator tried to
 *get the next element it detected the concurrent update.   So, the progress object now clones the vector
 *temporarily in fireProgressEvent and iterates through the clone.
 *
 * @author  tjquinn
 */
public class TestProgressObjectImpl {

    private static final String here = "devtests/deployment/jsr88/misc";
    
    /**
     *Provides a concrete implementation of the progress object for testing.  Note that the behavior being
     *tested is actually that of the superclass ProgressObjectImpl.
     */
    public class MyProgressObjectImpl extends ProgressObjectImpl {
       
        public MyProgressObjectImpl(TargetImpl target) {
            super(target);
        }
        
        /**
         *Required by the abstract class definition but not used during testing.
         */
        public void run() {}
        
        /**
         *Stands in as an operation that fires an event to registered listeners.
         */
        public void act() {
            fireProgressEvent(StateType.RUNNING, "starting");
            /*
             *This is where any real work would be done.  It's useful to test with two events just in case
             *that would uncover any problems.
             */
            fireProgressEvent(StateType.COMPLETED, "done");
        }
    }
    
    /**
     *Adds a new listener during the event handling.
     */
    public class MeddlingListenerAdder implements ProgressListener {
        
    
        public void handleProgressEvent(javax.enterprise.deploy.spi.status.ProgressEvent progressEvent) {
            /*
             *Meddle in the listener list by adding a new listener to the list.  This should trigger the error
             *in the original version of ProgressObjectImpl but should not in the fixed version.
             */
            TestProgressObjectImpl.this.theProgressObjectImpl.addProgressListener(new TestProgressObjectImpl.MeddlingListenerRemover());
        }
        
    }
    
    /**
     *Removes itself as a listener during the event handling.
     */
    public class MeddlingListenerRemover implements ProgressListener {
        
    
        public void handleProgressEvent(javax.enterprise.deploy.spi.status.ProgressEvent progressEvent) {
            /*
             *Meddle in the listener list by removing itself from the list.  This should trigger the error
             *in the original version of ProgressObjectImpl but should not in the fixed version.
             */
            TestProgressObjectImpl.this.theProgressObjectImpl.removeProgressListener(this);
        }
        
    }
    
    /* Local progress object implementation to be tested. */
    private MyProgressObjectImpl theProgressObjectImpl;
        
    /** Creates a new instance of TestProgressObjectImpl */
    public TestProgressObjectImpl() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestProgressObjectImpl test = new TestProgressObjectImpl();
        try {
            test.run(args);
            test.pass();
        } catch (Throwable th) {
            th.printStackTrace(System.out);
            test.fail();
        }
    }
    
    public void run(String[] args) {
        addNewListenerDuringEventHandling();
    }

    /**
     * Tamper with the listener list by adding a new listener during event handling.  
     */
    private void addNewListenerDuringEventHandling() {
        /*
         *Create a TargetImpl just to satisfy the signature of the constructor for the progress object implementation.
         */
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        TargetImpl target = new TargetImpl((AbstractDeploymentFacility)df, "test", "test");
        theProgressObjectImpl = new MyProgressObjectImpl(target);
        TestProgressObjectImpl.MeddlingListenerAdder meddlingListener1 = new TestProgressObjectImpl.MeddlingListenerAdder();
        TestProgressObjectImpl.MeddlingListenerAdder meddlingListener2 = new TestProgressObjectImpl.MeddlingListenerAdder();
        
        theProgressObjectImpl.addProgressListener(meddlingListener1);
        theProgressObjectImpl.addProgressListener(meddlingListener2);
        
        /*
         *Fire an event that will change the listener set.
         */
        theProgressObjectImpl.act();
        
    }
    
    private void log(String message) {
        System.out.println("[TestProgressObjectImpl]:: " + message);
    }

    private void pass() {
        log("PASSED: " + here);
        System.exit(0);
    }

    private void fail() {
        log("FAILED: " + here);
        System.exit(-1);
    }
}
