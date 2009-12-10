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

package javax.resource.spi.work;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * A standard {@link WorkContext WorkContext} that allows a {@link Work Work}
 * instance to propagate quality-of-service (QoS) hints about the {@link Work
 * Work} to the <code>WorkManager</code>.
 * 
 * @since 1.6
 * @see javax.resource.spi.work.WorkContextProvider
 * @version Java EE Connector Architecture 1.6
 */

public class HintsContext implements WorkContext {

	/**
	 * Determines if a deserialized instance of this class
	 * is compatible with this class.
	 */
	private static final long serialVersionUID = 7956353628297167255L;
	
	public static final String NAME_HINT = "javax.resource.Name";
	public static final String LONGRUNNING_HINT = "javax.resource.LongRunning";

	protected String description = "Hints Context";
	protected String name = "HintsContext";

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set a brief description of the role played by the instance of
	 * HintsContext and any other related debugging information.
	 * 
	 * This could be used by the resource adapter and the WorkManager for
	 * logging and debugging purposes.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Set the associated name of the HintsContext. This could be used by
	 * the resource adapter and the WorkManager for logging and debugging
	 * purposes.
	 */
	public void setName(String name) {
		this.name = name;
	}

	Map<String, Serializable> hints = new HashMap<String, Serializable>();

	/**
	 * Set a Hint and a related value. The hintName must be non-Null. Standard
	 * HintNames are defined in the Connector specification. Use of
	 * "javax.resource." prefixed hintNames are reserved for use by the
	 * Connector specification.
	 * 
	 */
	public void setHint(String hintName, Serializable value) {
		hints.put(hintName, value);
	}

	public Map<String, Serializable> getHints() {
		return hints;
	}

}
