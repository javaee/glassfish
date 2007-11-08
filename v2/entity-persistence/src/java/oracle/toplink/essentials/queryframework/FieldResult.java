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
package oracle.toplink.essentials.queryframework;


import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.sessions.DatabaseRecord;

/**
 * <p><b>Purpose</b>:
 * Concrete class to represent the FieldResult structure as defined by
 * the EJB 3.0 Persistence specification.  This class is a subcompent of the 
 * EntityResult.
 * 
 * @see EntityResult
 * @author Gordon Yorke
 * @since TopLink Java Essentials
 */

public class FieldResult {
    /** Stores the name of the bean attribute  */
    protected String attributeName;
    /** Stores passed in field name split on the '.' character */
    protected String[] multipleFieldIdentifiers;
    /** FieldResult now can contain multiple FieldResults in a collection if an attribute has multiple fields */
    java.util.Vector fieldResults;
    
    /** Stores the Columns name from the result set that contains the attribute value */
    protected String columnName;
    
    public FieldResult(String attributeName, String column){
        this.columnName = column;
        if (attributeName == null || this.columnName == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_values_for_field_result"));
        }
        multipleFieldIdentifiers = attributeName.split("\\.",0);
        this.attributeName = multipleFieldIdentifiers[0];
    }
    
    public String getAttributeName(){
        return this.attributeName;
    }
    
    public String getColumnName(){
        return this.columnName;
    }
    
    /**
     * INTERNAL:
     * This method is a convience method for extracting values from Results
     */
    public Object getValueFromRecord(DatabaseRecord record){
        return record.get(this.columnName);
    }
    
    /**
     * INTERNAL:
     */
    public java.util.Vector getFieldResults(){
        return fieldResults;
    }
    
    /**
     * INTERNAL:
     */
    public String[] getMultipleFieldIdentifiers(){
        return multipleFieldIdentifiers;
    }
    
    /**
     * INTERNAL:
     * This method is used to support mapping multiple fields, fields are 
     * concatenated/added to one fieldResult.
     */
    public void add(FieldResult newFieldResult){
      if( fieldResults ==null){
          fieldResults = new java.util.Vector();
          fieldResults.add(this);
      }
      fieldResults.add(newFieldResult);
    }
    
}
