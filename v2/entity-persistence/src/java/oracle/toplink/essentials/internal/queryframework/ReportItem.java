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
package oracle.toplink.essentials.internal.queryframework;

import java.util.List;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.expressions.FunctionExpression;
import oracle.toplink.essentials.internal.expressions.QueryKeyExpression;

/**
 * <b>Purpose</b>: represents an item requested (i.e. field for SELECT)
 *
 * @author Doug Clarke
 * @since 2.0
 */
public class ReportItem implements java.io.Serializable {

    /** Expression (partial) describing the attribute wanted */
    protected Expression attributeExpression;

    /** Name given for item, can be used to retieve value from result. Useful if same field retrieved multipe times */
    protected String name;

    /** Mapping which relates field to attribute, used to convert value and determine reference descriptor */
    protected DatabaseMapping mapping;
    
    /** Desriptor for object result that is not based on an expression */
    protected ClassDescriptor descriptor;
    
    /** Result type for this report item. */
    protected Class resultType;
    
    /** Stores the Join information for this item */
    protected JoinedAttributeManager joinManager;
    
    /** Stores the row index for this item, given multiple results and joins */
    protected int resultIndex;
    
    public ReportItem() {
        super();
    }

    public ReportItem(String name, Expression attributeExpression) {
        this.name = name;
        this.attributeExpression = attributeExpression;
        this.joinManager = new JoinedAttributeManager();
    }
    
    public Expression getAttributeExpression() {
        return attributeExpression;
    }

    public ClassDescriptor getDescriptor(){
        return this.descriptor;
    }
    
    /**
     * INTERNAL:
     * Set the list of expressions that represent elements that are joined because of their
     * mapping for this query.
     */
    public JoinedAttributeManager getJoinedAttributeManager() {
        return this.joinManager;
    }

    public DatabaseMapping getMapping() {
        return mapping;
    }

    public String getName() {
        return name;
    }
    
    public int getResultIndex() {
        return resultIndex;
    }

    public Class getResultType() {
        return resultType;
    }

    /**
     * INTERNAL:
     * Looks up mapping for attribute during preExecute of ReportQuery
     */
    public void initialize(ReportQuery query) throws QueryException {
        if (getMapping() == null) {
            DatabaseMapping mapping = query.getLeafMappingFor(getAttributeExpression(), query.getDescriptor());
            if (mapping == null){
                if (getAttributeExpression() != null){
                    if (getAttributeExpression().isExpressionBuilder()) {
                        Class resultClass = ((ExpressionBuilder)getAttributeExpression()).getQueryClass();
                        if (resultClass == null){
                            resultClass = query.getReferenceClass();
                        }
                        setDescriptor(query.getSession().getDescriptor(resultClass));
                        if (getDescriptor().hasInheritance()){
                            ((ExpressionBuilder)getAttributeExpression()).setShouldUseOuterJoinForMultitableInheritance(true);
                        }
                    }
                }
            }else{
                //Bug4942640  Widen the check to support collection mapping too
                if (mapping.isForeignReferenceMapping()){
                    setDescriptor(mapping.getReferenceDescriptor());
                    if (getDescriptor().hasInheritance()){
                        ((QueryKeyExpression)getAttributeExpression()).setShouldUseOuterJoinForMultitableInheritance(true);
                    }
                } else if (mapping.isAbstractDirectMapping()){
                    setMapping((DatabaseMapping)mapping);
                } else {
                    throw QueryException.invalidExpressionForQueryItem(getAttributeExpression(), query);
                }
            }
            this.joinManager.setDescriptor(this.descriptor);
            this.joinManager.setBaseQuery(query);
            if (getAttributeExpression() != null){
                if (getAttributeExpression().getBuilder().wasQueryClassSetInternally()){
                    //rebuild if class was not set by user this ensures the query has the same base
                    this.attributeExpression = getAttributeExpression().rebuildOn(query.getExpressionBuilder());
                }
                this.joinManager.setBaseExpressionBuilder(this.attributeExpression.getBuilder());
            }else{
                this.joinManager.setBaseExpressionBuilder(query.getExpressionBuilder());
            }
            if (this.descriptor!=null){
                this.joinManager.processJoinedMappings();
                this.joinManager.prepareJoinExpressions(query.getSession());
                this.joinManager.computeJoiningMappingQueries(query.getSession());
            }
        }
    }

    public boolean isContructorItem(){
        return false;
    }

    /**
     * @return true if there is no expression (null)
     */
    public boolean isPlaceHolder() {
        return getAttributeExpression() == null;
    }

    public void setDescriptor(ClassDescriptor descriptor){
        this.descriptor = descriptor;
    }
    
    public void setMapping(DatabaseMapping mapping) {
        this.mapping = mapping;
    }
    
    public void setResultIndex(int resultIndex) {
        this.resultIndex = resultIndex;
        this.joinManager.setParentResultIndex(resultIndex);
    }

    public void setResultType(Class resultType) {
        this.resultType = resultType;
    
        // Set it on the attribute expression as well if it is a function.
        if (getAttributeExpression()!=null && getAttributeExpression().isFunctionExpression()) {
            ((FunctionExpression) getAttributeExpression()).setResultType(resultType);
        }
    }

    public String toString() {
        return "ReportQueryItem(" + getName() + " -> " + getAttributeExpression() + ")";
    }

}
