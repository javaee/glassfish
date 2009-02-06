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
 * Server.java
 *
 * Created on August 4, 2003, 3:27 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;

/**
 *
 * @author  prakash
 */
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import java.util.logging.Level;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;

public class Server extends BaseElement {
	
	/** Creates a new instance of Server */
	public Server() {
	}
	
	/**
	 * element - server
	 * parentSource - server
	 * parentResult - domain for as7x.  server for as80
	 */
	public void transform(Element element, Element parentSource, Element parentResult) {
		// Attributes of server element: name, locale, log-root, application-root, session-store
		// application-root and log-root will not be transferred, as they are different for 8.0 appserver.
		// May be application-root need to be saved to migrate applications from the source directory.
		// session-store attribute should be stored in ejb-container element
		// Fetch data from server.xml and update it to domain.xml
		
		NodeList servers = parentResult.getElementsByTagName("server");
		Element server = null;
		for(int lh =0; lh < servers.getLength(); lh++){
			String resultElementID = ((Element)servers.item(lh)).getAttribute("name");
			if((element.getAttribute("name")).equals(resultElementID)){
				server = (Element)servers.item(lh);
				this.transferAttributes(element, server,null);
				break;
			}
		}
		if(server == null){
			// Add element - http-listener to result http-service.
			server = parentResult.getOwnerDocument().createElement("server");
			this.transferAttributes(element, server,null);
			this.appendElementToParent(parentResult,server);
		}
		super.transform(element, parentSource, server);
	}
	
}
