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

/* DynamicReconfigurator.java
 * $Id: DynamicReconfigurator.java,v 1.3 2005/12/25 03:43:38 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:43:38 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim - 
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio - 
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

package com.sun.enterprise.admin.monitor.registry.spi.reconfig;

import com.sun.enterprise.admin.monitor.registry.StatsHolder;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;

import com.sun.enterprise.admin.monitor.registry.spi.ValueListMap;

/**
 *
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
public final class DynamicReconfigurator implements ChangeHandler {
	
	private final ValueListMap listeners;
	private ChangeHandler successor;
	
	public DynamicReconfigurator(ValueListMap listeners) {
		this.listeners	= listeners;
		chain();
	}
	public void addListener(MonitoredObjectType type, MonitoringLevelListener listener) {
		if (listeners != null)
			listeners.put(type, listener);
	}
	public void removeListener(MonitoringLevelListener listener) {
		listeners.remove(listener);
	}
	public void handleChange(MonitoredObjectType type, MonitoringLevel from, MonitoringLevel to) {
		successor.handleChange(type, from, to);
	}
	
	private void chain() {
		// exercise care while forming this chain.
		final ChangeHandler c	= new EmptyChangeHandler();
        final ChangeHandler j   = new JVMChangeHandler(c, listeners);
		final ChangeHandler w	= new WebContainerChangeHandler(j, listeners);
		final ChangeHandler e	= new EjbContainerChangeHandler(w, listeners);
		final ChangeHandler ts	= new TransactionServiceChangeHandler(e, listeners);
		final ChangeHandler hs	= new HttpServiceChangeHandler(ts, listeners);		
		final ChangeHandler orb	= new OrbChangeHandler(hs, listeners);
		final ChangeHandler tp	= new ThreadPoolChangeHandler(orb, listeners);
        final ChangeHandler cp	= new ConnectionPoolChangeHandler(tp, listeners);
		this.successor = cp;
	}
}
