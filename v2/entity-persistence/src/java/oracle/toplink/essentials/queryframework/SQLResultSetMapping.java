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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import oracle.toplink.essentials.internal.localization.ExceptionLocalization;

/**
 * <p><b>Purpose</b>:
 * Concrete class to represent the SQLResultSetMapping structure as defined by
 * the EJB 3.0 Persistence specification.  This class is used by the 
 * ResultSetMappingQuery and is a component of the TopLink Project
 * 
 * @see oracle.toplink.essentials.sessions.Project
 * @author Gordon Yorke
 * @since TopLink Java Essentials
 */

public class SQLResultSetMapping {
    /** Stores the name of this SQLResultSetMapping.  This name is unique within
     * The project.
     */
    protected String name;
    
    /** Stores the list of SQLResult in the order they were
     * added to the Mapping
     */
    protected List results;
    
   
    public SQLResultSetMapping(String name){
        this.name = name;
        if (this.name == null){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_value_in_sqlresultsetmapping"));
        }
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this SQLResultSetMapping to actual class-based
     * settings. This method is used when converting a project that has been built
     * with class names to a project with classes.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        Iterator iterator = getResults().iterator();
        while (iterator.hasNext()){
            ((SQLResult)iterator.next()).convertClassNamesToClasses(classLoader);
        }
    };   

    public String getName(){
        return this.name;
    }
    
    public void addResult(SQLResult result){
        if (result == null){
            return;
        }
        getResults().add(result);
    }
    
    /**
     * Accessor for the internally stored list of ColumnResult.  Calling this
     * method will result in a collection being created to store the ColumnResult
     */
    public List getResults(){
        if (this.results == null){
            this.results = new ArrayList();
        }
        return this.results;
    }

}
