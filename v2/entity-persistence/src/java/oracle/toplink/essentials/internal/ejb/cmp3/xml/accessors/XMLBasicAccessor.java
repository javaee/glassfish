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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.BasicAccessor;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.accessors.objects.MetadataAccessibleObject;
import oracle.toplink.essentials.internal.ejb.cmp3.metadata.columns.MetadataColumn;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.accessors.XMLClassAccessor;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.columns.XMLColumn;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.sequencing.XMLGeneratedValue;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.sequencing.XMLTableGenerator;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.sequencing.XMLSequenceGenerator;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLHelper;
import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;

import oracle.toplink.essentials.internal.helper.DatabaseField;

import org.w3c.dom.Node;

/**
 * An XML extended basic accessor.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLBasicAccessor extends BasicAccessor implements XMLAccessor {
    private Node m_node;
    private XMLHelper m_helper;
    
    /**
     * INTERNAL:
     */
    public XMLBasicAccessor(MetadataAccessibleObject accessibleObject, Node node, XMLClassAccessor classAccessor) {
        super(accessibleObject, classAccessor);
        m_node = node;
        m_helper = classAccessor.getHelper();
    }

    /**
     * INTERNAL:
     */
    public String getCatalog() {
        return m_descriptor.getCatalog();
    }
    
    /**
     * INTERNAL: (Override from BasicAccessor)
     * Build a metadata column. If one isn't found in XML then look for an
     * annotation.
     */
    protected MetadataColumn getColumn(String loggingCtx) {
        Node node = m_helper.getNode(m_node, XMLConstants.COLUMN);
        
        if (node != null) {
            return new XMLColumn(node, m_helper, this);
        } else {
            return super.getColumn(loggingCtx);
        }
    }
    
    /**
     * INTERNAL:
     */
    public String getDocumentName() {
        return m_helper.getDocumentName();
    }
    
    /**
     * INTERNAL: (Override from DirectAccessor)
     */
    public String getEnumeratedType() {
        if (hasEnumerated()) {
            return m_helper.getNodeTextValue(m_node, XMLConstants.ENUMERATED);
        } else {
            return super.getEnumeratedType();
        }
    }
    
    /**
     * INTERNAL: (Override from BasicAccessor)
     */
    public String getFetchType() {
        return m_helper.getFetchTypeDefaultEAGER(m_node);
    }
    
    /**
     * INTERNAL:
     */
    public XMLHelper getHelper() {
        return m_helper;
    }
    
    /**
     * INTERNAL:
     */
    public String getSchema() {
        return m_descriptor.getSchema();
    }
    
    /**
     * INTERNAL: (Override from DirectAccessor)
     * 
     * Return the temporal type for this accessor. Assumes there is a temporal
     * node.
     */
    public String getTemporalType() {
        if (hasTemporal()) {
            return m_helper.getNodeTextValue(m_node, XMLConstants.TEMPORAL);
        } else {
            return super.getTemporalType();
        }
    }
    
    /**
     * INTERNAL: (Override from DirectAccessor)
     * 
	 * Method to check if m_node has an enumerated sub-element.
     */
	public boolean hasEnumerated() {
        Node node = m_helper.getNode(m_node, XMLConstants.ENUMERATED);
        
        if (node == null) {
            return super.hasEnumerated();
        } else {
            return true;
        }
    }
    
    /**
     * INTERNAL: (Override from DirectAccessor)
     * 
	 * Method to check if m_node has a temporal sub-element.
     */
	public boolean hasTemporal() {
        Node node = m_helper.getNode(m_node, XMLConstants.TEMPORAL);
        
        if (node == null) {
            return super.hasTemporal();
        } else {
            return true;
        }
    }
    
    /**
     * INTERNAL: (Override from BasicAccessor)
     * 
	 * Method to check if m_node represents a primary key.
	 */
	public boolean isId() {
        if (m_node.getLocalName().equals(XMLConstants.ID)) {
            return true;
        } else {
            return super.isId();   
        }
    }
    
    /**
     * INTERNAL: (Override from BasicAccessor)
     */
	public boolean isOptional() {
        return m_helper.isOptional(m_node);
    }
    
    /**
     * INTERNAL: (Override from DirectAccessor)
     * 
     * Return true if this accessor represents an BLOB/CLOB mapping, i.e. has a 
     * lob sub-element.
     */
	public boolean hasLob() {
        Node node = m_helper.getNode(m_node, XMLConstants.LOB);
        
        if (node == null) {
            return super.hasLob();
        } else {
            return true;
        }
    }
    
    /**
     * INTERNAL: (Override from BasicAccessor)
     * 
     * Return true if this accessor represents an optimistic locking field.
     */
    public boolean isVersion() {
        if (m_node.getLocalName().equals(XMLConstants.VERSION)) {
            return true;
        } else {
            return super.isVersion();   
        }
    }
    
    /**
     * INTERNAL: (Override from BasicAccessor)
     */
    protected void processGeneratedValue(DatabaseField field) {
        Node node = m_helper.getNode(m_node, XMLConstants.GENERATED_VALUE);

        if (node == null) {
            super.processGeneratedValue(field);
        } else {
            // Ask the common processor to process what we found.
            processGeneratedValue(new XMLGeneratedValue(node, m_helper), field);
        }
    }
    
    /**
     * INTERNAL: (Override from NonRelationshipAccessor)
     * 
	 * Process this accessor's sequence-generator node into a common metadata 
     * sequence generator.
     */
    protected void processSequenceGenerator() {
        Node node = m_helper.getNode(m_node, XMLConstants.SEQUENCE_GENERATOR);
        
        if (node != null) {
            // Process the xml defined sequence generators first.
            processSequenceGenerator(new XMLSequenceGenerator(node, m_helper));
        }
        
        // Process the annotation defined sequence generators second.
        super.processSequenceGenerator();
    }
    
    /**
     * INTERNAL: (Override from NonRelationshipAccessor)
     * 
	 * Process this accessor's table-generator node into a common metadata table 
     * generator.
     */
    protected void processTableGenerator() {
        Node node = m_helper.getNode(m_node, XMLConstants.TABLE_GENERATOR);
        
        if (node != null) {
            // Process the xml defined table generators first.
            processTableGenerator(new XMLTableGenerator(node, this));
        }
        
        // Process the annotation defined sequence generators second.
        super.processTableGenerator();
    }
}
