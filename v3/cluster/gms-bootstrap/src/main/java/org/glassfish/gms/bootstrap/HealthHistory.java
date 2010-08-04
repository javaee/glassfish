/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.gms.bootstrap;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.ee.cms.core.*;
import com.sun.logging.LogDomains;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to hold cluster history. This information is backed by
 * a ConcurrentMap, so iterating over the instances is
 * "weakly consistent" as the state could change at any time
 * (especially during cluster startup).
 */
public final class HealthHistory {

    final static Logger logger = LogDomains.getLogger(
        HealthHistory.class, LogDomains.CORE_LOGGER);

    /**
     * NOT_RUNNING means there is no time information associated
     */
    public static enum STATE { NOT_RUNNING,
        RUNNING, REJOINED, FAILURE, SHUTDOWN };

    /**
     * Used when no time information is known, for instance at
     * cluster startup before an instance has started. 
     */
    public static final long NOTIME = -1l;
    
    private final ConcurrentMap<String, InstanceHealth> healthMap;

    /*
     * Creates a health history that knows about the expected
     * list of instances. This is called from the GMS adapter
     * during initialization, before 
     */
    public HealthHistory(List<Server> servers) {
        healthMap = new ConcurrentHashMap<String, InstanceHealth>(
            servers.size());
        for (Server server : servers) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, String.format(
                    "instance name in HealthHistory constructor %s",
                    server.getName()));
            }
            if (healthMap.putIfAbsent(server.getName(),
                new InstanceHealth(STATE.NOT_RUNNING, NOTIME)) != null) {
                logger.log(Level.WARNING,
                    "duplicate.instance", server.getName());
            }
        }
    }

    /**
     * Returns the state/time of a specific instance.
     */
    public InstanceHealth getHealthByInstance(String name) {
        return healthMap.get(name);
    }

    /**
     * The returned list may be modified without affecting
     * the information in the HealthHistory object.
     */
    public List<String> getInstancesByState(STATE targetState) {
        List<String> retVal = new ArrayList<String>(healthMap.size());
        for (String name : healthMap.keySet()) {
            if (healthMap.get(name).state == targetState) {
                retVal.add(name);
            }
        }
        return retVal;
    }

    /**
     * Returns a copy of the instance names. 
     */
    public Set<String> getInstances() {
        return Collections.unmodifiableSet(healthMap.keySet());
    }

    /**
     * Called by GMS subsystem to update the health of an instance.
     *
     * TODO: add try/catch around everything for safety
     */
    public void updateHealth(Signal signal) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "signal: " + signal.toString());
        }
        String name = signal.getMemberToken();
        long time = signal.getStartTime();
        STATE state = null;

        // a little if-elsie....
        if (signal instanceof  JoinNotificationSignal) {
            /*
             * Means an instance is running. We will usually get a
             * JoinedAndReadyNotificationSignal after this in a usual
             * startup. If not, it means the DAS is restarting and
             * the cluster is already up. In that case, we need
             * the original startup time, not the time of the signal.
             */
            state = STATE.RUNNING;
        }   else if (signal instanceof JoinedAndReadyNotificationSignal) {
            /*
             * During a normal startup, this will occur after the
             * JoinNotificationSignal. If it's not a Rejoin, we
             * don't need to process the data since it's already
             * happened during the Join event. But it doesn't hurt.
             */
            JoinedAndReadyNotificationSignal jar =
                (JoinedAndReadyNotificationSignal) signal;
            RejoinSubevent sub = jar.getRejoinSubevent();
            if (sub == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "it's a joined and ready");
                }
                state = STATE.RUNNING;
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "it's a rejoin");
                }
                state = STATE.REJOINED;
                time = sub.getGroupJoinTime();
            }
        } else if (signal instanceof FailureNotificationSignal) {
            state = STATE.FAILURE;
        } else if (signal instanceof PlannedShutdownSignal) {
            state = STATE.SHUTDOWN;
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, String.format(
                    "Signal %s not handled in updateHealth",
                    signal.toString()));
            }
            return;
        }
        InstanceHealth ih = new InstanceHealth(state, time);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, String.format(
                "bobbyb: updating health with %s : %s for signal %s",
                name, ih.toString(), signal.toString()));
        }
        if (healthMap.put(name, ih) == null) {
            logger.log(Level.INFO, "unknown.instance",
                new Object [] {state, name});
        }
    }

    /*
     * Information in an InstanceHealth object is immutable. For
     * convenience, the fields are public for direct access.
     */
    public static final class InstanceHealth {

        /**
         * The last-known state of the instance.
         */
        public final STATE state;

        /**
         * The time, if known, corresponding to the last change in state.
         */
        public final long time;

        InstanceHealth(STATE state, long time) {
            this.state = state;
            this.time = time;
        }

        @Override
        public String toString() {
            return String.format("InstanceHealth: state '%s' time '%s'",
                state, new Date(time).toString());
        }
    }
}
