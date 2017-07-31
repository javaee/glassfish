/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.flashlight.provider;

import org.jvnet.hk2.annotations.Service;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Mahesh Kannan
 */
@Service
public class ProbeProviderEventManager {

    private Collection<ProbeProviderListener> listeners = new ArrayList<ProbeProviderListener>();

    private Set<String> registeredTuples = new HashSet<String>();

    public synchronized void registerProbeProviderListener(ProbeProviderListener listener) {
        listeners.add(listener);
        for (String str : registeredTuples) {
            String[] names = str.split(":");
            String m1 = null;
            String p1 = null;
            String a1 = null;
            if (names.length >= 1) {
                m1 = names[0].length() == 0 ? null : names[0];
            }
            if (names.length >= 2) {
                p1 = names[1].length() == 0 ? null : names[1];
            }
            if (names.length >= 3) {
                a1 = names[2].length() == 0 ? null : names[2];
            }
            listener.providerRegistered(m1, p1, a1);
            //System.out.println("Notifying listener");
        }
    }

    public synchronized void unregisterProbeProviderListener(ProbeProviderListener listener) {
        listeners.remove(listener);
    }

    public synchronized void notifyListenersOnRegister(String moduleName, String providerName, String appName) {
        String moduleName1 = moduleName == null ? "" : moduleName;
        String providerName1 = providerName == null ? "" : providerName;
        String appName1 = appName == null ? "" : appName;
        for (ProbeProviderListener listener : listeners) {
            listener.providerRegistered(moduleName, providerName, appName);
        }
        registeredTuples.add(moduleName1 + ":" + providerName1 + ":" + appName1);
    }

    public synchronized void notifyListenersOnUnregister(String moduleName, String providerName, String appName) {
        String moduleName1 = moduleName == null ? "" : moduleName;
        String providerName1 = providerName == null ? "" : providerName;
        String appName1 = appName == null ? "" : appName;
        for (ProbeProviderListener listener : listeners) {
            listener.providerUnregistered(moduleName, providerName, appName);
        }
        registeredTuples.remove(moduleName1 + ":" + providerName1 + ":" + appName1);
    }

}
