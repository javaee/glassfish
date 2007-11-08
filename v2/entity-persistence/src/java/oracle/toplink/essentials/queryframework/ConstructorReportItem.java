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

import java.util.Vector;
import java.util.List;
import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.internal.queryframework.ReportItem;


/**
 * <b>Purpose</b>: An item specifying a class constructor method to be used in a ReportQuery's returned results.  
 * Example:
 *  ConstructorReportItem citem = new ConstructorReportItem("Employee");
 *  citem.setResultType(Employee.class);
 *  citem.addAttribute("firstName", employees.get("firstName"));
 *  query.addConstructorReportItem(citem);
 *  
 * when executed will return a collection of ReportQueryResults that contain Employee objects created using
 * the new Employee(firstname) method. 
 *
 * @author Chris Delahunt
 * @since TopLink Essentials
 */
public class ConstructorReportItem extends ReportItem  {

    protected Class[] constructorArgTypes;
    protected List constructorMappings;
    public List reportItems;
    
    public ConstructorReportItem() {
    }
    
  /**
   * Method to add an expression to be used to return the parameter that is then passed into the constructor method.
   * Similar to ReportQuery's addAttribute method, but a name is not needed
   * @param name - string used to look up this result in the ReportQueryResult
   */
    public ConstructorReportItem(String name) {
        super(name,null);
    }
    
  /**
   * Method to add an expression to be used to return the parameter that is then passed into the constructor method.
   * Similar to ReportQuery's addAttribute method, but a name is not needed
   * @param attributeExpression 
   */
    public void addAttribute( Expression attributeExpression) {
        ReportItem item = new ReportItem(getName()+getReportItems().size(), attributeExpression);
        getReportItems().add(item);
    }
    
    public void addAttribute(String attributeName, Expression attributeExpression, List joinedExpressions) {
        ReportItem item = new ReportItem(attributeName, attributeExpression);
        item.getJoinedAttributeManager().setJoinedAttributeExpressions_(joinedExpressions);
        getReportItems().add(item);
    }
    
    public void addItem(ReportItem item) {
        getReportItems().add(item);
    }
    
    public Class[] getConstructorArgTypes(){
        return constructorArgTypes;
    }

    public List getConstructorMappings(){
        return constructorMappings;
    }

    public List getReportItems(){
        if (reportItems==null){
            reportItems=new Vector();
        }
        return reportItems;
    }

    /**
     * INTERNAL:
     * Looks up mapping for attribute during preExecute of ReportQuery
     */
    public void initialize(ReportQuery query) throws QueryException {
        int size= getReportItems().size();
        List mappings = new Vector();
        for (int i=0;i<size;i++){
            ReportItem item = (ReportItem)reportItems.get(i);
            item.initialize(query);
            mappings.add(item.getMapping());
        }
        setConstructorMappings(mappings);
    }
    
    public boolean isContructorItem(){
        return true;
    }

    public void setConstructorArgTypes(Class[] constructorArgTypes){
        this.constructorArgTypes = constructorArgTypes;
    }
    
    public void setConstructorMappings(List constructorMappings){
        this.constructorMappings = constructorMappings;
    }
    
    public void setReportItems(List reportItems){
        this.reportItems = reportItems;
    }
    
    public String toString() {
        String string = "ConstructorReportItem(" + getName() + " -> [";
        //don't use getReportItems to avoid creating collection.  
        if (reportItems!=null){
            int size=reportItems.size();
            for(int i=0;i<size;i++){
                string =string + reportItems.get(i).toString();
            }
        }
        return string +"])";
    }
    
}
