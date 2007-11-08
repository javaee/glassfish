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
package oracle.toplink.essentials.internal.helper;

/**
 * <p>
 * <b>Purpose</b>: Extension to the existing conversion manager to support the
 * EJB 3.0 spec. 
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Allow a null value default to be read into primitives. With the current
 * conversion manager, setting a null into a primitive causes and exception. 
 * This conversion manager was added to avoid that exception and therefore, add 
 * support for schemas that were built before the object model was mapped 
 * (using a primitive). Therefore, TopLink will not change the null column value 
 * in the database through this conversion. The value on the dataabse will only 
 * be changed if the user actually sets a new primitive value.
 * <li> Allows users to define their own set of default null values to be used
 * in the conversion.
 * </ul>
 * 
 * @author Guy Pelletier
 * @since TopLink 10.1.4 RI
 */
public class EJB30ConversionManager extends ConversionManager {
    public EJB30ConversionManager() {
        super();
    }

    /**
     * INTERNAL:
     */
    public Object getDefaultNullValue(Class theClass) {
        Object defaultNullValue = getDefaultNullValues().get(theClass);
        
        if (defaultNullValue == null && theClass.isPrimitive()) {
            return 0;
        } else {
            return defaultNullValue;
        }
    }
}
