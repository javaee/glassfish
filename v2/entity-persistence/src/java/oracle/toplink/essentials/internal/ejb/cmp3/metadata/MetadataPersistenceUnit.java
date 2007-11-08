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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata;

import oracle.toplink.essentials.internal.ejb.cmp3.xml.XMLConstants;

/**
 * Metadata object to hold persistence unit information.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class MetadataPersistenceUnit  {
    protected String m_access;
    protected String m_schema;
    protected String m_catalog;
    protected String m_conflict;
    protected boolean m_isCascadePersist;
    protected boolean m_isMetadataComplete;
    
    /**
     * INTERNAL:
     */
    public MetadataPersistenceUnit() {
        m_access = "";
        m_schema = "";
        m_catalog = "";
        m_isCascadePersist = false;
        m_isMetadataComplete = false;
    }
    
    /**
     * INTERNAL:
     * If equals returns false, call getConflict() for a finer grain reason why.
     */
    public boolean equals(Object objectToCompare) {
        MetadataPersistenceUnit persistenceUnit = (MetadataPersistenceUnit) objectToCompare;
            
        if (! persistenceUnit.getAccess().equals(getAccess())) {
            m_conflict = XMLConstants.ACCESS;
            return false;
        }
            
        if (! persistenceUnit.getCatalog().equals(getCatalog())) {
            m_conflict = XMLConstants.CATALOG;
            return false;
        }
            
        if (! persistenceUnit.getSchema().equals(getSchema())) {
            m_conflict = XMLConstants.SCHEMA;
            return false;
        }
            
        if (persistenceUnit.isCascadePersist() != isCascadePersist()) {
            m_conflict = XMLConstants.CASCADE_PERSIST;
            return false;
        }
                
        if (persistenceUnit.isMetadataComplete() != isMetadataComplete()) {
            m_conflict = XMLConstants.METADATA_COMPLETE;
            return false;
        }
        
        return true;
    }
    
    /**
     * INTERNAL:
     */
    public String getAccess() {
       return m_access; 
    }
    
    /**
     * INTERNAL:
     */
    public String getCatalog() {
       return m_catalog; 
    }
    
    /**
     * INTERNAL:
     * Calling this method after an equals call that returns false will give
     * you the conflicting metadata.
     */
    public String getConflict() {
       return m_conflict;
    }
    
    /**
     * INTERNAL:
     */
    public String getSchema() {
       return m_schema; 
    }
    
    /**
     * INTERNAL:
     */
    public boolean isCascadePersist() {
        return m_isCascadePersist;
    }
    
    /**
     * INTERNAL:
     */
    public boolean isMetadataComplete() {
        return m_isMetadataComplete;
    }
    
    /**
     * INTERNAL:
     */
    public void setAccess(String access) {
       m_access = access; 
    }
    
    /**
     * INTERNAL:
     */
    public void setCatalog(String catalog) {
       m_catalog = catalog;
    }
    
    /**
     * INTERNAL:
     */
    public void setIsCascadePersist(boolean isCascadePersist) {
        m_isCascadePersist = isCascadePersist;
    }
    
    /**
     * INTERNAL:
     */
    public void setIsMetadataComplete(boolean isMetadataComplete) {
        m_isMetadataComplete = isMetadataComplete;
    }
    
    /**
     * INTERNAL:
     */
    public void setSchema(String schema) {
       m_schema = schema;
    }
}
