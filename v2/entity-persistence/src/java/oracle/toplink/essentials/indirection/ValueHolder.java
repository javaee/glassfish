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
package oracle.toplink.essentials.indirection;

import java.io.Serializable;

/**
 * <p>
 * <b>Purpose</b>: Act as a place holder for a variable that required a value holder interface.
 * This class should be used to initialze an objects attributes that are using indirection is their mappings.
 */
public class ValueHolder implements WeavedAttributeValueHolderInterface, Cloneable, Serializable {
    
    /**
     * Stores the wrapped object.
     */
    protected Object value;

    /**
     * The two variable below are used as part of the implementation of WeavedAttributeValueHolderInterface
     * They are used to track whether a valueholder that has been weaved into a class is coordinated
     * with the underlying property
     */
    // Set internally in TopLink when the state of coordination between a weaved valueholder and the underlying property is known
    private boolean isCoordinatedWithProperty = false; 
    // Used to determine if this ValueHolder was added instantiated as part of the constructor of a weaved class
    private boolean isNewlyWeavedValueHolder = false;
    
    /**
     * PUBLIC:
     * Initialize the holder.
     */
    public ValueHolder() {
        super();
    }

    /**
     * PUBLIC:
     * Initialize the holder with an object.
     */
    public ValueHolder(Object value) {
        this.value = value;
    }

    /**
     * INTERNAL:
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            ;
        }

        return null;
    }

    /**
     * PUBLIC:
     * Return the wrapped object.
     */
    public synchronized Object getValue() {
        return value;
    }

    /**
     * Used as part of the implementation of WeavedAttributeValueHolderInterface
     * Used to track whether a valueholder that has been weaved into a class is coordinated
     * with the underlying property
     */
    public boolean isCoordinatedWithProperty(){
        return isCoordinatedWithProperty;
    }
    
    /**
     * Used as part of the implementation of WeavedAttributeValueHolderInterface
     * Used to determine if this ValueHolder was added instantiated as part of 
     * the constructor of a weaved class
     */
    public boolean isNewlyWeavedValueHolder(){
        return isNewlyWeavedValueHolder;
    }
    
    /**
     * PUBLIC:
     * Return a boolean indicating whether the
     * wrapped object has been set or not.
     */
    public boolean isInstantiated() {
        // Always return true since we consider 
        // null to be a valid wrapped object.
        return true;
    }

    /**
     * Used as part of the implementation of WeavedAttributeValueHolderInterface
     * Used to track whether a valueholder that has been weaved into a class is coordinated
     * with the underlying property
     * 
     * This method will be called internall when the state of Coordination between the
     * weaved valueholder and the underlying value is known
     */
    public void setIsCoordinatedWithProperty(boolean coordinated){
        this.isCoordinatedWithProperty = coordinated;
        // this is not a newly weaved valueholder any more since we have done some coordination work
        isNewlyWeavedValueHolder = false;
    }
    
    /**
     * Used as part of the implementation of WeavedAttributeValueHolderInterface
     * Used to determine if this ValueHolder was added instantiated as part of 
     * the constructor of a weaved class
     * 
     * This method will be called when a ValueHolder is instantiated in a weaved class
     */
    public void setIsNewlyWeavedValueHolder(boolean isNew){
        this.isNewlyWeavedValueHolder = isNew;
    }
    
    /**
     * PUBLIC:
     * Set the wrapped object.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        if (getValue() == null) {
            return "{" + null + "}";
        }
        return "{" + getValue().toString() + "}";
    }
}
