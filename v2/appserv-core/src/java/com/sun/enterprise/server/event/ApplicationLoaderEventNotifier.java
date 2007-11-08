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

/*
 * @(#) AbstractLoader.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.server.event;

import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

public class ApplicationLoaderEventNotifier {

    private static Logger _logger =
	LogDomains.getLogger(LogDomains.LOADER_LOGGER);

    private static ApplicationLoaderEventNotifier _notifier =
	new ApplicationLoaderEventNotifier();

    private ArrayList listeners = new ArrayList();
    
    private ArrayList appclientListeners = new ArrayList();

    private ApplicationLoaderEventNotifier() {
    }

    public static ApplicationLoaderEventNotifier getInstance() {
	return _notifier;
    }

    public void addListener(ApplicationLoaderEventListener listener) {
	synchronized (listeners) {
	    listeners.add(listener);
	}
    }

    public void removeListener(ApplicationLoaderEventListener listener) {
	synchronized (listeners) {
	    listeners.remove(listener);
	}
    }
    
    public void addListener(ApplicationClientLoaderEventListener listener) {
	synchronized (appclientListeners) {
	    appclientListeners.add(listener);
	}
    }

    public void removeListener(ApplicationClientLoaderEventListener listener) {
	synchronized (appclientListeners) {
	    appclientListeners.remove(listener);
	}
    }

    public void notifyListeners(ApplicationEvent event) {
	ArrayList myListeners = null;

	_logger.log(Level.FINE, "LoaderEventNotifier: " + event);

	synchronized (listeners) {
	    myListeners = (ArrayList) listeners.clone();
	}

	int sz = myListeners.size();
	for (int i=0; i<sz; i++) {
	   ApplicationLoaderEventListener listener =
	       (ApplicationLoaderEventListener) myListeners.get(i);

	   try {
	       listener.handleApplicationEvent(event);
	   } catch (Exception ex) {
	       _logger.log(Level.WARNING, "Exception during "
		       + "handleApplicationEvent", ex);
	   }
	}
    }

    public void notifyListeners(EjbContainerEvent event) {
	ArrayList myListeners = null;

	_logger.log(Level.FINE, "LoaderEventNotifier: " + event);

	synchronized (listeners) {
	    myListeners = (ArrayList) listeners.clone();
	}

	int sz = myListeners.size();
	for (int i=0; i<sz; i++) {
	   ApplicationLoaderEventListener listener =
	       (ApplicationLoaderEventListener) myListeners.get(i);

	   try {
	       listener.handleEjbContainerEvent(event);
	   } catch (Exception ex) {
	       _logger.log(Level.WARNING, "Exception during "
		       + "handleEjbContainerEvent", ex);
	   }
	}
    }

    public void notifyListeners(ApplicationClientEvent event) {
	ArrayList myListeners = null;

	_logger.log(Level.FINE, "LoaderEventNotifier: " + event);

	synchronized (appclientListeners) {
	    myListeners = (ArrayList) appclientListeners.clone();
	}

	int sz = myListeners.size();
	for (int i=0; i<sz; i++) {
	   ApplicationClientLoaderEventListener listener =
	       (ApplicationClientLoaderEventListener) myListeners.get(i);

	   try {
	       listener.handleApplicationClientEvent(event);
	   } catch (Exception ex) {
	       _logger.log(Level.WARNING, "Exception during "
		       + "handleApplicationClientEvent", ex);
	   }
	}
    }
}
