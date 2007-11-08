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

package com.sun.enterprise.config.serverbeans.validation;

import java.io.File;
import java.util.logging.Level;

/**
    Class which contains Meta data for all types of attributes which is present in Validation Descriptor
 *  XML File
 *
 *  Sample
 *      <attribute name=<Name> type="address" />
 *      <attribute name=<Name> type="integer" range="low,high" />
 *      <attribute name=<Name> type="string"  max-length="length" />
    
    @author Srinivas Krishnan
    @version 2.0
*/

/* Class for attribute type file */

public class AttrClassName extends AttrType {
    
    public AttrClassName(String name, String type, boolean optional) {
        super(name,type, optional);
    }
    
    public void validate(Object o, ValidationContext valCtx) {
        _logger.log(Level.CONFIG, "Testing attr: "+valCtx.attrName);

	super.validate(o, valCtx); // call to common validator first
        if(o == null)
		return;
        if(o.equals(""))
            valCtx.result.failed(valCtx.smh.getLocalString(getClass().getName() + ".nullClassName",
                                        "Attribute({0}=\"\" : ClassName not Valid", new Object[] {valCtx.attrName}));
   
        // Remove the package, extract the identifier alone                                        
        String className = (String)o;                                        
        int classStart = className.lastIndexOf(".");
        if(classStart != -1)
            className = className.substring(classStart+1);
        if(!isValidClassName(className)){
            valCtx.result.failed(valCtx.smh.getLocalString(getClass().getName() + ".invalidClassName",
                                        "Attribute({0}={1}) : ClassName not Valid", new Object[] {valCtx.attrName, (String)o}));
        }
    }
    
    public static boolean isValidClassName(String className) {
        boolean valid = true;
        for(int i=0;i<className.length();i++) {
            if(i == 0) {
                if(!Character.isJavaIdentifierStart(className.charAt(i)))
                    valid = false;
            }
            if(!Character.isJavaIdentifierPart(className.charAt(i)))
                valid = false;
        }
        return valid;
    }
}
