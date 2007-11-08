/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.queries;

import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataFieldResult;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries.MetadataEntityResult;

/**
 * Object to hold onto an xml entity result metadata.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLEntityResult extends MetadataEntityResult {
    protected Node m_node;
    protected XMLHelper m_helper;

    /**
     * INTERNAL:
     */
    public XMLEntityResult(Node node, XMLHelper helper) {
        m_node = node;
        m_helper = helper;
    }
    
    /**
     * INTERNAL:
     */
    public String getDiscriminatorColumn() {
        return m_helper.getNodeValue(m_node, XMLConstants.ATT_DISCRIMINATOR_COLUMN, "");
    }
    
    /**
     * INTERNAL:
     * Note this attribute is required but we send in the default void.class
     * object to ensure we go through the correct class loading.
     */
    public Class getEntityClass() {
        return m_helper.getNodeValue(m_node, XMLConstants.ATT_ENTITY_CLASS, void.class);
    }
    
    /**
     * INTERNAL:
     */
    public List<MetadataFieldResult> getFieldResults() {
        if (m_fieldResults == null) {
            m_fieldResults = new ArrayList<MetadataFieldResult>();
            NodeList fieldResultNodes = m_helper.getNodes(m_node, XMLConstants.FIELD_RESULT);
            
            if (fieldResultNodes != null) {
                for (int i = 0; i < fieldResultNodes.getLength(); i++) {
                    m_fieldResults.add(new XMLFieldResult(fieldResultNodes.item(i), m_helper));
                }
            }
        }
        
        return m_fieldResults;
    }
}
