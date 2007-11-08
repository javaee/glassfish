/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 2005, 2006, Oracle. All rights reserved.
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
package oracle.toplink.essentials.internal.weaving;

// J2SE imports
import java.util.*;

/**
 * INTERNAL:
 * Internal helper class that holds details of a persistent class.
 * Used by {@link TopLinkWeaver}
 * 
 */

public class ClassDetails {

	// the name of this class (obviously!)
	protected String className;
	// superclass' name
	protected String superClassName;
	// superclass' ClassDetails - only populated if superclass
	// is also persistent
	protected ClassDetails superClassDetails;

	protected boolean weaveValueHolders = false;
	// map of this class' persistent attributes
	protected Map attributesMap; // Map<String, AttributeDetails>
                                 // where the key is the Attribute name
    protected Map getterMethodToAttributeDetails;
    
    protected Map setterMethodToAttributeDetails;
    
    protected List lazyOneToOneMappings;
    
    protected boolean isMappedSuperClass = false;
	
	// default constructor
	public ClassDetails() {
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}

	public String getSuperClassName() {
		return superClassName;
	}
	public void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}

	public ClassDetails getSuperClassDetails() {
		return superClassDetails;
	}
	public void setSuperClassDetails(ClassDetails superClassDetails) {
		this.superClassDetails = superClassDetails;
	}

	public boolean weavedValueHolders() {
		return weaveValueHolders;
	}
	public void weaveValueHolders(boolean weaveValueHolders) {
		this.weaveValueHolders = weaveValueHolders;
	}
	
	public Map getAttributesMap() {
		return attributesMap;
	}

    public Map getGetterMethodToAttributeDetails(){
        return getterMethodToAttributeDetails;
    }
    
    public List getLazyOneToOneMappings(){
        return lazyOneToOneMappings;
    }
    
    public Map getSetterMethodToAttributeDetails(){
        return setterMethodToAttributeDetails;
    }
    
	public void setAttributesMap(Map weavedVHAttributes) {
		this.attributesMap = weavedVHAttributes;
	}
    
    public void setGetterMethodToAttributeDetails(Map map){
        getterMethodToAttributeDetails = map;
    }
    
    public void setLazyOneToOneMappings(List lazyOneToOneMappings){
        this.lazyOneToOneMappings = lazyOneToOneMappings;
    }
    
    public boolean isMappedSuperClass(){
        return isMappedSuperClass;
    }
    
    public void setIsMappedSuperClass(boolean isMappedSuperClass){
        this.isMappedSuperClass = isMappedSuperClass;
    }
    
    public void setSetterMethodToAttributeDetails(Map map){
        setterMethodToAttributeDetails = map;
    }
	
    public AttributeDetails getAttributeDetailsFromClassOrSuperClass(String attributeName){
        AttributeDetails attribute = (AttributeDetails)attributesMap.get(attributeName);
        if (attribute == null && superClassDetails != null){
            return superClassDetails.getAttributeDetailsFromClassOrSuperClass(attributeName);
        }
       return attribute; 
    }
    
	public String toString() {
		StringBuffer sb = new StringBuffer(className);
		sb.append(" extends ");
		sb.append(superClassName);
		sb.append(" weaveVH: ");
		if (weavedValueHolders()) {
			sb.append("true");
		}
		else {
			sb.append("false");
		}
		return sb.toString();
	}
}
