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

import java.util.Vector;

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

/* Class for attribute type String */
 
public class AttrString extends AttrType {
    
    int maxLength;
    Vector ee;
    String expr;
    
    public AttrString(String name, String type, boolean optional) {
        super(name,type, optional);
        this.maxLength = 0;
        ee = null;
        expr = null;
    }
    
    public void setRegExpression(String str) {
        expr = str;
    }
    
    public void setEnumstring(Vector vec) {
        ee = vec;
    }
    
    public int getMaxLength() {
        return maxLength;
    }
    
    public void setMaxLength(int max) {
        maxLength = max;
    }
    
    public void validate(Object o, ValidationContext valCtx) {
        super.validate(o, valCtx); // call to common validator first
        if(o == null) {
            return; 
        }
        
        final String str = (String)o;
        if(0 != maxLength && str.length() > maxLength) {
            reportAttributeError(valCtx, "invalidStringLength",
                "Attribute[{0}={1}] : String {2} length is greather than maximum length {3}",
                new Object[] {valCtx.attrName, str, str, String.valueOf(maxLength)});
        }
        if(null != ee && !ee.contains(str) ){
            reportAttributeError(valCtx, "strInvalidEnum",
                "Attribute({0}={1}) : Invalid String - Required {2}",
                new Object[] {valCtx.attrName, str, ee.toString()});
        }
        
        if(null != expr && !str.matches(expr)) 
        {
            String printOwnerName = GenericValidator.getConfigElementPrintName(
                valCtx, getFutureXPathForValidatingAttribute(valCtx), false, false);
            String generic_descr = valCtx.smh.getLocalString(
                "default_pattern_description",
                "Please refer to admin documentation." );
            String descr = valCtx.smh.getLocalString(
                "pattern_description_for_" + (expr.replaceAll("[\\\\=:]", "`")),
                generic_descr );
            
            reportAttributeError(valCtx, "regexpNotMatch",
                "Value \"{1}\" is not valid for attribute \"{0}\" of {2}. {3}", 
                new Object[] {valCtx.attrName, str, printOwnerName, descr});
        }
    }
}

