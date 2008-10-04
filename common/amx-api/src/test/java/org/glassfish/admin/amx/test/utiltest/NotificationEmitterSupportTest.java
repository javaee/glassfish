/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package org.glassfish.admin.amx.test.utiltest;

import com.sun.appserv.management.util.jmx.NotificationEmitterSupport;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.RunnableBase;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class NotificationEmitterSupportTest
        extends junit.framework.TestCase {
    public NotificationEmitterSupportTest() {
    }

    public void
    setUp()
            throws Exception {
    }

    public void
    tearDown()
            throws Exception {
    }

    private static final class MyListener
            implements NotificationListener {
        private final BlockingQueue<Notification> mReceived;

        public MyListener() {
            mReceived = new ArrayBlockingQueue<Notification>(NUM_NOTIFS);
        }

        public void
        handleNotification(
                final Notification notif,
                final Object handback) {
            mReceived.add(notif);
        }

        public Notification[]
        take(final int numRequired) {
            if (numRequired == 0) {
                throw new IllegalArgumentException();
            }

            final List<Notification> items = new ArrayList<Notification>();

            int numTaken = 0;
            Notification notif;
            while (numTaken < numRequired) {
                try {
                    notif = mReceived.take();
                }
                catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
                items.add(notif);
                ++numTaken;
            }

            final Notification[] itemsArray = new Notification[items.size()];
            items.toArray(itemsArray);
            return itemsArray;
        }
    }


    private Notification[]
    makeNotifications(final int num) {
        final Notification[] items = new Notification[num];
        for (int i = 0; i < items.length; ++i) {
            items[i] = new Notification("TEST-" + i, this, i, System.currentTimeMillis());
        }
        return items;
    }

    private void
    sendNotifications(
            final Notification[] items,
            final NotificationEmitterSupport emitter) {
        for (int i = 0; i < items.length; ++i) {
            emitter.sendNotification(items[i]);
        }
    }
    
    /** use a big number which might help catch race conditions */
    private static final int NUM_NOTIFS = 2000;

    private void
    doTest(final boolean async) {
        final MyListener listener = new MyListener();
        final NotificationEmitterSupport emitter = new NotificationEmitterSupport(async);
        final NotificationFilter filter = null;
        final Object handback = this;
        emitter.addNotificationListener(listener, filter, handback);

        final Notification[] itemsToSend = makeNotifications(NUM_NOTIFS);
        sendNotifications(itemsToSend, emitter);
        emitter.sendAll();  // ensure that all have been sent
        emitter.cleanup();  // make sure it's done

        final Notification[] itemsReceived = listener.take(NUM_NOTIFS);
        assert itemsReceived.length == itemsToSend.length :
                "Expected " + itemsToSend.length + ", but received only " + itemsReceived.length;

        final Set<Notification> itemsSentSet = GSetUtil.newSet(itemsToSend);
        final Set<Notification> itemsReceivedSet = GSetUtil.newSet(itemsReceived);

        assert itemsSentSet.equals(itemsReceivedSet) :
                "Items sent are not the same as the items received";
    }

/*
    public void
    testSync()
            throws Exception {
        doTest(false);
    }
*/

    private final class EmitterThread
            extends RunnableBase {
        EmitterThread(final String name) {
            super(name);
        }

        protected void doRun() {
            doTest(true);
        }
    }

    public void
    testAsync()
            throws Exception {
        final int numEmitters = 50;

        // create them all first...
        final EmitterThread[] emitters = new EmitterThread[numEmitters];
        for (int i = 0; i < numEmitters; ++i) {
            emitters[i] = new EmitterThread("emitter-" + i);
        }
        
        // let them fight it out by starting them all at once
        for (int i = 0; i < numEmitters; ++i) {
            emitters[i].submit(RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD);
        }

        for (int i = 0; i < numEmitters; ++i) {
            final Throwable t = emitters[i].waitDone();
            if ( t != null )
            {
                t.printStackTrace();
            }
            assert t == null : "Throwable from EmitterThread-" + i + ": " + ExceptionUtil.toString(t);
        }
    }

}


























