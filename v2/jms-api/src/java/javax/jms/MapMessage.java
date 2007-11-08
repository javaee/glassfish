/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


package javax.jms;

import java.util.Enumeration;

/** A <CODE>MapMessage</CODE> object is used to send a set of name-value pairs.
  * The names are <CODE>String</CODE> objects, and the values are primitive 
  * data types in the Java programming language. The names must have a value that
  * is not null, and not an empty string. The entries can be accessed 
  * sequentially or randomly by name. The order of the entries is undefined. 
  * <CODE>MapMessage</CODE> inherits from the <CODE>Message</CODE> interface
  * and adds a message body that contains a Map.
  *
  * <P>The primitive types can be read or written explicitly using methods
  * for each type. They may also be read or written generically as objects.
  * For instance, a call to <CODE>MapMessage.setInt("foo", 6)</CODE> is 
  * equivalent to <CODE>MapMessage.setObject("foo", new Integer(6))</CODE>.
  * Both forms are provided, because the explicit form is convenient for
  * static programming, and the object form is needed when types are not known
  * at compile time.
  *
  * <P>When a client receives a <CODE>MapMessage</CODE>, it is in read-only 
  * mode. If a client attempts to write to the message at this point, a 
  * <CODE>MessageNotWriteableException</CODE> is thrown. If 
  * <CODE>clearBody</CODE> is called, the message can now be both read from and 
  * written to.
  *
  * <P><CODE>MapMessage</CODE> objects support the following conversion table. 
  * The marked cases must be supported. The unmarked cases must throw a 
  * <CODE>JMSException</CODE>. The <CODE>String</CODE>-to-primitive conversions 
  * may throw a runtime exception if the primitive's <CODE>valueOf()</CODE> 
  * method does not accept it as a valid <CODE>String</CODE> representation of 
  * the primitive.
  *
  * <P>A value written as the row type can be read as the column type.
  *
  * <PRE>
  * |        | boolean byte short char int long float double String byte[]
  * |----------------------------------------------------------------------
  * |boolean |    X                                            X
  * |byte    |          X     X         X   X                  X
  * |short   |                X         X   X                  X
  * |char    |                     X                           X
  * |int     |                          X   X                  X
  * |long    |                              X                  X
  * |float   |                                    X     X      X
  * |double  |                                          X      X
  * |String  |    X     X     X         X   X     X     X      X
  * |byte[]  |                                                        X
  * |----------------------------------------------------------------------
  * </PRE>
  *
  * <P>Attempting to read a null value as a primitive type must be treated
  * as calling the primitive's corresponding <code>valueOf(String)</code> 
  * conversion method with a null value. Since <code>char</code> does not 
  * support a <code>String</code> conversion, attempting to read a null value 
  * as a <code>char</code> must throw a <code>NullPointerException</code>.
  *
  * @version     1.1 February 2, 002
  * @author      Mark Hapner
  * @author      Rich Burridge
  *
  * @see         javax.jms.Session#createMapMessage()
  * @see         javax.jms.BytesMessage
  * @see         javax.jms.Message
  * @see         javax.jms.ObjectMessage
  * @see         javax.jms.StreamMessage
  * @see         javax.jms.TextMessage
  */
 
public interface MapMessage extends Message { 


    /** Returns the <CODE>boolean</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>boolean</CODE>
      *
      * @return the <CODE>boolean</CODE> value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.      
      */

    boolean 
    getBoolean(String name) throws JMSException;


    /** Returns the <CODE>byte</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>byte</CODE>
      *
      * @return the <CODE>byte</CODE> value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.
      */ 

    byte 
    getByte(String name) throws JMSException;


    /** Returns the <CODE>short</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>short</CODE>
      *
      * @return the <CODE>short</CODE> value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.      
      */ 

    short 
    getShort(String name) throws JMSException;


    /** Returns the Unicode character value with the specified name.
      *
      * @param name the name of the Unicode character
      *
      * @return the Unicode character value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.     
      */ 

    char 
    getChar(String name) throws JMSException;


    /** Returns the <CODE>int</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>int</CODE>
      *
      * @return the <CODE>int</CODE> value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.      
      */ 

    int 
    getInt(String name) throws JMSException;


    /** Returns the <CODE>long</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>long</CODE>
      *
      * @return the <CODE>long</CODE> value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.      
      */ 

    long 
    getLong(String name) throws JMSException;


    /** Returns the <CODE>float</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>float</CODE>
      *
      * @return the <CODE>float</CODE> value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.     
      */ 

    float 
    getFloat(String name) throws JMSException;


    /** Returns the <CODE>double</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>double</CODE>
      *
      * @return the <CODE>double</CODE> value with the specified name
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.      
      */ 

    double 
    getDouble(String name) throws JMSException;


    /** Returns the <CODE>String</CODE> value with the specified name.
      *
      * @param name the name of the <CODE>String</CODE>
      *
      * @return the <CODE>String</CODE> value with the specified name; if there 
      * is no item by this name, a null value is returned
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.      
      */ 

