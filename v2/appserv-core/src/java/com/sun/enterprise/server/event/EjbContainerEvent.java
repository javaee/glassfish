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
 * @(#) ApplicationLoaderEventListener.java
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

import com.sun.enterprise.deployment.EjbDescriptor;

    /*
     * An EjbContainerEvent is generated whenever an EjbContainer
     *	is loaded / unloaded.
     *
     *	EjbContainerEvent.getEventType() specifies the exact
     *	EjbContainer event.
     *
     * Generally, EjbContainer Events are sent in the following order
     *  a) BEFORE_EJB_CONTAINER_LOADED
     *  b) AFTER_EJB_CONTAINER_LOADED
     *  c) BEFORE_EJB_CONTAINER_UNLOADED
     *  d) AFTER_EJB_CONTAINER_UNLOADED
     *
     */
public class EjbContainerEvent {

    public static final int BEFORE_EJB_CONTAINER_LOAD = 20;
    public static final int AFTER_EJB_CONTAINER_LOAD = 21;
    public static final int BEFORE_EJB_CONTAINER_UNLOAD = 22;
    public static final int AFTER_EJB_CONTAINER_UNLOAD = 23;

    private int		    eventType;
    private EjbDescriptor   ejbDescriptor;
    private ClassLoader	    loader;

    public EjbContainerEvent(int eventType, EjbDescriptor ejbDescriptor,
	    ClassLoader loader)
    {
	this.eventType = eventType;
	this.ejbDescriptor = ejbDescriptor;
	this.loader = loader;
    }
    
    public int getEventType() {
	return this.eventType;
    }

    public EjbDescriptor getEjbDescriptor() {
	return this.ejbDescriptor;
    }

    public ClassLoader getClassLoader() {
	return this.loader;
    }

    public String toString() {
	StringBuffer sbuf = new StringBuffer("EjbEvent: ");
	switch (eventType) {
	    case BEFORE_EJB_CONTAINER_LOAD:
		sbuf.append("BEFORE_LOAD -> ");
		break;
	    case AFTER_EJB_CONTAINER_LOAD:
		sbuf.append("AFTER_LOAD -> ");
		break;
	    case BEFORE_EJB_CONTAINER_UNLOAD:
		sbuf.append("BEFORE_UNLOAD -> ");
		break;
	    case AFTER_EJB_CONTAINER_UNLOAD:
		sbuf.append("AFTER_UNLOAD -> ");
		break;
	    default:
		//
	}

	if (ejbDescriptor != null) {
	    sbuf.append(ejbDescriptor.getName());
	}

	return sbuf.toString();
    }

}
