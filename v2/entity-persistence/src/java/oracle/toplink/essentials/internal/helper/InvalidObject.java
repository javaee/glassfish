/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.  
 * // Copyright (c) 2004, 2006, Oracle. All rights reserved.
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

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkrogh      10/07/05 - 
    pkrogh      09/29/05 - 
    gyorke      08/09/05 - gyorke_10-essentials-directory-creation_050808
    dmahar      08/04/05 - 
    pkrogh      08/28/04 - codeformat
    smcritch    05/14/04 - smcritch_refactor_session_read031604
    smcritch    04/26/04 - Creation
 */
package oracle.toplink.essentials.internal.helper;


/**
 * <b>Purpose</b>:Indicates an object that should not be returned from
 * query execution.
 * <p>
 * When conforming if checkEarly return finds a matching object by exact primary
 * key, but that object is deleted, want to return null from query execution.
 * <p>
 * However if null is returned from checkEarly return that will indicate that
 * no object was found and to go to the database.  Hence returning null is not
 * enough, something else needed to be returned, indicating not only that
 * checkEarlyReturn had failed but query execution should not proceed.
 * <p>
 * Can be used in other instances where returning null is ambiguous.
 * <p>
 * Implements singleton pattern
 *  @author  Stephen McRitchie
 *  @since   release specific (what release of product did this appear in)
 */
public class InvalidObject {
    public static final InvalidObject instance = new InvalidObject();

    private InvalidObject() {
    }

    /**
     * @return singleton invalid object.
     */
    public static InvalidObject instance() {
        return instance;
    }
}
