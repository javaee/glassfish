/*
* The contents of this file are subject to the terms 
* of the Common Development and Distribution License 
* (the License).  You may not use this file except in
* compliance with the License.
* 
* You can obtain a copy of the license at 
* https://glassfish.dev.java.net/public/CDDLv1.0.html or
* glassfish/bootstrap/legal/CDDLv1.0.txt.
* See the License for the specific language governing 
* permissions and limitations under the License.
* 
* When distributing Covered Code, include this CDDL 
* Header Notice in each file and include the License file 
* at glassfish/bootstrap/legal/CDDLv1.0.txt.  
* If applicable, add the following below the CDDL Header, 
* with the fields enclosed by brackets [] replaced by
* you own identifying information: 
* "Portions Copyrighted [year] [name of copyright owner]"
* 
* Copyright 2007 Sun Microsystems, Inc. All rights reserved.
*/

package javax.xml.registry;

/**
 * Signals that a JAXR exception has occurred. It contains no members other than the standard reason String.
 *
 * @see JAXRResponse
 * @author     Nicholas Kassem
 * @author     Mark Hapner
 * @author     Rajiv Mordani
 * @author     Farrukh Najmi
 */
public class JAXRException extends Exception implements JAXRResponse  {
    
    /**
     * The Throwable that caused this JAXRException to occur. 
     * This is used when a JAXRException has a nested Throwable.
     */
    protected Throwable cause;
    
    /**
     * Constructs a <code>JAXRException</code> object with no
     * reason or embedded Throwable.
     */
    public JAXRException() {
        super();
        this.cause = null;
    }
    
    /**
     * Constructs a <code>JAXRException</code> object with the given
     * <code>String</code> as the reason for the exception being thrown.
     *
     * @param reason a description of what caused the exception
     */
    public JAXRException(String reason) {
        super(reason);
        this.cause = null;
    }
    
    /**
     * Constructs a <code>JAXRException</code> object with the given
     * <code>String</code> as the reason for the exception being thrown
     * and the given <code>Throwable</code> object as an embedded
     * Throwable.
     *
     * @param reason a description of what caused the exception
     * @param cause a <code>Throwable</code> object that is to
     *        be embedded in this <code>JAXRException</code> object
     */
    public JAXRException(String reason, Throwable cause) {
        super(reason);
        initCause(cause);
    }
    
    /**
     * Constructs a <code>JAXRException</code> object initialized
     * with the given <code>Throwable</code> object.
     *
     * @param cause the Throwable that caused this Exception
     */
    public JAXRException(Throwable cause) {
        super(cause.toString());
        initCause(cause);
    }
    
    /**
     * Returns the detail message for this <code>JAXRException</code>
     * object.
     * <P>
     * If there is an embedded Throwable, and if the
     * <code>JAXRException</code> object has no detail message of its
     * own, this method will return the detail message from the embedded
     * Throwable.
     *
     * @return the error or warning message for this
     *         <code>JAXRException</code> or, if it has none, the
     *         message of the embedded Throwable, if there is one
     */
    public String getMessage() {
        String message = super.getMessage();
        if (message == null && cause != null) {
            return cause.getMessage();
        } else {
            return message;
        }
    }
    
    /**
     * Returns the Throwable embedded in this <code>JAXRException</code>
     * if there is one. Otherwise, this method returns <code>null</code>.
     *
     * @return the embedded Throwable or <code>null</code> if there is
     *         none
     */
    
    public Throwable getCause() {
        return cause;
    }
    
    /**
     * Initializes the <i>cause</i> of this throwable to the specified value.
     * (The cause is the throwable that caused this throwable to get thrown.)
     *
     * <p>This method can be called at most once.  It is generally called from
     * within the constructor, or immediately after creating the
     * throwable.  If this throwable was created
     * with {@link #JAXRException(Throwable)} or
     * {@link #JAXRException(String,Throwable)}, this method cannot be
     * called even once.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @return  a reference to this <code>Throwable</code> instance.
     * @throws IllegalArgumentException if <code>cause</code> is this
     *         throwable.  (A throwable cannot
     *         be its own cause.)
     * @throws IllegalStateException if this throwable was
     *         created with {@link #JAXRException(Throwable)} or
     *         {@link #JAXRException(String,Throwable)}, or this
     * method has already been called on this throwable.
     */
    public synchronized Throwable initCause(Throwable cause) {
        if(this.cause != null) {
            throw new IllegalStateException("Can't override cause");
        }
        if(cause == this) {
            throw new IllegalArgumentException("Self-causation not permitted");
        }
        this.cause = cause;
        
        return this;
    }
    
    public String getRequestId(){
        // Write your code here
        return null;
    }
    
    public int getStatus(){
        // Write your code here
        return 0;
    }
    
    /**
     * Returns true if a response is available, false otherwise.
     * This is a polling method and must not block.
     */
    public boolean isAvailable() throws JAXRException {
        return true;
    }
    
}
