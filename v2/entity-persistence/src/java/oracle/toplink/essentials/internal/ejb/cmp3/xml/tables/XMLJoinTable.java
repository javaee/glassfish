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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.tables;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.MetadataLogger;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataJoinColumns;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.tables.MetadataJoinTable;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLJoinColumns;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Object to hold onto an XML join table metadata in a TopLink database table.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLJoinTable extends MetadataJoinTable {
    protected XMLHelper m_helper;
    protected NodeList m_joinColumns;
    protected NodeList m_inverseJoinColumns;
    
    /**
     * INTERNAL:
     */
    public XMLJoinTable(Node node, XMLHelper helper, MetadataLogger logger) {
        super(logger);
        
        m_helper = helper;
        
        m_name = helper.getNodeValue(node, XMLConstants.ATT_NAME);
        m_schema = helper.getNodeValue(node, XMLConstants.ATT_SCHEMA);
        m_catalog = helper.getNodeValue(node, XMLConstants.ATT_CATALOG);
        
        m_joinColumns = helper.getNodes(node, XMLConstants.JOIN_COLUMN);
        m_inverseJoinColumns =  helper.getNodes(node, XMLConstants.INVERSE_JOIN_COLUMN);
            
        processName();
        XMLTableHelper.processUniqueConstraints(node, helper, m_databaseTable);
    }

    /**
     * INTERNAL:
     */
    public boolean loadedFromXML() {
        return true;
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     */
    protected MetadataJoinColumns processInverseJoinColumns() {
        return new XMLJoinColumns(m_inverseJoinColumns, m_helper);
    }
    
    /**
     * INTERNAL: (OVERRIDE)
     */
    protected MetadataJoinColumns processJoinColumns() {
        return new XMLJoinColumns(m_joinColumns, m_helper);
    }
}
