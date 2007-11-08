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

/**
 * INTERNAL:
 * This interface defines funtionality required by ValueHolders for OneToOneMappings that 
 * have LAZY access weaved into them and use Property (method) based access
 * 
 * The weaving feature adds a paralell valueholder to the class it weaves and uses that valueholder
 * to control the lazy loading.  The methods on this interface provide information about how that weaved
 * valueholder is related to the underlying value.
 * @author tware
 *
 */
public interface WeavedAttributeValueHolderInterface extends ValueHolderInterface {

    /**
     * When a valueholder is triggered, the weaved code will ensure its value is 
     * coordinated with the underlying property.  This method allows TopLink to determine
     * if that has happened.
     * @return
     */
    public boolean isCoordinatedWithProperty();
    
    /**
     * TopLink will call this method when the triggering of a weaved valueholder causes it's
     * value to be coordinated with the underlying property
     */
    public void setIsCoordinatedWithProperty(boolean coordinated);
    
    /**
     * This method returns whether this valueholder has been newly instantiated by weaved code.
     * @return
     */
    public boolean isNewlyWeavedValueHolder();
    
    /**
     * TopLink weaving calls this method on any valueholder it weaves into a class to indicate
     * that it is new and it's value should not be considered.  The method is also called when coordination
     * with the underlying value occurs to indicate the value can now be trusted.
     * @param isNew
     */
    public void setIsNewlyWeavedValueHolder(boolean isNew);
}
