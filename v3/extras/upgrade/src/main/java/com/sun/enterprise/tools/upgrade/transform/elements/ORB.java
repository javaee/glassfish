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
 * AuthDB.java
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
import java.util.logging.Level;

public class ORB extends BaseElement {

    /** Creates a new instance of Element */
    public ORB() {
    }
    /**
     * element - orb
     * parentSource - parent iiop-service of element
     * parentResult - parent iiop-service of result
     */
    public void transform(Element element, Element parentSource, Element parentResult){
		logger.log(Level.FINE, stringManager.getString("upgrade.transform.transformingMSG", this.getClass().getName(), element.getTagName()));
        NodeList resultORBs = parentResult.getElementsByTagName("orb");
        // There should be one and only one orb.
        Element resultORB = (Element)resultORBs.item(0);
		if (resultORB == null){
			//-- added for as91.x support
			resultORB = parentResult.getOwnerDocument().createElement(element.getTagName());
            this.transferAttributes(element, resultORB, null);
            this.appendElementToParent(parentResult,resultORB); 
		}else{
			logger.log(Level.FINE, this.getClass().getName() +  ":: resultORB " + resultORB.getTagName());
			// No need to add attrbitues which are missing.
			this.transferAttributes(element, resultORB, null);
		}
        super.transform(element, parentSource, resultORB);
    }

}
