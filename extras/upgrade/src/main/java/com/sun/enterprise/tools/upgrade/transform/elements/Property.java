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
 * Element.java
 *
 * Created on August 4, 2003, 2:04 PM
 */

package com.sun.enterprise.tools.upgrade.transform.elements;

/**
 *
 * @author  prakash
 */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.sun.enterprise.tools.upgrade.transform.ElementToObjectMapper;
import com.sun.enterprise.tools.upgrade.common.UpgradeConstants;
import java.util.logging.Level;

public class Property extends BaseElement {
	
	/** Creates a new instance of Element */
	public Property() {
	}
	/**
	 * element - Property
	 * parentSource - parent of Property
	 * parentResult - result element that need to be updated.
	 */
	public void transform(Element element, Element parentSource, Element parentResult){
		logger.log(Level.FINE, stringManager.getString("upgrade.transform.transformingMSG", this.getClass().getName(), element.getTagName()));
		// Property has name and value. and possible description element.
		logger.log(Level.FINE, this.getClass().getName() +  ":: canTransferAttributes ", canTransferAttributes(element, parentSource, parentResult));
		if(!canTransferAttributes(element, parentSource, parentResult))
			return;
		NodeList resultProperties = parentResult.getElementsByTagName("property");
		Element resultProperty = null;
		if(resultProperties != null){
			for(int index=0; index < resultProperties.getLength(); index++){
				if(element.getAttribute("name").equals(((Element)resultProperties.item(index)).getAttribute("name"))){
					resultProperty = (Element)resultProperties.item(index);
					resultProperty.getAttributeNode("value").setValue(element.getAttributeNode("value").getValue());
					this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
					break;
				}
			}
		}
		if(resultProperty == null){
			resultProperty = parentResult.getOwnerDocument().createElement("property");
			this.transferAttributes(element, resultProperty, null);
			this.handleSpecialCases(element, resultProperty, parentSource, parentResult);
			parentResult.appendChild(resultProperty);
		}
		super.transform(element, parentSource, resultProperty);
	}
	
	// This method is used to avoid transferring certain property elements.
	private boolean canTransferAttributes(Element element, Element parentSource, Element parentResult){
		if(parentSource.getTagName().equals("jmx-connector")) {
			if(element.getAttribute("name").equals("client-hostname")) {
				return false;
			}
		}
		return true;
	}
	private void handleSpecialCases(Element source, Element target, Element parentSource, Element targetParent){
		if(parentSource.getTagName().equals("jms-resource") && targetParent.getTagName().equals("admin-object-resource")){
			if(source.getAttribute("name").equals("imqDestinationName")){
				target.setAttribute("name", "Name");
			}
		}
	}
	
}
