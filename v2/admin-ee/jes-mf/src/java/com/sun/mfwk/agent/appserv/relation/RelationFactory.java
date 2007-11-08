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
 * Copyright 2005-2006 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.relation;

import java.util.Map;
import org.w3c.dom.Element;
import com.sun.mfwk.CMM_MBean;
import com.sun.mfwk.MfObjectFactory;
import javax.management.ObjectName;
import com.sun.mfwk.relations.Relation;
import com.sun.mfwk.relations.RelationType;
import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.modeler.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Field;

/**
 * Factory class to create relation from declarative xml file.
 */
class RelationFactory {
    
    /**
     * Creates a new instance of RelationFactory
     * 
     * @param  ctx  context for process
     */
    RelationFactory(ModelerContext ctx) {
        _context = ctx;
    }
    
    
    /**
     * Starts the relation processing.
     *
     * @param  relation   xml element describing a relation
     *
     * @throws Exception if a problem with processing
     */
    Relation create(Element relation, RelationMappingService rms) 
            throws Exception {

        String source = rms.getSource(relation);
        String dest = rms.getDestination(relation);
        String type = rms.getType(relation);
                
        CMM_MBean sMbean = getMBean(source);
        CMM_MBean dMbean = getMBean(dest);
        Class typeClass = getTypeName(type);

        Relation r = new Relation(sMbean, dMbean, typeClass, null, 
                        _context.getModuleName());
        return r;
    }

    Relation create(String source, String dest, String type) throws Exception {
        return create(source, dest, type, false);
    }
    
    /**
     * creates relation
     *
     * @throws Exception if a problem with processing
     */
    Relation create(String source, String dest, String type, boolean anyway) 
            throws Exception {
                
        CMM_MBean sMbean = getMBean(source);
        CMM_MBean dMbean = getMBean(dest);
        Class typeClass = getTypeName(type);
        
        LogDomains.getLogger().finest("source mbean = " + sMbean);
        LogDomains.getLogger().finest("dest mbean = " + dMbean);
        LogDomains.getLogger().finest("type = " + typeClass);
        
        if (sMbean != null && dMbean != null) {
            return new Relation(sMbean, dMbean, typeClass, null, 
                _context.getModuleName());
        }
        else if (anyway) {
            if (dMbean != null) {
                return new Relation(source,  dMbean, typeClass, null, 
                    _context.getModuleName());
            }
            else if (sMbean != null) {
                return new Relation(sMbean,  dest, typeClass, null, 
                    _context.getModuleName());
            }
        }
        return null;
    }
    
    
    /**
     * Returns the relation type class.
     *
     * @param  type  relationship type
     * @return  relation type
     * 
     * @throws NoSuchFieldException  if type is not found
     * @throws IllegalAccessException if illegal acces
     */
    Class getTypeName(String type) 
            throws NoSuchFieldException, IllegalAccessException {

        Class typeName = null;

        Class rtClass = RelationType.class;
        Field f = rtClass.getField(type);
        Object obj = f.get(null);
        if (obj instanceof RelationType) {
            RelationType rt = (RelationType) obj;
            typeName = rt.getRelationTypeName();
        }
        
        return typeName;
    }

    /**
     * Returns mbean for the given object id. This method tokenizes the 
     * object name if it contains a token of type ${key}.
     *
     * @param  oid  object id
     *
     * @return mbean for the given oid or null
     * @throws Exception  if an erro getting mbean
     */
    CMM_MBean getMBean(String oid) throws Exception {

        CMM_MBean mbean = null;

        MfObjectFactory factory = 
            MfObjectFactory.getObjectFactory(_context.getModuleName());

        // replaces tokens from object name
        String tokenizedOID=ObjectNameHelper.tokenize(oid,_context.getTokens());

        if (factory.isObjectCreated(tokenizedOID)) {
            mbean = factory.getObject(tokenizedOID);
        }
        
        return mbean;
    }

    // ---- VARIABLES - PRIVATE ----------------------
    private ModelerContext _context = null;
}
