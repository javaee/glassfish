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


/**
 * <p><b>Purpose</b>:
 * Used to provide the user with a means of contoling the behaviour of in memory queries
 * that access un-instantiated indirection in the query..
 *
 * <p><b>Description</b>:
 *      This class contains a state variable that is used to determine what a query should do
 * when accesing un-instantiated indirection.  Use use this policy access the policy of a query
 * set the policy behaviour using the methods below.  All read queries have the policy set to throw exception by default
 *
 * @author Gordon Yorke
 * @since TopLink/Java 3.6.3
 */
public class InMemoryQueryIndirectionPolicy implements java.io.Serializable {
    public static final int SHOULD_THROW_INDIRECTION_EXCEPTION = 0;
    public static final int SHOULD_TRIGGER_INDIRECTION = 1;
    public static final int SHOULD_IGNORE_EXCEPTION_RETURN_CONFORMED = 2;
    public static final int SHOULD_IGNORE_EXCEPTION_RETURN_NOT_CONFORMED = 3;
    protected int policy;

    public InMemoryQueryIndirectionPolicy() {
        this.policy = SHOULD_THROW_INDIRECTION_EXCEPTION;
    }

    public InMemoryQueryIndirectionPolicy(int policyValue) {
        this.policy = policyValue;
    }

    public boolean shouldTriggerIndirection() {
        return this.policy == SHOULD_TRIGGER_INDIRECTION;
    }

    public boolean shouldThrowIndirectionException() {
        return this.policy == SHOULD_THROW_INDIRECTION_EXCEPTION;
    }

    public boolean shouldIgnoreIndirectionExceptionReturnConformed() {
        return this.policy == SHOULD_IGNORE_EXCEPTION_RETURN_CONFORMED;
    }

    public boolean shouldIgnoreIndirectionExceptionReturnNotConformed() {
        return this.policy == SHOULD_IGNORE_EXCEPTION_RETURN_NOT_CONFORMED;
    }

    public void ignoreIndirectionExceptionReturnNotConformed() {
        this.policy = SHOULD_IGNORE_EXCEPTION_RETURN_NOT_CONFORMED;
    }

    public void ignoreIndirectionExceptionReturnConformed() {
        this.policy = SHOULD_IGNORE_EXCEPTION_RETURN_CONFORMED;
    }

    public void triggerIndirection() {
        this.policy = SHOULD_TRIGGER_INDIRECTION;
    }

    public void throwIndirectionException() {
        this.policy = SHOULD_THROW_INDIRECTION_EXCEPTION;
    }

    //feature 2297
    public int getPolicy() {
        return this.policy;
    }

    public void setPolicy(int policy) {
        this.policy = policy;
    }
}
