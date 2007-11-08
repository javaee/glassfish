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

/* Class for attribute type Integer */

public class AttrInt extends AttrType {
    
    public static final int IGNORE_LOW = -2147483648;
    public static final int IGNORE_HIGH = 2147483638;
    int highRange;
    int lowRange;
    
    public AttrInt(String name, String type, boolean optional) {
        super(name,type, optional);
        this.highRange = IGNORE_HIGH;
        this.lowRange = IGNORE_LOW;
    }
    
    public int getHighRange() {
        return highRange;
    }
    
    public int getLowRange() {
        return lowRange;
    }
    
    public void setHighRange(int high) {
        highRange = high;
    }
    
    public void setLowRange(int low) {
        lowRange = low;
    }
    
    public void validate(Object value, ValidationContext valCtx) {
        super.validate(value, valCtx); // call to common validator first
        int tmp=0;
        boolean success=true;
        if(value == null || value.equals(""))
            return;
        if(valCtx.isDELETE())
            return;
        try {
             tmp = Integer.parseInt(value.toString());
        } catch(NumberFormatException n) {
            reportAttributeError(valCtx, "invalidInteger",
                "Attribute({0}={1}) : {2} Invalid integer", 
                new Object[] {valCtx.attrName, value, value});    
            success=false;
        }
        if(success) {
            if( (lowRange != IGNORE_LOW && tmp < lowRange) || (highRange != IGNORE_HIGH && tmp > highRange) ) {
                if(lowRange == 0 && highRange == IGNORE_HIGH) {
                    reportAttributeError(valCtx, "invalidIntegerNegative",
                    "Attribute({0}={1}) : {2} Invalid Value, Cannot be a negative number", 
                    new Object[] {valCtx.attrName, String.valueOf(tmp), String.valueOf(tmp)});
                }
                else
                {
                    reportAttributeError(valCtx, "invalidIntegerRange",
                        "Attribute({0}={1}) : {2} Invalid Value, Valid Range {3},{4}", 
                        new Object[] {valCtx.attrName, String.valueOf(tmp), String.valueOf(tmp), String.valueOf(lowRange), String.valueOf(highRange)});
                }
            }
            String compValue = getValueForAttribute((String)getRuleValue("le-than"), valCtx);
            if(compValue!=null && !compValue.trim().equals("0"))
            {
               if(compareIntWithStr(tmp, compValue)>0)
                   reportAttributeError(valCtx, "not-le-than",
                           "Value ({0}) should be less or equal than to value of attribute {1} ({2})",
                           new Object[]{value, getRuleValue("le-than"), compValue});
            }
            compValue = getValueForAttribute((String)getRuleValue("ge-than"), valCtx);
            if(compValue!=null && !compValue.trim().equals("0"))
            {
               if(compareIntWithStr(tmp, compValue)<0)
                   reportAttributeError(valCtx, "not-ge-then",
                           "Value ({0}) should be more or equal to value of attribute {1} ({2})",
                           new Object[]{value, getRuleValue("ge-than"), compValue});
            }
                
            compValue = getValueForAttribute((String)getRuleValue("gt-than"), valCtx);
            if(compValue!=null  && !compValue.trim().equals("0"))
            {
               if(compareIntWithStr(tmp, compValue)<=0)
                   reportAttributeError(valCtx, "not-gt-then",
                           "Value ({0}) should be more then value of attribute {1} ({2})",
                           new Object[]{value, getRuleValue("gt-than"), compValue});
            }
            compValue = getValueForAttribute((String)getRuleValue("ls-than"), valCtx);
            if(compValue!=null  && !compValue.trim().equals("0"))
            {
               if(compareIntWithStr(tmp, compValue)>=0)
                   reportAttributeError(valCtx, "not-ls-then",
                           "Value ({0}) should be less than value of attribute {1} ({2})",
                           new Object[]{value, getRuleValue("ls-than"), compValue});
            }
        }
    }
    
    int compareIntWithStr(int iVal, String strVal)
    {
        return (iVal-Integer.parseInt(strVal));
    }
}
