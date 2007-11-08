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
package oracle.toplink.essentials.internal.ejb.cmp3.metadata.queries;

import java.util.ArrayList;
import java.util.List;

/**
 * Object to hold onto a named query metadata's hints.
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public abstract class MetadataQuery  {
    private String m_name;
    private String m_query;
    private Object m_location; // Where it was found, i.e. java class or xml document.
    private List<MetadataQueryHint> m_hints;
    
    /**
     * INTERNAL:
     */
    protected MetadataQuery() {
        m_hints = new ArrayList<MetadataQueryHint>();
    }
    
    /**
     * INTERNAL:
     */
    protected void addHint(MetadataQueryHint hint) {
        m_hints.add(hint);
    }
    
    /**
     * INTERNAL:
     */
    public String getEJBQLString() {
        return m_query;
    }
    
    /**
     * INTERNAL:
     */
    public List<MetadataQueryHint> getHints() {
        return m_hints; 
    }
    
    /**
     * INTERNAL:
     */
    public abstract String getIgnoreLogMessageContext();
    
    /**
     * INTERNAL:
     */
    public Object getLocation() {
        return m_location;
    }
    
    /**
     * INTERNAL:
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * INTERNAL: (Overriden in XMLNamedNativeQuery and XMLNamedQuery)
     */
    public boolean loadedFromAnnotations() {
        return true;
    }
    
    /**
     * INTERNAL: (Overriden in XMLNamedNativeQuery and XMLNamedQuery)
     */
    public boolean loadedFromXML() {
        return false;
    }  
    
    /**
     * INTERNAL:
     */
    protected void setEJBQLString(String query) {
        m_query = query;
    }
    
    /**
     * INTERNAL:
     */
    protected void setLocation(Object location) {
        m_location = location;
    }
    
    /**
     * INTERNAL:
     */
    protected void setName(String name) {
        m_name = name;
    }
}
