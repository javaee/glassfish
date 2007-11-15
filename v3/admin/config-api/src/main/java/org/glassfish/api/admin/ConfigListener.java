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
package org.glassfish.api.admin;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 * A ConfigListener is interested in ConfigEvents which are fired when changes 
 * occur in the configuration. Events can be either a new configuration added, a
 * a configuration removed or just a configuration changed. 
 * 
 * @author Jerome Dochez
 */
public interface ConfigListener {

	/**
	 * A new configuration object was added. 
	 *
	 * @param owner is the configuration object to which the new config was added to. 
	 * @param propertyName is the name of the added configuration object as known to the
	 * owner configuration.
	 * @param added is the new configuration object added to the owner configuration.
     * @throws PropertyVetoException if the listener wish to veto the changes
	 */
    public void configAdded(Object owner, String propertyName, Object added) throws PropertyVetoException;

	/**
	 * A configuration object has change (maybe multiple times)
	 *
	 * @param evt is the array of changes description
     * @throws PropertyVetoException if the listener wish to veto the changes
	 */
    public void changed(PropertyChangeEvent... evt) throws PropertyVetoException;

	/**
	 * A configuration object was removed.
	 *
	 * @param owner is the configuration object owning the removed configuration
	 * @param propertyName is the name of the removed configuration as known to 
	 * the owner configuiration.
	 * @param removed is the removed configuration.
     * @throws PropertyVetoException if the listener wish to veto the changes
	 */
    public void configRemoved(Object owner, String propertyName, Object removed) throws PropertyVetoException;
    
}
