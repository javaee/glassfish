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

import java.util.*;
import oracle.toplink.essentials.expressions.Expression;

/**
 * <p><b>Purpose</b>: A fetch group is a performance enhancement that allows a group of
 * attributes of an object to be loaded on demand, which means that the data for an attribute
 * might not loaded from the underlying data source until an explicit access call for the
 * attribute first occurs. It avoids the wasteful practice of loading up all data of the object?s
 * attributes, in which the user is interested in only partial of them.
 *
 * A great deal of caution and careful system use case analysis should be use when using the fetch
 * group feature, as the extra round-trip would well offset the gain from the deferred loading in
 * many cases.
 *
 * TopLink fetch group support is twofold: the pre-defined fetch groups at the descriptor level; and
 * dynamic (use case) fetch groups at the query level.
 *
 * TopLink fetch group support is only on CMP project.
 *
 * Every query can has at most one fetch group. There is an optional pre-defined default fetch group
 * at the descriptor level. If set, and the query has no fetch group being set, the default fetch group
 * would be used, unless query.setShouldUseDefaultFetchGroup(false) is also called. In the latter case,
 * the full object will be fetched after the query execution.
 *
 * @see oracle.toplink.essentials.queryframework.FetchGroup
 * @see oracle.toplink.essentials.queryframework.FetchGroupTracker
 *
 * @author King Wang
 * @since TopLink 10.1.3.
 */
public class FetchGroup implements java.io.Serializable {
    //fetch group name, default is empty if not set
    private String name = "";

    //all attributes in the group
    private Set attributes = new TreeSet();

    //attibute expression list used for the query preparation
    private List fetchGroupAttributeExpressions;

    /**
     * Constructor.
     */
    public FetchGroup() {
        this("");
    }

    /**
     * Constructor with a group name.
     */
    public FetchGroup(String name) {
        this.name = name;
        this.fetchGroupAttributeExpressions = new ArrayList();
    }

    /**
     * Return all attributes defined in the group
     */
    public Set getAttributes() {
        return attributes;
    }

    /**
     * Add an attribute to the group
     */
    public void addAttribute(String attrName) {
        attributes.add(attrName);
    }

    /**
     * Add a set of attributes to the group
     */
    public void addAttributes(Set newAttributes) {
        attributes.addAll(newAttributes);
    }

    /**
    * Remove an attribute from the group
    */
    public void removeAttribute(String attrName) {
        attributes.remove(attrName);
    }

    /**
     * Return the group name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the group name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * INTERNAL:
     * Return true if this fetch group is a super-set of the passed in fetch group
     */
    public boolean isSupersetOf(FetchGroup anotherGroup) {
        return (anotherGroup != null) && getAttributes().containsAll(anotherGroup.getAttributes());
    }

    /**
     * INTERNAL:
     * Return the attibute expression list.
     */
    public List getFetchGroupAttributeExpressions() {
        return fetchGroupAttributeExpressions;
    }

    /**
     * INTERNAL:
     * Set the attibute expression list.
     */
    public void setFetchGroupAttributeExpressions(List fetchGroupAttributeExpressions) {
        this.fetchGroupAttributeExpressions = fetchGroupAttributeExpressions;
    }

    /**
     * INTERNAL:
     * Return if fetch group attributes.
     */
    public boolean hasFetchGroupAttributeExpressions() {
        return !fetchGroupAttributeExpressions.isEmpty();
    }

    /**
    * INTERNAL:
    * Specify that only a subset of the class' attributes be fetched in this query.
    * <p>This allows for the query to be optimized through selecting less data.
    * <p>Partial objects will be returned from the query, where the unspecified attributes will be left in their default value.
    * The primary key, and optimistic locking version (version or timestamp) if defined, will always be fetched.
    * The partial object is cached and can be modified.
    * An access through getter to an un-fetched attribute will trigger another SELECT to load the whole object.
    * <p>Note: Modifying un-fetched attribute is not supported and exception would thrown.
    */
    public void addFetchGroupAttribute(Expression attributeExpression) {
        getFetchGroupAttributeExpressions().add(attributeExpression);
    }
}