    String 
    getString(String name) throws JMSException;


    /** Returns the byte array value with the specified name.
      *
      * @param name the name of the byte array
      *
      * @return a copy of the byte array value with the specified name; if there
      * is no
      * item by this name, a null value is returned.
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      * @exception MessageFormatException if this type conversion is invalid.      
      */ 

    byte[] 
    getBytes(String name) throws JMSException;


    /** Returns the value of the object with the specified name.
      *
      * <P>This method can be used to return, in objectified format,
      * an object in the Java programming language ("Java object") that had 
      * been stored in the Map with the equivalent
      * <CODE>setObject</CODE> method call, or its equivalent primitive
      * <CODE>set<I>type</I></CODE> method.
      *
      * <P>Note that byte values are returned as <CODE>byte[]</CODE>, not 
      * <CODE>Byte[]</CODE>.
      *
      * @param name the name of the Java object
      *
      * @return a copy of the Java object value with the specified name, in 
      * objectified format (for example, if the object was set as an 
      * <CODE>int</CODE>, an <CODE>Integer</CODE> is returned); if there is no 
      * item by this name, a null value is returned
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      */ 

    Object 
    getObject(String name) throws JMSException;



    /** Returns an <CODE>Enumeration</CODE> of all the names in the 
      * <CODE>MapMessage</CODE> object.
      *
      * @return an enumeration of all the names in this <CODE>MapMessage</CODE>
      *
      * @exception JMSException if the JMS provider fails to read the message
      *                         due to some internal error.
      */

    Enumeration
    getMapNames() throws JMSException;


    /** Sets a <CODE>boolean</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>boolean</CODE>
      * @param value the <CODE>boolean</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */

    void 
    setBoolean(String name, boolean value) throws JMSException;


    /** Sets a <CODE>byte</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>byte</CODE>
      * @param value the <CODE>byte</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
     * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setByte(String name, byte value) 
			throws JMSException;


    /** Sets a <CODE>short</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>short</CODE>
      * @param value the <CODE>short</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
       * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setShort(String name, short value) 
			throws JMSException;


    /** Sets a Unicode character value with the specified name into the Map.
      *
      * @param name the name of the Unicode character
      * @param value the Unicode character value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
       * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setChar(String name, char value) 
			throws JMSException;


    /** Sets an <CODE>int</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>int</CODE>
      * @param value the <CODE>int</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setInt(String name, int value) 
			throws JMSException;


    /** Sets a <CODE>long</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>long</CODE>
      * @param value the <CODE>long</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setLong(String name, long value) 
			throws JMSException;


    /** Sets a <CODE>float</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>float</CODE>
      * @param value the <CODE>float</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
       * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setFloat(String name, float value) 
			throws JMSException;


    /** Sets a <CODE>double</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>double</CODE>
      * @param value the <CODE>double</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setDouble(String name, double value) 
			throws JMSException;


    /** Sets a <CODE>String</CODE> value with the specified name into the Map.
      *
      * @param name the name of the <CODE>String</CODE>
      * @param value the <CODE>String</CODE> value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setString(String name, String value) 
			throws JMSException;


    /** Sets a byte array value with the specified name into the Map.
      *
      * @param name the name of the byte array
      * @param value the byte array value to set in the Map; the array
      *              is copied so that the value for <CODE>name</CODE> will
      *              not be altered by future modifications
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception NullPointerException if the name is null, or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void
    setBytes(String name, byte[] value) 
			throws JMSException;


    /** Sets a portion of the byte array value with the specified name into the 
      * Map.
      *  
      * @param name the name of the byte array
      * @param value the byte array value to set in the Map
      * @param offset the initial offset within the byte array
      * @param length the number of bytes to use
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
       * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 
 
    void
    setBytes(String name, byte[] value, 
		 int offset, int length) 
			throws JMSException;


    /** Sets an object value with the specified name into the Map.
      *
      * <P>This method works only for the objectified primitive
      * object types (<code>Integer</code>, <code>Double</code>, 
      * <code>Long</code>&nbsp;...), <code>String</code> objects, and byte 
      * arrays.
      *
      * @param name the name of the Java object
      * @param value the Java object value to set in the Map
      *
      * @exception JMSException if the JMS provider fails to write the message
      *                         due to some internal error.
      * @exception IllegalArgumentException if the name is null or if the name is
      *                          an empty string.
      * @exception MessageFormatException if the object is invalid.
      * @exception MessageNotWriteableException if the message is in read-only 
      *                                         mode.
      */ 

    void 
    setObject(String name, Object value) 
			throws JMSException;


    /** Indicates whether an item exists in this <CODE>MapMessage</CODE> object.
      *
      * @param name the name of the item to test
      *
      * @return true if the item exists
      *
      * @exception JMSException if the JMS provider fails to determine if the 
      *                         item exists due to some internal error.
      */ 
 
    boolean
    itemExists(String name) throws JMSException;
}
