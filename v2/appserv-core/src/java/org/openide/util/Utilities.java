/*
 * Utilities.java
 *
 * Created on October 26, 2006, 3:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.openide.util;


import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
    
/**
 *
 * @author dochez
 */
public class Utilities {
    
    private static ActiveQueue activeReferenceQueue;
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }
    
/**
     * Useful queue for all parts of system that use <code>java.lang.ref.Reference</code>s
     * together with some <code>ReferenceQueue</code> and need to do some clean up
     * when the reference is enqueued. Usually, in order to be notified about that, one
     * needs to either create a dedicated thread that blocks on the queue and is
     * <code>Object.notify</code>-ed, which is the right approach but consumes
     * valuable system resources (threads) or one can periodically check the content
     * of the queue by <code>RequestProcessor.Task.schedule</code> which is
     * completely wrong, because it wakes up the system every (say) 15 seconds.
     * In order to provide useful support for this problem, this queue has been
     * provided.
     * <P>
     * If you have a reference that needs cleanup, make it implement <link>Runnable</link>
     * and register it with the queue:
     * <PRE>
     * class MyReference extends WeakReference<Thing> implements Runnable {
     *     private final OtherInfo dataToCleanUp;
     *     public MyReference(Thing ref, OtherInfo data) {
     *         super(ref, Utilities.activeReferenceQueue());
     *         dataToCleanUp = data;
     *     }
     *     public void run() {
     *         dataToCleanUp.releaseOrWhateverYouNeed();
     *     }
     * }
     * </PRE>
     * When the <code>ref</code> object is garbage collected, your run method
     * will be invoked by calling
     * <code>((Runnable) reference).run()</code>
     * and you can perform whatever cleanup is necessary. Be sure not to block
     * in such cleanup for a long time as this prevents other waiting references
     * from cleaning themselves up.
     * <P>
     * Do not call any <code>ReferenceQueue</code> methods. They
     * will throw exceptions. You may only enqueue a reference.
     * <p>
     * Be sure to call this method anew for each reference.
     * Do not attempt to cache the return value.
     * @since 3.11
     */
    public static synchronized ReferenceQueue<Object> activeReferenceQueue() {
        if (activeReferenceQueue == null) {
            activeReferenceQueue = new ActiveQueue(false);
        }

        activeReferenceQueue.ping();

        return activeReferenceQueue;
    }
    
/** Implementation of the active queue.
     */
    private static final class ActiveQueue extends ReferenceQueue<Object> implements Runnable {

        private static final Logger LOGGER = Logger.getLogger(ActiveQueue.class.getName().replace('$', '.'));

        /** number of known outstanding references */
        private int count;
        private boolean deprecated;

        public ActiveQueue(boolean deprecated) {
            this.deprecated = deprecated;
        }

        public Reference<Object> poll() {
            throw new UnsupportedOperationException();
        }

        public Reference<Object> remove(long timeout) throws IllegalArgumentException, InterruptedException {
            throw new InterruptedException();
        }

        public Reference<Object> remove() throws InterruptedException {
            throw new InterruptedException();
        }

        public void run() {
            while (true) {
                try {
                    Reference<?> ref = super.remove(0);
                    LOGGER.finer("dequeued reference");

                    if (!(ref instanceof Runnable)) {
                        LOGGER.warning(
                            "A reference not implementing runnable has been added to the Utilities.activeReferenceQueue(): " +
                            ref.getClass() // NOI18N
                        );

                        continue;
                    }

                    if (deprecated) {
                        LOGGER.warning(
                            "Utilities.ACTIVE_REFERENCE_QUEUE has been deprecated for " + ref.getClass() +
                            " use Utilities.activeReferenceQueue" // NOI18N
                        );
                    }

                    // do the cleanup
                    try {
                        ((Runnable) ref).run();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Should not happen.
                        // If it happens, it is a bug in client code, notify!
                        LOGGER.log(Level.WARNING, null, t);
                    } finally {
                        // to allow GC
                        ref = null;
                    }
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }

                synchronized (this) {
                    assert count > 0;
                    count--;
                    if (count == 0) {
                        // We have processed all we have to process (for now at least).
                        // Could be restarted later if ping() called again.
                        // This could also happen in case someone called activeReferenceQueue() once and tried
                        // to use it for several references; in that case run() might never be called on
                        // the later ones to be collected. Can't really protect against that situation.
                        // See issue #86625 for details.
                        LOGGER.fine("stopping thread");
                        break;
                    }
                }
            }
        }

        synchronized void ping() {
            if (count == 0) {
                Thread t = new Thread(this, "Active Reference Queue Daemon"); // NOI18N
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(true); // to not prevent exit of VM
                t.start();
                // Note that this will not be printed during IDE startup because
                // it happens before logging is even initialized.
                LOGGER.fine("starting thread");
            } else {
                LOGGER.finer("enqueuing reference");
            }
            count++;
        }

    }
    
    
}
