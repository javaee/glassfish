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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.columns;

import org.w3c.dom.Node;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumn;

/**
 * Object to hold onto xml join column metadata in a TopLink database fields.
 * 
 * @author Guy Pelletier
 * @since TopLink 10.1.3/EJB 3.0 Preview
 */
public class XMLJoinColumn extends MetadataJoinColumn {
    /**
     * INTERNAL:
     * Called for association override.
     */
    public XMLJoinColumn(Node node, XMLHelper helper) {
        // Process the primary key field metadata.
        m_pkField.setName(helper.getNodeValue(node, XMLConstants.ATT_REFERENCED_COLUMN_NAME, DEFAULT_REFERENCED_COLUMN_NAME));
        
        // Process the foreign key field metadata.
        m_fkField.setName(helper.getNodeValue(node, XMLConstants.ATT_NAME, DEFAULT_NAME));
        m_fkField.setTableName(helper.getNodeValue(node, XMLConstants.ATT_TABLE, DEFAULT_TABLE));
        m_fkField.setUnique(helper.getNodeValue(node, XMLConstants.ATT_UNIQUE, DEFAULT_UNIQUE));
        m_fkField.setNullable(helper.getNodeValue(node, XMLConstants.ATT_NULLABLE, DEFAULT_NULLABLE));
        m_fkField.setUpdatable(helper.getNodeValue(node, XMLConstants.ATT_UPDATABLE, DEFAULT_UPDATABLE));
        m_fkField.setInsertable(helper.getNodeValue(node, XMLConstants.ATT_INSERTABLE, DEFAULT_INSERTABLE));
        m_fkField.setColumnDefinition(helper.getNodeValue(node, XMLConstants.ATT_COLUMN_DEFINITION, DEFAULT_COLUMN_DEFINITION));
    }
    
    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
        return true;
    }
}
